package com.dattilio.reader.network;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.dattilio.reader.R;
import com.dattilio.reader.persist.DBHelper;
import com.dattilio.reader.persist.ReaderContentProvider;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.comments.Comment;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NetworkService extends IntentService {

    public static final String ERROR_TEXT = "error";
    private static final String ACTION_GET = "com.dattilio.reader.action.GET";
    private static final String ACTION_GET_PHOTO_COMMENTS = "com.dattilio.reader.action.GET_PHOTO_COMMENTS";

    private static final String EXTRA_PHOTO_PER_REQUEST = "com.dattilio.reader.extra.PHOTO_PER_REQUEST";
    private static final String EXTRA_CURRENT_PAGE = "com.dattilio.reader.extra.CURRENT_PAGE";
    private static final String EXTRA_ID = "com.dattilio.reader.extra.ID";

    private Flickr mFlickr;

    public NetworkService() {
        super("NetworkService");
    }

    public static void startActionGetPhotos(Context context) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_GET);
        SharedPreferences preferences = context.getSharedPreferences("flictures", MODE_PRIVATE);
        int currentPage = preferences.getInt("currentPage", 0);
        intent.putExtra(EXTRA_PHOTO_PER_REQUEST, context.getResources().getInteger(R.integer.photos_per_request));
        intent.putExtra(EXTRA_CURRENT_PAGE, currentPage);
        context.startService(intent);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("currentPage", ++currentPage);
        editor.apply();

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
                int photoPerRequest = intent.getIntExtra(EXTRA_PHOTO_PER_REQUEST, 25);
                int currentPage = intent.getIntExtra(EXTRA_CURRENT_PAGE, 0);
                handleActionGet(photoPerRequest, currentPage);
            } else if (ACTION_GET_PHOTO_COMMENTS.equals(action)) {
                final String id = intent.getStringExtra(EXTRA_ID);
                handleActionGetPhotoComments(id);
            }
        }
    }

    private void handleActionGet(int photoPerRequest, int currentPage) {
        try {
            String date = null;
            Set<String> extras = new HashSet<String>();
            extras.add("owner_name");
            PhotoList photoList = getFlickr().getInterestingnessInterface().getList(date, extras, photoPerRequest, currentPage);
            //Now we update the ContentProvider with the results
            ContentResolver contentResolver = getContentResolver();
            for (Photo photo : photoList) {
                ContentValues values = new ContentValues();
                values.put(DBHelper.ID, photo.getId());
                values.put(DBHelper.URL, photo.getUrl());
                values.put(DBHelper.FARM, photo.getFarm());
                values.put(DBHelper.TITLE, photo.getTitle());
                values.put(DBHelper.OWNER, photo.getOwner().getId());
                values.put(DBHelper.OWNER_NAME, photo.getOwner().getUsername());
                values.put(DBHelper.SERVER, photo.getServer());
                values.put(DBHelper.SECRET, photo.getSecret());
                values.put(DBHelper.TIMESTAMP, System.currentTimeMillis());
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
        } catch (Exception e) {
            handleException(e);
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
        } else if (e instanceof FlickrException) {
            error = ((FlickrException) e).getErrorMessage();
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