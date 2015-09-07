
package com.worldspotlightapp.android.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.interfaces.IOnActionBarRequestListener;
import com.worldspotlightapp.android.maincontroller.MainController;
import com.worldspotlightapp.android.maincontroller.modules.activitytrackermodule.IActivityTrackerModule;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.AbstractCityModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule;
import com.worldspotlightapp.android.maincontroller.modules.gpslocalizationmodule.IGpsLocalizationModule;
import com.worldspotlightapp.android.maincontroller.modules.notificationmodule.INotificationModule;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.AbstractUserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.AbstractVideosModuleObservable;

/**
 * Abstract base class created to be extended by all the activities.
 * It contain the follow features
 * - Connection with MainController and all its modules
 * - Check for Google Play services
 */
public abstract class AbstractBaseActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, MainController.IOnMainControllerInstantiatedListener,
        IOnActionBarRequestListener {

    private static final String TAG = "AbstractBaseActivity";

    //    Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 99999;

    // YouTube Package Name
    private static final String YOU_TUBE_PACKAGE_NAME = "com.google.android.youtube";

    protected Context mContext;
    protected Intent mIntent;
    protected ActionBar mActionBar;
    protected MainController mMainController;
    protected INotificationModule mNotificationModule;
    protected AbstractUserDataModuleObservable mUserDataModule;
    protected IGpsLocalizationModule mGpsLocalizationModule;
    protected AbstractVideosModuleObservable mVideosModule;
    protected IEventsTrackingModule mEventTrackingModule;
    protected IActivityTrackerModule mActivityTrackerModule;
    protected AbstractCityModuleObservable mCityModuleObservable;

    // Special variables for GpsLocalizationModule
    private boolean isRegisteredForLocalizationService;
    private boolean mResolvingError;
    //    Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        mIntent = getIntent();

        mActionBar = getSupportActionBar();

        // Get the data
        mMainController = MainController.getCurrentInstance(mContext);
        mNotificationModule = mMainController.getNotificationModule();
        mUserDataModule = mMainController.getUserDataModule();
        mGpsLocalizationModule = mMainController.getGpsLocalizationModule();
        mVideosModule = mMainController.getVideosModule();
        mEventTrackingModule = mMainController.getEventTrackingModule();
        mActivityTrackerModule = mMainController.getActivityTrackerModule();

        // Getting the resolution error saved for localization service
        mResolvingError =
                savedInstanceState != null &&
                        savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        mActivityTrackerModule.notifyActivityCreated();
        if (mActivityTrackerModule.isThereOnlyOneActivityRunning()) {
            mEventTrackingModule.trackAppInitialization();
        }
    }

    protected void registerForLocalizationService() {
        isRegisteredForLocalizationService = true;
        mGpsLocalizationModule.registerForLocalizationService(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isRegisteredForLocalizationService && !mResolvingError) {
            mGpsLocalizationModule.connectWithLocalizationService();
        }
    }

    @Override
    protected void onStop() {
        if (isRegisteredForLocalizationService) {
            mGpsLocalizationModule.disconnectWithLocalizationService();
        }
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again
                mGpsLocalizationModule.connectWithLocalizationService();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialogForGooglePlayServices(result.getErrorCode());
            mResolvingError = true;
        }
    }

    /**
     * Creating the error dialog for Google Play Service based on the error code
     * @param errorCode
     *      The errorCode created by Connection result
     */
    private void showErrorDialogForGooglePlayServices(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    /**
     * Method called from errorDialogFragment when the dialog is dismissed
     */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /**
     * A fragment to display an error dialog
     */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {}

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int erroCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(erroCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((AbstractBaseActivity)getActivity()).onDialogDismissed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RESOLVE_ERROR:
                mResolvingError = false;
                if (resultCode == RESULT_OK) {
                    // Make sure the app is not already connected or attempting to connect
                    mGpsLocalizationModule.connectWithLocalizationService();
                }
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        // This is not absolute necessary because once the localization service has
        // been registered, it could be used for other activities.
//        if (isRegisteredForLocalizationService) {
//            mGpsLocalizationModule.unregisterForLocalizationService();
//        }

        mActivityTrackerModule.notifyActivityDestroyed();
        // If there is not activity running, then finalize
        if (!mActivityTrackerModule.isThereAnyActivityRunning()) {
            mEventTrackingModule.trackAppFinalization();
        }

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    /**
     * Method used to check if the user has logged in or not.
     * if not, it will show the alert dialog ask the user to log in
     *
     * @return
     *      True if the user has logged in
     *      False if the user has not logged in
     */
    protected boolean showAlertIfUserHasNotLoggedIn() {
        return showAlertIfUserHasNotLoggedIn(
                getResources().getString(R.string.abstract_base_activity_user_must_logged_in));
    }

    /**
     * Method used to check if the user has logged in or not.
     * if not, it will show the alert dialog ask the user to log in
     *
     * @param message
     *      The customized message to be shown to the user
     *
     * @return
     *      True if the user has logged in
     *      False if the user has not logged in
     */
    public boolean showAlertIfUserHasNotLoggedIn(String message) {
        boolean hasUserLoggedIn = mUserDataModule.hasUserData();

        // Show alert dialog if the user has not logged in
        if (!hasUserLoggedIn) {
            mNotificationModule.showAlertDialog(
                    mContext,
                    getString(R.string.notification_module_dialog_user_not_logged_in_title),
                    message,
                    getString(R.string.notification_module_dialog_yes),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.v(TAG, "Positive button clicked. Showing login screen to the user");
                            Intent startSignUpLoginActivityIntent = new Intent(mContext, SignUpLoginActivity.class);
                            startActivity(startSignUpLoginActivityIntent);
                        }
                    },
                    getString(R.string.notification_module_dialog_no),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.v(TAG, "Nagative button clicked. Dismissing this alert");
                        }
                    }
                );
        }
        return hasUserLoggedIn;
    }

    /**
     * Method used to launch YouTube app from this app. It checks if the user has the app installed first
     *
     * @return
     *      True if the user has YouTube installed
     *      False otherwise
     */
    protected boolean launchYouTubeApp() {
        // Launcheds YouTube app
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(YOU_TUBE_PACKAGE_NAME);
        if (intent == null) {
            // The user has not YouTube Installed
            return false;
        }
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        mContext.startActivity(intent);
        return true;
    }

    @Override
    public INotificationModule getNotificationModule() {
        return mNotificationModule;
    }

    @Override
    public AbstractUserDataModuleObservable getUserDataModule() {
        return mUserDataModule;
    }

    @Override
    public IGpsLocalizationModule getGpsLocalizationModule() {
        return mGpsLocalizationModule;
    }

    @Override
    public AbstractVideosModuleObservable getVideosModule() {
        return mVideosModule;
    }

    @Override
    public IEventsTrackingModule getEventTrackingModule() {
        return mEventTrackingModule;
    }

    @Override
    public IActivityTrackerModule getActivityTrackerModule() {
        return mActivityTrackerModule;
    }

    @Override
    public AbstractCityModuleObservable getCityModuleObservable() {
        return mCityModuleObservable;
    }

    @Override
    public ActionBar getActionBarFromActivity() {
        return mActionBar;
    };

}
