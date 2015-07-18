package com.worldspotlightapp.android.maincontroller.modules.usermodule;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.worldspotlightapp.android.maincontroller.Preferences;
import com.worldspotlightapp.android.maincontroller.Preferences.StringId;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleLikeResponse;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleUnlikeResponse;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleUserResponse;
import com.worldspotlightapp.android.model.Like;
import com.worldspotlightapp.android.model.UserData;
import com.worldspotlightapp.android.ui.MainApplication;
import com.worldspotlightapp.android.utils.Secret;


public class UserDataModuleObservable extends AbstractUserDataModuleObservable {

    private static final String TAG = "UserDataModule";

    private final Preferences mPreferences;

    private UserData mUserData;

    private List<Like> mLikedVideosList;

    /**
     * The unique identifier of the device/user
     */
    private volatile UUID mUuid;

    private Object lock = new Object();

    public UserDataModuleObservable(Preferences preferences) {
        super();
        this.mPreferences = preferences;
        generateUUID();
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            mUserData = new UserData(parseUser);
            updateUserDataIfNeeded();
        }
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
        if (mUserData == null && ParseUser.getCurrentUser() != null) {
            mUserData = new UserData(ParseUser.getCurrentUser());
            updateUserDataIfNeeded();
        }

        return mUserData != null;
    }

    @Override
    public UserData getUserData() {
        return mUserData;
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
                        mUserData = new UserData(parseUser);
                        updateUserDataIfNeeded();
                        UserDataModuleUserResponse userDataModuleUserResponse = new UserDataModuleUserResponse(parseResponse, mUserData);
                        if (mUserData.isNew()) {
                            // User signed up and logged in through Facebook
                            setChanged();
                            notifyObservers(userDataModuleUserResponse);
                        } else {
                            // User logged in through Facebook
                            setChanged();
                            notifyObservers(userDataModuleUserResponse);
                        }
                        // User has signed in but the parse user is false. This is an inconsistent state.
                    } else {
                        // if the current user exists
                        if (ParseUser.getCurrentUser() != null) {
                            mUserData = new UserData(ParseUser.getCurrentUser());
                            updateUserDataIfNeeded();
                            UserDataModuleUserResponse userDataModuleUserResponse = new UserDataModuleUserResponse(parseResponse, mUserData);
                            setChanged();
                            notifyObservers(userDataModuleUserResponse);
                            // The user has logged with Facebook but the current user does not exists.
                            // Show the error to the user
                        } else {
                            // Update the Parse response
                            ParseResponse loginErrorParseResponse =
                                    new ParseResponse.Builder(e).statusCode(ParseResponse.ERROR_LOGIN_WITH_FACEBOOK).build();
                            UserDataModuleUserResponse userDataModuleUserResponse = new UserDataModuleUserResponse(loginErrorParseResponse, null);
                            setChanged();
                            notifyObservers(userDataModuleUserResponse);
                        }
                    }
                    // Some error happend. Show them to the user
                } else {
                    UserDataModuleUserResponse userDataModuleUserResponse = new UserDataModuleUserResponse(parseResponse, null);
                    setChanged();
                    notifyObservers(userDataModuleUserResponse);
                }
            }
        });
    }

    @Override
    public void signupForGooglePlusUsers(Observer observer, final String name, final String email, final String profilePhotoUrl, final String profileUrl) {
        addObserver(observer);
        final String password = Secret.generatePassword(name, email);

        ParseUser.logInInBackground(email, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                // If there is not error on login
                if (!parseResponse.isError()) {
                    if (parseUser != null) {
                        mUserData = new UserData(parseUser);
                        updateUserDataIfNeeded();
                        UserDataModuleUserResponse userDataModuleUserResponse = new UserDataModuleUserResponse(parseResponse, mUserData);
                        if (ParseUser.getCurrentUser().isNew()) {
                            // User signed up and logged in through Facebook
                            setChanged();
                            notifyObservers(userDataModuleUserResponse);
                        } else {
                            // User logged in through Facebook
                            setChanged();
                            notifyObservers(userDataModuleUserResponse);
                        }
                        // User has signed in but the parse user is false. This is an inconsistent state.
                    } else {
                        // if the current user exists
                        if (ParseUser.getCurrentUser() != null) {
                            mUserData = new UserData(parseUser.getCurrentUser());
                            updateUserDataIfNeeded();
                            UserDataModuleUserResponse userDataModuleUserResponse = new UserDataModuleUserResponse(parseResponse, mUserData);
                            setChanged();
                            notifyObservers(userDataModuleUserResponse);
                            // The user has logged with Facebook but the current user does not exists.
                            // Show the error to the user
                        } else {
                            // Update the Parse response
                            ParseResponse loginErrorParseResponse =
                                    new ParseResponse.Builder(e).statusCode(ParseResponse.ERROR_LOGIN_WITH_GOOGLE).build();
                            UserDataModuleUserResponse userDataModuleUserResponse = new UserDataModuleUserResponse(loginErrorParseResponse, null);
                            setChanged();
                            notifyObservers(userDataModuleUserResponse);
                        }
                    }
                    // If there is any error on login, try to sign up
                } else {
                    mUserData = new UserData(name, email, email, password, profilePhotoUrl, true, profileUrl);
                    mUserData.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                            // If sign up was ok
                            if (!parseResponse.isError()) {
                                updateUserDataIfNeeded();
                                UserDataModuleUserResponse userDataModuleUserResponse = new UserDataModuleUserResponse(parseResponse, mUserData);
                                setChanged();
                                notifyObservers(userDataModuleUserResponse);
                                // If there is any problem on Sign up
                            } else {
                                // Reset the value of User
                                mUserData = null;
                                ParseResponse signupErrorParseResponse =
                                        new ParseResponse.Builder(e).statusCode(ParseResponse.ERROR_LOGIN_WITH_GOOGLE).build();
                                UserDataModuleUserResponse userDataModuleUserResponse = new UserDataModuleUserResponse(signupErrorParseResponse, null);
                                setChanged();
                                notifyObservers(userDataModuleUserResponse);
                            }
                        }
                    });
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

    @Override
    public void likeAVideo(Observer observer, boolean likeIt, final String videoId) {
        addObserver(observer);
        if (!hasUserData() || mUserData == null) {
            Log.e(TAG, "Trying to like a video while the user has not logged in");
            ParseResponse parseResponse =
                    new ParseResponse.Builder(null).statusCode(ParseResponse.ERROR_USER_NOT_LOGGED_IN).build();
            setChanged();
            UserDataModuleLikeResponse userDataModuleLikeResponse =
                    new UserDataModuleLikeResponse(parseResponse, null);
            setChanged();
            notifyObservers(userDataModuleLikeResponse);
            return;
        }

        final Like newLike = new Like(mUserData.getObjectId(), videoId);

        // Try to avoid the null problem
        if (mLikedVideosList == null) {
            mLikedVideosList = new ArrayList<Like>();
        }

        // If the user likes a video
        if (likeIt) {
            // Only update if the user does not liked the video before
            if (!mLikedVideosList.contains(newLike)) {
                mLikedVideosList.add(newLike);
                // Save the like
                newLike.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                        if (!parseResponse.isError()) {
                            Log.v(TAG, "Like saved correcty. It has the object id " + newLike.getObjectId());
                            UserDataModuleLikeResponse userDataModuleLikeResponse = new UserDataModuleLikeResponse(parseResponse, newLike);
                            setChanged();
                            notifyObservers(userDataModuleLikeResponse);
                        // There was some error
                        } else {
                            Log.e(TAG, "Error saving the like for the user ", e);
                            UserDataModuleLikeResponse userDataModuleLikeResponse = new UserDataModuleLikeResponse(parseResponse, null);
                            setChanged();
                            notifyObservers(userDataModuleLikeResponse);
                        }
                    }
                });
            }
        // If the user does not like a video
        } else {
            // Only update if the user does liked the video before
            if (mLikedVideosList.contains(newLike)) {
                // the recent created like cannot be used because it does not contains the objectId. To be deleted,
                // the
                Like likeToBeRemoved = mLikedVideosList.get(mLikedVideosList.indexOf(newLike));
                mLikedVideosList.remove(likeToBeRemoved);
                likeToBeRemoved.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                        if (!parseResponse.isError()) {
                            UserDataModuleUnlikeResponse userDataModuleUnlikeResponse = new UserDataModuleUnlikeResponse(parseResponse, newLike);
                            setChanged();
                            notifyObservers(userDataModuleUnlikeResponse);
                            // There was some error
                        } else {
                            Log.e(TAG, "Error deleting the like for the user ", e);
                            UserDataModuleUnlikeResponse userDataModuleUnlikeResponse = new UserDataModuleUnlikeResponse(parseResponse, newLike);
                            setChanged();
                            notifyObservers(userDataModuleUnlikeResponse);
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean doesUserLikeThisVideo(String videoId) {
        if (mLikedVideosList == null) {
            return false;
        }

        // If the user does not exists, not do anything
        if (!hasUserData() || mUserData == null) {
            return false;
        }

        final Like newLike = new Like(mUserData.getObjectId(), videoId);

        return mLikedVideosList.contains(newLike);
    }

    @Override
    public void logout() {
        if (hasUserData()) {
            ParseUser.logOut();
            mUserData = null;
        }
    }

    /**
     * Update all the data related with the user, such as liked videos or so.
     */
    private void updateUserDataIfNeeded() {
        Log.v(TAG, "Updating user data if needed");
        if (!hasUserData() || mUserData == null) {
            Log.e(TAG, "Trying to update user data when the user has not logged in");
            return;
        }

        // Update object id
        if (mUserData.getObjectId() == null) {
            Log.v(TAG, "The user does not have the object id. Get it from the current user");
            ParseUser parseUser = ParseUser.getCurrentUser();
            if (parseUser != null) {
                Log.v(TAG, "Current user exists and it is " + parseUser);
                mUserData.setObjectId(parseUser.getObjectId());
            } else {
                Log.e(TAG, "Current user does not exists!");
            }
        }

        // Update the list of liked video only if needed
        if (mLikedVideosList != null) {
            return;
        }

        ParseQuery<Like> parseQueryForLikes = ParseQuery.getQuery(Like.class);
        parseQueryForLikes.whereEqualTo(Like.PARSE_TABLE_COLUMN_USER_ID, mUserData.getObjectId());
        parseQueryForLikes.findInBackground(new FindCallback<Like>() {
            @Override
            public void done(List<Like> list, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    Log.v(TAG, "The list of likes has been correctly retrieved from the backed " + list);
                    mLikedVideosList = list;
                } else {
                    Log.e(TAG, "Error retrieving the list of likes from backend");
                }
            }
        });
    }
}