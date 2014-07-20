package com.dattilio.reader.network;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.dattilio.reader.R;
import com.dattilio.reader.persist.DBHelper;
import com.dattilio.reader.persist.ReaderContentProvider;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.comments.Comment;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public class NetworkService extends IntentService {

    public static final String ERROR_TEXT = "error";
    private static final String ACTION_GET = "com.dattilio.reader.action.GET";
    private static final String ACTION_GET_PHOTO_COMMENTS = "com.dattilio.reader.action.GET_PHOTO_COMMENTS";

    private static final String EXTRA_URL = "com.dattilio.reader.extra.URL";
    private static final String EXTRA_ID = "com.dattilio.reader.extra.ID";

    private Flickr mFlickr;

    public NetworkService() {
        super("NetworkService");
    }

    public static void startActionGet(Context context, String url) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_GET);
        intent.putExtra(EXTRA_URL, url);
        context.startService(intent);
    }

    public static void startActionGetPhotoComments(Context context, String id) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_GET_PHOTO_COMMENTS);
        intent.putExtra(EXTRA_ID, id);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                handleActionGet(url);
            } else if (ACTION_GET_PHOTO_COMMENTS.equals(action)) {
                final String id = intent.getStringExtra(EXTRA_ID);
                handleActionGetPhotoComments(id);
            }
        }
    }

    /**
     * When our service recieves an ACTION_GET, we attempt to parse an array of FeedItems from a JSON
     * string returned by the provided url.
     *
     * @param urlString - Url from which we want to retrieve the JSON string.
     */
    private void handleActionGet(String urlString) {
        try {
            String date = null;
            PhotoList photoList = getFlickr().getInterestingnessInterface().getList(date, null, 25, 0);

//            OkHttpClient client = new OkHttpClient();
//            URL url = new URL(urlString);
//            Gson gson = new Gson();
//            //Using OkHttp and GSON we are able to do the downloading and parsing in essentially one line
//            JsonReader reader = new JsonReader(new InputStreamReader(client.open(url).getInputStream()));
//            FeedItem[] items = gson.fromJson(reader, FeedItem[].class);
//            reader.close();

            //Now we update the ContentProvider with the results
            ContentResolver contentResolver = getContentResolver();
            for (Photo photo : photoList) {
                ContentValues values = new ContentValues();
                values.put(DBHelper.ID, photo.getId());
                values.put(DBHelper.URL, photo.getUrl());
                values.put(DBHelper.FARM, photo.getFarm());
                values.put(DBHelper.TITLE, photo.getTitle());
                values.put(DBHelper.OWNER, photo.getOwner().getId());
                values.put(DBHelper.SERVER, photo.getServer());
                values.put(DBHelper.SECRET, photo.getSecret());
                contentResolver.insert(ReaderContentProvider.PHOTO_URI, values);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleActionGetPhotoComments(String id) {
        try {
            List<Comment> list = getFlickr().getCommentsInterface().getList(id, null, null);
            ContentResolver contentResolver = getContentResolver();
            for (Comment comment : list) {
                ContentValues values = new ContentValues();
                values.put(DBHelper.ID, comment.getId());
                values.put(DBHelper.PHOTO_ID, id);
                values.put(DBHelper.AUTHOR, comment.getAuthor());
                values.put(DBHelper.AUTHOR_NAME, comment.getAuthorName());
                values.put(DBHelper.CONTENT, comment.getText());
                contentResolver.insert(ReaderContentProvider.COMMENT_URI, values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FlickrException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * If we fail to retrieve our data, we broadcast an error message to be displayed
     * in our Activity
     *
     * @param e An exception for which we need to display an error message.
     */
    private void handleException(Exception e) {
        String error = getString(R.string.default_network_error);
        if (e instanceof MalformedURLException) {
            error = getString(R.string.malformed_url_error);
        } else if (e instanceof IOException) {
            error = getString(R.string.io_error);
        }
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ResponseReceiver.ERROR_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(ERROR_TEXT, error);
        sendBroadcast(broadcastIntent);
    }

    private Flickr getFlickr() {
        if (mFlickr == null) {
            mFlickr = new Flickr(getString(R.string.flickr_api_key));

        }
        return mFlickr;
    }
}