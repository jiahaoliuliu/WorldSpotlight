package com.worldspotlightapp.android.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.worldspotlightapp.android.R;

import java.util.Observable;

public class SignUpActivity extends AbstractBaseActivityObserver {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }

    @Override
    protected void processDataIfExists() {
        // TODO: implement this
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO: Implement this
    }
}
