package com.worldspotlightapp.android;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Parse
        Parse.initialize(this, "w2gtAwnJOsaTsf1DhBqOUblbCa9Pec91IA4uM875", "qIp5KT6KyyvLr7D3rQPm0kY2znawZ1jwQbHrMAe8");
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
}
