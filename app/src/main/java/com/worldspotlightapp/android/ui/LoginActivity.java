package com.worldspotlightapp.android.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.UserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleUserResponse;

import java.util.Observable;

public class LoginActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "LoginActivity";

    // Views
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;

    // The response from parse
    private ParseResponse mParseReseponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Lock the screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

        // Link the views
        mUserNameEditText = (EditText) findViewById(R.id.user_name_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);

        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.login_button:
                    login();
                    break;
            }
        }
    };

    /**
     * Try to login with the data introduced by the user
     */
    private void login() {
        if (!areAllFieldsOk()) {
            return;
        }

        // All the fields are ok, continue with sign up
        String username = mUserNameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        // Sign up
        mNotificationModule.showLoadingDialog(mContext);
        mUserDataModule.loginWithParse(this, username, password);
    }

    /**
     * Check if all the fields are ok.
     * - The user name cannot be empty
     * - The user name must be an email address
     * - The password must has more than MINIMUM_PASSWORD_LENGTH characters
     * @return
     */
    private boolean areAllFieldsOk() {

        // To save the results of the checkings
        boolean areAllFieldsOk = true;

        // Username
        String username = mUserNameEditText.getText().toString();
        //      It cannot be empty
        if (TextUtils.isEmpty(username)) {
            mUserNameEditText.setError(getString(R.string.error_message_field_empty));
            areAllFieldsOk = false;
            //      It must be an email address
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            mUserNameEditText.setError(getString(R.string.error_message_email_address_required));
            areAllFieldsOk = false;
        } else {
            mUserNameEditText.setError(null);
        }

        // Password
        String password = mPasswordEditText.getText().toString();
        //      It cannot be empty
        if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError(getString(R.string.error_message_field_empty));
            areAllFieldsOk = false;
            //      It must have at least MINIMUM_PASSWORD_LENGTH characters
        } else if (password.length() < SignUpActivity.MINIMUM_PASSWORD_LENGTH) {
            mPasswordEditText.setError(getString(R.string.error_message_string_too_short, SignUpActivity.MINIMUM_PASSWORD_LENGTH));
            areAllFieldsOk = false;
        } else {
            mPasswordEditText.setError(null);
        }

        return areAllFieldsOk;
    }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.v(TAG, "home button pressed");
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
