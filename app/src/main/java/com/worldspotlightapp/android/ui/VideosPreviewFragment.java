
package com.worldspotlightapp.android.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.Video;

public class VideosPreviewFragment extends Fragment {

    private Activity mActivity;
    private Picasso mPicasso;

    /**
     * The unique id of the video
     */
    private String mObjectId;
    private String mThumbnailUrl;
    private String mTitle;
    private String mDescription;
    private String mNavigation;

    // Views
    private RelativeLayout mVideoPreviewRelativeLayout;
    private ImageView mThumbnailImageView;
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;

    public static VideosPreviewFragment newInstance(String objectId, String thumbnailUrl, String title, String description) {
        VideosPreviewFragment videosPreviewFragment = new VideosPreviewFragment();
        Bundle args = new Bundle();
        args.putString(Video.INTENT_KEY_OBJECT_ID, objectId);
        args.putString(Video.INTENT_KEY_THUMBNAIL_URL, thumbnailUrl);
        args.putString(Video.INTENT_KEY_TITLE, title);
        args.putString(Video.INTENT_KEY_DESCRIPTION, description);
        videosPreviewFragment.setArguments(args);
        return videosPreviewFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Set the content of the view
        mVideoPreviewRelativeLayout = (RelativeLayout)inflater.inflate(R.layout.video_preview_layout, container,
                false);
        // Link the layout
        mThumbnailImageView = (ImageView) mVideoPreviewRelativeLayout.findViewById(R.id.thumbnail_image_view);
        mTitleTextView = (TextView) mVideoPreviewRelativeLayout.findViewById(R.id.title_text_view);
        mDescriptionTextView = (TextView) mVideoPreviewRelativeLayout.findViewById(R.id.description_text_view);
        return mVideoPreviewRelativeLayout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Get the offer Id from the arguments
        Bundle bundle = getArguments();
        if (bundle == null
                || !bundle.containsKey(Video.INTENT_KEY_OBJECT_ID)
                || !bundle.containsKey(Video.INTENT_KEY_THUMBNAIL_URL)
                || !bundle.containsKey(Video.INTENT_KEY_TITLE)
                || !bundle.containsKey(Video.INTENT_KEY_DESCRIPTION)

                ) {
            throw new IllegalArgumentException("You must instantiate this fragment using the method newInstance");
        }

        mObjectId = bundle.getString(Video.INTENT_KEY_OBJECT_ID);

        // Load images
        mPicasso = Picasso.with(mActivity);
        mPicasso.load(mThumbnailUrl);
        mThumbnailUrl = bundle.getString(Video.INTENT_KEY_THUMBNAIL_URL);

        // Title
        mTitle = bundle.getString(Video.INTENT_KEY_TITLE);
        mTitleTextView.setText(mTitle);

        mDescription = bundle.getString(Video.INTENT_KEY_DESCRIPTION);
        mDescriptionTextView.setText(mDescription);

        mVideoPreviewRelativeLayout.setOnClickListener(onClickListener);
    }

    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.video_preview_relative_layout:
                    // Start the video details activity
                    Intent startVideoDetailsActivityIntent = new Intent(mActivity, VideoDetailsActivity.class);
                    startVideoDetailsActivityIntent.putExtra(Video.INTENT_KEY_OBJECT_ID, mObjectId);
                    startActivity(startVideoDetailsActivityIntent);
                    break;
            }
        }
    };
}
