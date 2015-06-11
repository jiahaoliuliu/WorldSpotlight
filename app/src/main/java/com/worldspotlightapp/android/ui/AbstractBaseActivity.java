
package com.worldspotlightapp.android.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.worldspotlightapp.android.maincontroller.MainController;
import com.worldspotlightapp.android.maincontroller.modules.gpslocalizationmodule.IGpsLocalizationModule;
import com.worldspotlightapp.android.maincontroller.modules.notificationmodule.INotificationModule;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.AbstractUserDataModuleObservable;

/**
 * Abstract base class created to be extended by all the activities.
 * It contain the follow features
 * - Connection with MainController and all its modules
 * - Check for Google Play services
 */
public abstract class AbstractBaseActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "BaseActivity";

    protected Context mContext;
    protected ActionBar mActionBar;
    protected MainController mMainController;
    protected INotificationModule mNotificationModule;
    protected AbstractUserDataModuleObservable mUserDataModule;
    protected IGpsLocalizationModule mGpsLocalizationModule;

    // Special variables for GpsLocalizationModule
    private boolean isRegisteredForLocalizationService;
    private boolean mResolvingError;
    //    Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 99999;
    //    Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Lock the screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);

        mContext = this;

        mActionBar = getSupportActionBar();

        // Get the data
        mMainController = MainController.getCurrentInstance(mContext);
        mNotificationModule = mMainController.getNotificationModule();
        mUserDataModule = mMainController.getUserDataModule();
        mGpsLocalizationModule = mMainController.getGpsLocalizationModule();

        // Getting the resolution error saved for localization service
        mResolvingError =
                savedInstanceState != null &&
                        savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
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
        if (requestCode == REQUEST_RESOLVE_ERROR) {
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

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }
}
