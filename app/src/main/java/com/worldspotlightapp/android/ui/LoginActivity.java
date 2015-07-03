package com.worldspotlightapp.android.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.parse.ParseFacebookUtils;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.UserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleResponse;

import java.util.Observable;

public class LoginActivity extends AbstractBaseActivityObserver implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginActivity";

    /**
     * Request code used to invoke sing in user interactions.
     */
    private static final int RC_SIGN_IN = 0;

    /**
     * Client used to interact with Google APIs.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us from starting further
     * intents
     */
    private boolean mIntentInProgress;

    // Views
    private ImageView mCancelImageView;
    private Button mFacebookLoginButton;
    private SignInButton mGooglePlusSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if the user has logged in
        if (mUserDataModule.hasUserData()) {
            Log.v(TAG, "The user has already logged in");
            goToMainActivity();
        }

        // Initialize Google API Clients
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        // Link the views
        mCancelImageView = (ImageView)findViewById(R.id.cancel_image_view);
        mCancelImageView.setOnClickListener(onClickListener);

        mFacebookLoginButton = (Button)findViewById(R.id.facebook_login_button);
        mFacebookLoginButton.setOnClickListener(onClickListener);

        mGooglePlusSignInButton = (SignInButton)findViewById(R.id.google_plus_login_buttn);
        mGooglePlusSignInButton.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.cancel_image_view:
                    goToMainActivity();
                    break;
                case R.id.facebook_login_button:
                    mNotificationModule.showLoadingDialog(mContext);
                    mUserDataModule.loginWithFacebook(LoginActivity.this, LoginActivity.this);
                    break;
                case R.id.google_plus_login_buttn:
                    mNotificationModule.showLoadingDialog(mContext);
                    mGoogleApiClient.connect();
                    break;
            }
        }
    };

    /**
     * Starts the main activity and finish the actual one
     */
    private void goToMainActivity() {
        Intent startMainActivityIntent = new Intent(mContext, MainActivity.class);
        startActivity(startMainActivityIntent);
        finish();
    }

    @Override
    protected void processDataIfExists() {
        // Check if the user has logged in
        if (mUserDataModule.hasUserData()) {
            Log.v(TAG, "The user has already logged in");
            mNotificationModule.dismissLoadingDialog();
            goToMainActivity();
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.v(TAG, "Data received from " + observable + ", Object:" + o);
        if (observable instanceof UserDataModuleObservable) {
            if (o instanceof UserDataModuleResponse) {

                // There is not need to store the data

                if (mIsInForeground) {
                    processDataIfExists();
                }

                observable.deleteObserver(this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Log.v(TAG, "The user is asked to sign in. Result returned with result code " + resultCode
                    + ", and with data " + data);
            mIntentInProgress = false;

            switch (resultCode) {
                case RESULT_CANCELED:
                    Log.v(TAG, "The Google Plus Sign in has been canceled. Dismissing the loading dialog");
                    mNotificationModule.dismissLoadingDialog();
                    break;
                default:
                    if (!mGoogleApiClient.isConnecting()) {
                        mGoogleApiClient.connect();
                    }
                    break;
            }
        }
    }

    //------------------------------ Google Plus ----------------------/


    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // We've resolved any connection errors. mGoogle ApiClient can be used to
        // access Google APIs on behalf of the user
        Log.v(TAG, "Google Plus connected");
        mNotificationModule.dismissLoadingDialog();
        // TODO: Get user data
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "Google Plus suspended. Trying to connect again.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "Google Plus connection failed. The result is " + result);
        if (!mIntentInProgress && result.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(result.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent. Return to the default
                // state and attempt to connect to get an updated ConnectionResult
                Log.e(TAG, "Error start intent sender for result. Trying to connect again ", e);
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }
}
