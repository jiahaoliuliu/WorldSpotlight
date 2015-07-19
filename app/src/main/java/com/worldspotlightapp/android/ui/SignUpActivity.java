package com.worldspotlightapp.android.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.worldspotlightapp.android.R;

import java.util.Observable;

public class SignUpActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "SignUpActivity";

    private static final int MINIMUM_PASSWORD_LENGTH = 6;

    // Views
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private EditText mVerifyPasswordEditText;
    private Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

        // Link the views
        mUserNameEditText = (EditText) findViewById(R.id.user_name_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);
        mVerifyPasswordEditText = (EditText) findViewById(R.id.verify_password_edit_text);

        mSignUpButton = (Button) findViewById(R.id.sign_up_button);
        mSignUpButton.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sign_up_button:
                    signUp();
                    break;
            }
        }
    };

    /**
     * Try to sign up with the data introduced by the user
     *
     */
    private void signUp() {
        if (!areAllFieldsOk()) {
            return;
        }
    }

    /**
     * Check if all the fields are ok.
     * - The user name cannot be empty
     * - The user name must be an email address
     * - The password must has more than MINIMUM_PASSWORD_LENGTH characters
     * - The password should match with the password in verify field
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
        } else if (password.length() < MINIMUM_PASSWORD_LENGTH) {
            mPasswordEditText.setError(getString(R.string.error_message_string_too_short, MINIMUM_PASSWORD_LENGTH));
            areAllFieldsOk = false;
        }

        // Verify Password
        String verifyPassword = mVerifyPasswordEditText.getText().toString();
        //      It cannot be empty
        if (TextUtils.isEmpty(verifyPassword)) {
            mVerifyPasswordEditText.setError(getString(R.string.error_message_field_empty));
            areAllFieldsOk = false;
            //      It must match with the content in password field
        } else if (!verifyPassword.equals(password)) {
            mVerifyPasswordEditText.setError(getString(R.string.sign_up_activity_error_message_password_not_match));
            areAllFieldsOk = false;
        }

        return areAllFieldsOk;
    }

    @Override
    protected void processDataIfExists() {
        // TODO: implement this
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO: Implement this
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
