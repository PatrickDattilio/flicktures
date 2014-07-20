package com.dattilio.reader;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dattilio.reader.network.NetworkService;
import com.dattilio.reader.persist.DBHelper;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;
import com.squareup.picasso.Picasso;


class FeedAdapter extends CursorAdapter {

    private final int srcWidth;
    private final int srcHeight;
    private final int avatarWidth;
    private final int avatarHeight;

    private LayoutInflater mInflater;

    public FeedAdapter(Context context, Cursor cursor) {

        super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
        Resources res = context.getResources();

        //Item image and avatar image dimensions are set via resource here allowing tablet/phones
        //and other DPI buckets to have specific sizes using only xml
        srcWidth = (int) res.getDimension(R.dimen.src_width);
        srcHeight = (int) res.getDimension(R.dimen.src_height);
        avatarWidth = (int) res.getDimension(R.dimen.avatar_width);
        avatarHeight = (int) res.getDimension(R.dimen.avatar_height);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = new SquareImageView(context);//mInflater.inflate(R.layout.grid_item, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.image = (SquareImageView) view;
//        viewHolder.title = (TextView) view.findViewById(R.id.item_title);
//        viewHolder.url = (TextView) view.findViewById(R.id.item_url);
//        viewHolder.userImage = (ImageView) view.findViewById(R.id.item_user_image);
//        viewHolder.userName = (TextView) view.findViewById(R.id.item_user_name);
        view.setTag(viewHolder);
        ((ImageView) view).setScaleType(ImageView.ScaleType.CENTER_CROP);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder vhold = (ViewHolder) view.getTag();
        User user = new User();
        user.setId(cursor.getString(PhotoQuery.OWNER));
        com.googlecode.flickrjandroid.photos.Photo photo = new com.googlecode.flickrjandroid.photos.Photo();
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
        static final int SERVER = 5;
        static final int SECRET = 6;
    }

    private static class ViewHolder {
        SquareImageView image;
        TextView title;
        TextView url;
        ImageView userImage;
        TextView userName;
    }

//    /**
//     * A long press on an photo will launch an ACTION_VIEW intent
//     */
//    private class PhotoOnLongClickListener implements View.OnLongClickListener {
//        final Photo photo;
//
//        public PhotoOnLongClickListener(Photo item) {
//            this.photo = item;
//        }
//
//        @Override
//        public boolean onLongClick(View v) {
//            Intent i = new Intent(Intent.ACTION_VIEW);
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            i.setData(Uri.parse(photo.getUrl()));
//            v.getContext().getApplicationContext().startActivity(i);
//            return true;
//        }
//    }

    /**
     * Clicking an photo will start an ItemActionMode
     */
    private class PhotoOnClickListener implements View.OnClickListener {
        private final Photo item;
        private final int position;
        private final Context context;

        public PhotoOnClickListener(Photo item, Context context, int position) {
            this.item = item;
            this.context = context;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            NetworkService.startActionGetPhotoComments(context, item.getId());
            Intent intent = new Intent(context, PhotoCommentActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(DBHelper.ID, item.getId());
            bundle.putString(DBHelper.TITLE, item.getTitle());
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
