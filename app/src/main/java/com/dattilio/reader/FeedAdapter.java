package com.dattilio.reader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dattilio.reader.network.NetworkService;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;
import com.squareup.picasso.Picasso;


class FeedAdapter extends CursorAdapter {

    public FeedAdapter(Context context, Cursor cursor) {
        super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = new SquareImageView(context);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.image = (SquareImageView) view;
        view.setTag(viewHolder);
        ((ImageView) view).setScaleType(ImageView.ScaleType.CENTER_CROP);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder vhold = (ViewHolder) view.getTag();
        User user = new User();
        user.setId(cursor.getString(PhotoQuery.OWNER));
        user.setUsername(cursor.getString(PhotoQuery.OWNER_NAME));
        Photo photo = new Photo();
        photo.setId(cursor.getString(PhotoQuery.ID));
        photo.setUrl(cursor.getString(PhotoQuery.URL));
        photo.setFarm(cursor.getString(PhotoQuery.FARM));
        photo.setTitle(cursor.getString(PhotoQuery.TITLE));
        photo.setOwner(user);
        photo.setServer(cursor.getString(PhotoQuery.SERVER));
        photo.setSecret(cursor.getString(PhotoQuery.SECRET));

        //Load the image using Picasso into the photo image, resizing/cropping to fit.
        Picasso.with(context).load(photo.getLargeSquareUrl()).placeholder(R.drawable.placeholder).fit().into(vhold.image);
        view.setOnClickListener(new PhotoOnClickListener(photo, context, cursor.getPosition()));
    }

    /* An interface defining the column numbers for each field in a photo */
    private interface PhotoQuery {
        static final int ID = 0;
        static final int URL = 1;
        static final int FARM = 2;
        static final int TITLE = 3;
        static final int OWNER = 4;
        static final int OWNER_NAME = 5;
        static final int SERVER = 6;
        static final int SECRET = 7;
    }

    private static class ViewHolder {
        SquareImageView image;
    }

    /**
     * Clicking an photo will start an ItemActionMode
     */
    private class PhotoOnClickListener implements View.OnClickListener {
        private final Photo item;
        private final Context context;

        public PhotoOnClickListener(Photo item, Context context, int position) {
            this.item = item;
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            NetworkService.startActionGetPhotoComments(context, item.getId());
            Intent intent = new Intent(context, PhotoCommentActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("photo", item);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }

//    /**
//     * Currently gives two options for a selected photo, opening an ACTION_VIEW intent
//     * (same as long press) or a share intent.
//     */
//    private class ItemActionMode implements ActionMode.Callback {
//        private final Photo selectedItem;
//        private final int position;
//        private final Context context;
//
//        public ItemActionMode(Photo item, Context context, int position) {
//            this.selectedItem = item;
//            this.context = context;
//            this.position = position;
//        }
//
//        @Override
//        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//            ((GridView) ((FeedReaderActivity) context).findViewById(R.id.gridview)).setItemChecked(position, true);
//            mode.getMenuInflater().inflate(R.menu.action_mode, menu);
//            return true;
//        }
//
//        @Override
//        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//            return true;
//        }
//
//        @Override
//        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            if (item.getItemId() == R.id.open_pin && selectedItem != null) {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(selectedItem.getUrl()));
//                context.startActivity(i);
//            } else if (item.getItemId() == R.id.share && selectedItem != null) {
//                Intent i = new Intent(Intent.ACTION_SEND);
//                i.setType("text/plain");
//                i.putExtra(Intent.EXTRA_SUBJECT, selectedItem.getTitle());
//                i.putExtra(Intent.EXTRA_TEXT, selectedItem.getUrl());
//                context.startActivity(Intent.createChooser(i, "Share Pin"));
//            }
//            return false;
//        }
//
//        @Override
//        public void onDestroyActionMode(ActionMode mode) {
//            ((ListView) ((FeedReaderActivity) context).findViewById(R.id.gridview)).setItemChecked(position, false);
//        }
//    }

}
