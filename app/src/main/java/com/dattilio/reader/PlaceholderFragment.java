package com.dattilio.reader;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dattilio.reader.persist.DBHelper;
import com.dattilio.reader.persist.ReaderContentProvider;
import com.googlecode.flickrjandroid.photos.Photo;
import com.squareup.picasso.Picasso;

/**
 * Created by Pmoney on 7/20/2014.
 */
public class PlaceholderFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int COMMENT_LOADER = 0;
    private CommentAdapter mAdapter;
    private Photo mPhoto;

    public PlaceholderFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhoto = (Photo) getArguments().getSerializable("photo");
        getLoaderManager().initLoader(COMMENT_LOADER, null, this);
        mAdapter = new CommentAdapter(getActivity(), null);
        setListAdapter(mAdapter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo_comment, container, false);
        Photo photo = (Photo) getArguments().getSerializable("photo");

        Picasso.with(getActivity()).load(photo.getLargeSquareUrl()).placeholder(R.drawable.placeholder).resizeDimen(R.dimen.header_photo_width, R.dimen.header_photo_height).into((ImageView) rootView.findViewById(R.id.header_photo_comment_photo));
        ((TextView) rootView.findViewById(R.id.header_photo_comment_title)).setText(photo.getTitle());
        ((TextView) rootView.findViewById(R.id.header_photo_comment_author)).setText(photo.getOwner().getUsername());

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case COMMENT_LOADER:
                return new CursorLoader(getActivity(), ReaderContentProvider.COMMENT_URI, null, DBHelper.PHOTO_ID + " = ?", new String[]{mPhoto.getId()}, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        if (!data.isAfterLast()) {
            //findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
            mAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }


}
