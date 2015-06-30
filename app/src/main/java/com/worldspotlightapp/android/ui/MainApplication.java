package com.worldspotlightapp.android.ui;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.utils.LocalConstants;
import io.fabric.sdk.android.Fabric;

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
        if (IS_PRODUCTION) {
            Parse.initialize(this, LocalConstants.PARSE_APPLICATION_ID_PRODUCTION, LocalConstants.PARSE_CLIENT_KEY_PRODUCTION);
        } else {
            Parse.initialize(this, LocalConstants.PARSE_APPLICATION_ID_DEBUG, LocalConstants.PARSE_CLIENT_KEY_DEBUG);
        }
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

        // Initialize Fabric/Crashlytics
        if (!IS_PRODUCTION) {
            Fabric.with(this, new Crashlytics());
        }
    }

    public static MainApplication getInstance() {
        return sInstance;
    }

}
