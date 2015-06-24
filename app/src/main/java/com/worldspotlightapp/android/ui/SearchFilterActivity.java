package com.worldspotlightapp.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.worldspotlightapp.android.R;

import java.util.Observable;

public class SearchFilterActivity extends AbstractBaseActivity {

    private static final String TAG = "SearchFilterActivity";

    public static final String INTENT_KEY_KEYWORD = "com.worldspotlightapp.android.ui.SearchFilterActivity.keyword";

    // View
    private EditText mKeywordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filter);

        // Link the views
        mKeywordEditText = (EditText) findViewById(R.id.keyword_edit_text);
        mKeywordEditText.setOnEditorActionListener(new OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String keyword = mKeywordEditText.getText().toString();
                    if (TextUtils.isEmpty(keyword)) {
                        mNotificationModule.showToast(R.string.error_keyword_empty, true);
                        return true;
                    }

                    Log.v(TAG, "Done button pressed. The content of the editText is " + mKeywordEditText.getText().toString());
                    // Send the keyword to the MainActivity
                    Intent sendKeywordToMainActivityIntent = new Intent(mContext, MainActivity.class);
                    sendKeywordToMainActivityIntent.putExtra(INTENT_KEY_KEYWORD, mKeywordEditText.getText());
                    startActivity(sendKeywordToMainActivityIntent);
                    return true;
                }
                return false;
            }
        });
    }
}
