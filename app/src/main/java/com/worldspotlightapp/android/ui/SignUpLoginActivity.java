package com.worldspotlightapp.android.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.parse.ParseFacebookUtils;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.ScreenId;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.EventId;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.UserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleUserResponse;

import java.util.Observable;

public class SignUpLoginActivity extends AbstractBaseActivityObserver implements
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
    private Button mFacebookLoginButton;
    private Button mGooglePlusSignInButton;
    private Button mSignUpButton;
    private Button mLoginButton;
    private Button mSkipButton;

    // The response from parse
    private ParseResponse mParseReseponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_login);

        // Check if the user has logged in
        if (mUserDataModule.hasUserData()) {
            Log.v(TAG, "The user has already logged in");
            finish();
        }

        // Initialize Google API Clients
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        // Link the views
        mFacebookLoginButton = (Button)findViewById(R.id.facebook_login_button);
        mFacebookLoginButton.setOnClickListener(onClickListener);

        mGooglePlusSignInButton = (Button)findViewById(R.id.google_plus_login_button);
        mGooglePlusSignInButton.setOnClickListener(onClickListener);

        mSignUpButton = (Button)findViewById(R.id.sign_up_button);
        mSignUpButton.setOnClickListener(onClickListener);

        mLoginButton = (Button)findViewById(R.id.log_in_button);
        mLoginButton.setOnClickListener(onClickListener);

        mSkipButton = (Button)findViewById(R.id.skip_login_button);
        mSkipButton.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.facebook_login_button:
                    mEventTrackingModule.trackUserAction(ScreenId.LOGIN_SCREEN, EventId.LOGIN_WITH_FACEBOOK);
                    mNotificationModule.showLoadingDialog(mContext);
                    mUserDataModule.loginWithFacebook(SignUpLoginActivity.this, SignUpLoginActivity.this);
                    break;
                case R.id.google_plus_login_button:
                    mEventTrackingModule.trackUserAction(ScreenId.LOGIN_SCREEN, EventId.LOGIN_WITH_GOOGLE_PLUS);
                    mNotificationModule.showLoadingDialog(mContext);
                    mGoogleApiClient.connect();
                    break;
                case R.id.sign_up_button:
                    // Launch the Sign up activity
                    Intent startSignUpActivityIntent = new Intent(mContext, SignUpActivity.class);
                    startActivity(startSignUpActivityIntent);
                    break;
                case R.id.log_in_button:
                    // Launch the login activity
                    Intent startLoginActivityIntent = new Intent(mContext, LoginActivity.class);
                    startActivity(startLoginActivityIntent);
                    break;
                case R.id.skip_login_button:
                    mEventTrackingModule.trackUserAction(ScreenId.LOGIN_SCREEN, EventId.SKIP_LOGIN);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void processDataIfExists() {
        // Check if the user has logged in
        if (mUserDataModule.hasUserData()) {
            Log.v(TAG, "The user has already logged in");
            mNotificationModule.dismissLoadingDialog();
            finish();
        } else if (mParseReseponse != null) {
            Log.v(TAG, "Error on login/signu " + mParseReseponse);
            mNotificationModule.showToast(mParseReseponse.getHumanRedableResponseMessage(mContext), true);

            // Remove parse response
            mParseReseponse = null;

            mNotificationModule.dismissLoadingDialog();
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.v(TAG, "Data received from " + observable + ", Object:" + o);
        if (observable instanceof UserDataModuleObservable) {
            if (o instanceof UserDataModuleUserResponse) {

                // Get parse response, which could be error
                UserDataModuleUserResponse userDataModuleUserResponse = (UserDataModuleUserResponse)o;
                mParseReseponse = userDataModuleUserResponse.getParseResponse();

                if (isInForeground()) {
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
    public void onConnected(Bundle bundle) {
        // We've resolved any connection errors. mGoogle ApiClient can be used to
        // access Google APIs on behalf of the user
        Log.v(TAG, "Google Plus connected");
        // Get user data
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            String personGooglePlusProfile = currentPerson.getUrl();
            String personPhotoUrl = currentPerson.getImage().getUrl();

            mNotificationModule.showLoadingDialog(mContext);
            mUserDataModule.signupForGooglePlusUsers(this, personName, email, personPhotoUrl, personGooglePlusProfile);
        }
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
