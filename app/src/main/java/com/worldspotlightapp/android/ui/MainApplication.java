package com.worldspotlightapp.android.ui;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.User;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.utils.Secret;
import io.fabric.sdk.android.Fabric;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class MainApplication extends Application {

    private static MainApplication sInstance;

    public static final boolean IS_PRODUCTION = true;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        // Initialize Parse
        Parse.enableLocalDatastore(sInstance);
        ParseObject.registerSubclass(Video.class);
        ParseObject.registerSubclass(User.class);
        if (IS_PRODUCTION) {
            Parse.initialize(this, Secret.PARSE_APPLICATION_ID_PRODUCTION, Secret.PARSE_CLIENT_KEY_PRODUCTION);
        } else {
            Parse.initialize(this, Secret.PARSE_APPLICATION_ID_DEBUG, Secret.PARSE_CLIENT_KEY_DEBUG);
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

        // Initialize Facebook utils provided by Parse
        ParseFacebookUtils.initialize(sInstance);

        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(sInstance);

        // Initialize Fabric/Crashlytics
        if (!IS_PRODUCTION) {
            Fabric.with(this, new Crashlytics());
        }
    }

    public static MainApplication getInstance() {
        return sInstance;
    }

}
