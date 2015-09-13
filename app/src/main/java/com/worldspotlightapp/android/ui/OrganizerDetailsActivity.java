package com.worldspotlightapp.android.ui;

import android.os.Bundle;
import com.worldspotlightapp.android.R;

import java.util.Observable;

public class OrganizerDetailsActivity extends AbstractBaseActivityObserver {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_details);
    }

    @Override
    protected void processDataIfExists() {
        // TODO: implement this
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO: implement this
    }
}
