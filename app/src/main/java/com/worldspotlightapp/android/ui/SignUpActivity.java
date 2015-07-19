package com.worldspotlightapp.android.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.worldspotlightapp.android.R;

import java.util.Observable;

public class SignUpActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void processDataIfExists() {
        // TODO: implement this
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO: Implement this
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.v(TAG, "home button pressed");
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
