package com.worldspotlightapp.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.worldspotlightapp.android.R;

public class LoginActivity extends AbstractBaseActivity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if the user has logged in
        if (mUserDataModule.hasUserData()) {
            Log.v(TAG, "The user has already logged in");
            goToMainActivity();
        }
    }

    /**
     * Starts the main activity and finish the actual one
     */
    private void goToMainActivity() {
        Intent startMainActivityIntent = new Intent(mContext, MainActivity.class);
        startActivity(startMainActivityIntent);
        finish();
    }
}
