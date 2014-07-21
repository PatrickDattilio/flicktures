package com.dattilio.reader.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.dattilio.reader.R;

/**
 * Created by Patrick Dattilio on 4/23/2014.
 */
public class ResponseReceiver extends BroadcastReceiver {
    public static final String ERROR_RESPONSE =
            "com.dattilio.intent.action.ERROR";

    private final Activity mActivity;

    public ResponseReceiver(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String error = intent.getStringExtra(NetworkService.ERROR_TEXT);

        Toast.makeText(context, error, Toast.LENGTH_LONG).show();

        if (mActivity.findViewById(R.id.progressbar) != null)
            mActivity.findViewById(R.id.progressbar).setVisibility(View.INVISIBLE);
        if (mActivity.findViewById(R.id.gridview) != null)
            mActivity.findViewById(R.id.gridview).setVisibility(View.VISIBLE);
    }
}
