package com.worldspotlightapp.android.ui;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.utils.LocalConstants;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class MainApplication extends Application {

    private static MainApplication sInstance;

    public static final boolean IS_PRODUCTION = false;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        // Initialize Parse
        ParseObject.registerSubclass(Video.class);
        Parse.initialize(this, LocalConstants.PARSE_APPLICATION_ID, LocalConstants.PARSE_CLIENT_KEY);
        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
    }

    public static MainApplication getInstance() {
        return sInstance;
    }

}
