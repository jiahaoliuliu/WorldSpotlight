package com.worldspotlightapp.android.ui;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.worldspotlightapp.android.model.City;
import com.worldspotlightapp.android.model.HashTag;
import com.worldspotlightapp.android.model.Like;
import com.worldspotlightapp.android.model.Organize;
import com.worldspotlightapp.android.model.Organizer;
import com.worldspotlightapp.android.model.Report;
import com.worldspotlightapp.android.model.UserData;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.utils.DebugOptions;
import com.worldspotlightapp.android.utils.Secret;
import io.fabric.sdk.android.Fabric;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class MainApplication extends MultiDexApplication {

    private static MainApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        // Initialize Parse
        ParseObject.registerSubclass(HashTag.class);
        ParseObject.registerSubclass(Video.class);
        ParseObject.registerSubclass(UserData.class);
        ParseObject.registerSubclass(Like.class);
        ParseObject.registerSubclass(Report.class);
        ParseObject.registerSubclass(City.class);
        ParseObject.registerSubclass(Organizer.class);
        ParseObject.registerSubclass(Organize.class);
        if (DebugOptions.IS_PRODUCTION) {
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
        if (!DebugOptions.IS_PRODUCTION) {
            Fabric.with(this, new Crashlytics());
        }
    }

    public static MainApplication getInstance() {
        return sInstance;
    }

}
