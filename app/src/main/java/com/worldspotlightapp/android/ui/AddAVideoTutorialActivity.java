package com.worldspotlightapp.android.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.worldspotlightapp.android.R;

public class AddAVideoTutorialActivity extends AbstractBaseActivity {

    private static final String TAG = "AddAVideoTActivity";

    private CheckBox disableMeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_video_tutorial);

        // Link the views
        disableMeCheckBox = (CheckBox) findViewById(R.id.disable_me_check_box);
        disableMeCheckBox.setChecked(mUserDataModule.shouldTheAppNotShowAddAVideoTutorial());

        disableMeCheckBox.setOnClickListener(onClickListener);

    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.disable_me_check_box:
                    Log.v(TAG, "Is checkbox enabled? " + disableMeCheckBox.isChecked());
                    mUserDataModule.hideAddAVideoTutorial(disableMeCheckBox.isChecked());
                    break;
            }
        }
    };
}
