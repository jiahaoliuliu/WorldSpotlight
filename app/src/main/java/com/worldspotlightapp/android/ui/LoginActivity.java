package com.worldspotlightapp.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.parse.ParseFacebookUtils;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.UserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.VideosModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideosListResponse;

import java.util.Observable;
import java.util.Stack;

public class LoginActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "LoginActivity";

    // Views
    private Button mFacebookLoginButton;
    private ImageView mCancelImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if the user has logged in
        if (mUserDataModule.hasUserData()) {
            Log.v(TAG, "The user has already logged in");
            goToMainActivity();
        }

        // Link the views
        mCancelImageView = (ImageView)findViewById(R.id.cancel_image_view);
        mCancelImageView.setOnClickListener(onClickListener);

        mFacebookLoginButton = (Button)findViewById(R.id.facebook_login_button);
        mFacebookLoginButton.setOnClickListener(onClickListener);
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
    }
}
