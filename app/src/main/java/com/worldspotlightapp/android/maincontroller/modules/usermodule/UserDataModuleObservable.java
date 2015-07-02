package com.worldspotlightapp.android.maincontroller.modules.usermodule;

import java.util.Observer;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.worldspotlightapp.android.maincontroller.Preferences;
import com.worldspotlightapp.android.maincontroller.Preferences.StringId;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleResponse;
import com.worldspotlightapp.android.ui.MainApplication;


public class UserDataModuleObservable extends AbstractUserDataModuleObservable {

    private static final String LOG_TAG = "UserDataModule";

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final Preferences mPreferences;

    private ParseUser mParseUser;

    /**
     * The unique identifier of the device/user
     */
    private volatile UUID mUuid;

    private Object lock = new Object();

    public UserDataModuleObservable(Preferences preferences) {
        super();
        this.mPreferences = preferences;
        generateUUID();
        mParseUser = ParseUser.getCurrentUser();
    }

    @Override
    public UUID getUuid() {
        if (mUuid == null) {
            generateUUID();
        }

        return mUuid;
    }

    @Override
    public boolean hasUserData() {
        return mParseUser != null;
    }

    @Override
    public void loginWithFacebook(Observer observer, Activity activity) {
        addObserver(observer);
        ParseFacebookUtils.logInWithReadPermissionsInBackground(activity, null, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    if (parseUser != null) {
                        UserDataModuleResponse userDataModuleResponse = new UserDataModuleResponse(parseResponse);
                        if (parseUser.isNew()) {
                            // User signed up and logged in through Facebook
                            setChanged();
                            notifyObservers(userDataModuleResponse);
                        } else {
                            // User logged in through Facebook
                            setChanged();
                            notifyObservers(userDataModuleResponse);
                        }
                        // User has signed in but the parse user is false. This is an inconsistent state.
                    } else {
                        // if the current user exists
                        if (ParseUser.getCurrentUser() != null) {
                            UserDataModuleResponse userDataModuleResponse = new UserDataModuleResponse(parseResponse);
                            setChanged();
                            notifyObservers(userDataModuleResponse);
                            // The user has logged with Facebook but the current user does not exists.
                            // Show the error to the user
                        } else {
                            // Update the Parse response
                            ParseResponse loginErrorParseResponse =
                                    new ParseResponse.Builder(e).statusCode(ParseResponse.ERROR_LOGIN_WITH_FACEBOOK).build();
                            UserDataModuleResponse userDataModuleResponse = new UserDataModuleResponse(loginErrorParseResponse);
                            setChanged();
                            notifyObservers(userDataModuleResponse);
                        }
                    }
                    // Some error happend. Show them to the user
                } else {
                    UserDataModuleResponse userDataModuleResponse = new UserDataModuleResponse(parseResponse);
                    setChanged();
                    notifyObservers(userDataModuleResponse);
                }
            }
        });
    }

    /**
     * Based on the code in StackOverFlow:
     * http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id Returns a
     * unique UUID for the current android device. As with all UUIDs, this unique ID is
     * "very highly likely" to be unique across all Android devices. Much more so than ANDROID_ID
     * is.
     *
     * The UUID is generated by using ANDROID_ID as the base key if appropriate, falling back on
     * TelephonyManager.getDeviceID() if ANDROID_ID is known to be incorrect, and finally falling
     * back on a random UUID that's persisted to SharedPreferences if getDeviceID() does not return
     * a usable value.
     *
     * In some rare circumstances, this ID may change. In particular, if the device is factory reset
     * a new device ID may be generated. In addition, if a user upgrades their phone from certain
     * buggy implementations of Android 2.2 to a newer, non-buggy version of Android, the device ID
     * may change. Or, if a user uninstalls your app on a device that has neither a proper Android
     * ID nor a Device ID, this ID may change on reinstallation.
     *
     * Note that if the code falls back on using TelephonyManager.getDeviceId(), the resulting ID
     * will NOT change after a factory reset. Something to be aware of.
     *
     * Works around a bug in Android 2.2 for many devices when using ANDROID_ID directly.
     *
     * @see http://code.google.com/p/android/issues/detail?id=10603
     *
     * @return a UUID that may be used to uniquely identify your device for most purposes.
     */
    private void generateUUID() {
        // If the uuid has not been generated before
        synchronized (lock) {
            // It could be that the UUID has been generated by other threads
            // that has the lock. Check it again
            if (mUuid == null) {
                final String id = mPreferences.get(StringId.UUID);
                if (id != null) {
                    // Use the ids previously computed and stored in the
                    // prefs file
                    mUuid = UUID.fromString(id);
                } else {
                    final String androidId = Secure.getString(MainApplication.getInstance()
                            .getContentResolver(), Secure.ANDROID_ID);
                    // Use the Android ID unless it's broken, in which case
                    // fallback on deviceId,
                    // unless it's not available, then fallback on a random
                    // number which we store to a prefs file
                    if (!"9774d56d682e549c".equals(androidId)) {
                        mUuid = UUID.nameUUIDFromBytes(androidId.getBytes());
                    } else {
                        final String deviceId = ((TelephonyManager) MainApplication.getInstance()
                                .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                        mUuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes())
                                : UUID.randomUUID();
                    }
                    // Write the value out to the prefs file
                    mPreferences.set(StringId.UUID, mUuid.toString());
                }
            }
        }
    }
}