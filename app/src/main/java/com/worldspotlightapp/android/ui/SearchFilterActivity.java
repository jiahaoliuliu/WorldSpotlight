package com.worldspotlightapp.android.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.worldspotlightapp.android.R;

import java.util.Observable;

public class SearchFilterActivity extends AbstractBaseActivityObserver {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filter);
    }

    @Override
    protected void processDataIfExists() {
        //TODO: Implement this
    }

    @Override
    public void update(Observable observable, Object o) {
        //TODO: Implement this
    }
}
