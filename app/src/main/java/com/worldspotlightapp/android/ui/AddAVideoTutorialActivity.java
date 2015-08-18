package com.worldspotlightapp.android.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.ScreenId;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.EventId;

public class AddAVideoTutorialActivity extends AbstractBaseActivity {

    private static final String TAG = "AddAVideoTActivity";

    // Views
    private CheckBox mDisableMeCheckBox;
    private Button mLaunchYoutubeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_video_tutorial);

        // Set the action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

        // Link the views
        mDisableMeCheckBox = (CheckBox) findViewById(R.id.disable_me_check_box);
        mDisableMeCheckBox.setOnClickListener(onClickListener);

        mLaunchYoutubeButton = (Button) findViewById(R.id.launch_youtube_button);
        mLaunchYoutubeButton.setOnClickListener(onClickListener);

        // Update the views
        mDisableMeCheckBox.setChecked(mUserDataModule.shouldTheAppNotShowAddAVideoTutorial());
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.disable_me_check_box:
                    Log.v(TAG, "Is checkbox enabled? " + mDisableMeCheckBox.isChecked());
                    mUserDataModule.hideAddAVideoTutorial(mDisableMeCheckBox.isChecked());
                    break;
                case R.id.launch_youtube_button:
                    mEventTrackingModule.trackUserAction(ScreenId.ADD_A_VIDEO_TUTORIAL_SCREEN, EventId.OPEN_YOUTUBE);
                    // Try to launch youtube app
                    if(!launchYouTubeApp()) {
                        mNotificationModule.showToast(R.string.error_message_not_possible_launching_you_tube_app, true);
                    }
                    break;
            }
        }
    };

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
