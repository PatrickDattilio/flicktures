package com.dattilio.reader;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.dattilio.reader.network.ResponseReceiver;

public class PhotoCommentActivity extends ActionBarActivity {

    private ResponseReceiver mReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_comment);

        mReceiver = new ResponseReceiver(this);
        if (savedInstanceState == null) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ResponseReceiver.ERROR_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}

