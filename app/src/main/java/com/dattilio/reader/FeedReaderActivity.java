package com.dattilio.reader;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;

import com.dattilio.reader.network.NetworkService;
import com.dattilio.reader.network.ResponseReceiver;
import com.dattilio.reader.persist.DBHelper;
import com.dattilio.reader.persist.ReaderContentProvider;

public class FeedReaderActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>, AbsListView.OnScrollListener {

    private static final int FEED_LOADER = 0;
    private FeedAdapter mAdapter;
    private ResponseReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feed_reader);

        receiver = new ResponseReceiver(this);
        getSupportLoaderManager().initLoader(FEED_LOADER, null, FeedReaderActivity.this);
        mAdapter = new FeedAdapter(FeedReaderActivity.this, null);
        ((GridView) findViewById(R.id.gridview)).setAdapter(mAdapter);
        ((GridView) findViewById(R.id.gridview)).setOnScrollListener(this);

        findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
                findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
                findViewById(R.id.gridview).setVisibility(View.VISIBLE);
                findViewById(R.id.error_layout).setVisibility(View.INVISIBLE);
            }
        });
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ResponseReceiver.ERROR_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void refresh() {
        NetworkService.startActionGetPhotos(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case FEED_LOADER:
                return new CursorLoader(this, ReaderContentProvider.PHOTO_URI, null, null, null, DBHelper.TIMESTAMP);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        if (!data.isAfterLast()) {
            findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
            mAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            if (view.getLastVisiblePosition() > (mAdapter.getCount() / 2)) {
                NetworkService.startActionGetPhotos(view.getContext());
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
