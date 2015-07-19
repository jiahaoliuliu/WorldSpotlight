package com.worldspotlightapp.android.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.worldspotlightapp.android.R;

import java.util.Observable;

public class LoginActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void processDataIfExists() {
        // TODO: Implement this
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
