
package com.worldspotlightapp.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.ScreenId;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.EventId;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.OnEventTrackingModuleRequestedListener;
import com.worldspotlightapp.android.model.Video;

import java.util.ArrayList;

public class VideosPreviewFragment extends Fragment {

    private static final String INTENT_KEY_SHOW_ARROWS = "com.worldspotlightapp.android.ui.VideosPreviewFragment.showArrows";

    public interface IOnVideosPreviewFragmentClickedListener {

        /**
         * Method invoked when the user clicks on the video preview fragment.
         *
         * @param objectId
         *      The object id of the video which the data is displaying in the
         *      video preview fragment
         */
        void onClickOnVideoPreviewFragment(String objectId);
    }

    private Activity mActivity;
    private Picasso mPicasso;

    /**
     * The unique id of the video
     */
    private String mObjectId;
    private String mThumbnailUrl;
    private String mTitle;
    private String mDescription;
    private boolean mShowArrows;

    // Views
    private RelativeLayout mVideoPreviewRelativeLayout;
    private ImageView mThumbnailImageView;
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;
    private ImageView mLeftArrowImageView;
    private ImageView mRightArrowImageView;

    // Others
    private OnEventTrackingModuleRequestedListener mOnEventTrackingModuleRequestedListener;
    private IEventsTrackingModule mEventTrackingModule;

    private IOnVideosPreviewFragmentClickedListener mOnVideosPreviewFragmentClickedListener;

    public static VideosPreviewFragment newInstance(
            String objectId, String thumbnailUrl, String title, String description, boolean showArrows) {
        VideosPreviewFragment videosPreviewFragment = new VideosPreviewFragment();
        Bundle args = new Bundle();
        args.putString(Video.INTENT_KEY_OBJECT_ID, objectId);
        args.putString(Video.INTENT_KEY_THUMBNAIL_URL, thumbnailUrl);
        args.putString(Video.INTENT_KEY_TITLE, title);
        args.putString(Video.INTENT_KEY_DESCRIPTION, description);
        args.putBoolean(INTENT_KEY_SHOW_ARROWS, showArrows);
        videosPreviewFragment.setArguments(args);
        return videosPreviewFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;

        try {
            mOnEventTrackingModuleRequestedListener = (OnEventTrackingModuleRequestedListener)activity;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(activity.toString() + " must implements the mOnEventTrackingModuleRequestedListener interface");
        }

        try {
            mOnVideosPreviewFragmentClickedListener = (IOnVideosPreviewFragmentClickedListener)activity;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(activity.toString() + " must implements the IOnVideosPreviewFragmentClickedListener interface");
        }

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
        mLeftArrowImageView = (ImageView) mVideoPreviewRelativeLayout.findViewById(R.id.left_arrow_image_view);
        mRightArrowImageView = (ImageView) mVideoPreviewRelativeLayout.findViewById(R.id.right_arrow_image_view);

        return mVideoPreviewRelativeLayout;
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
                || !bundle.containsKey(INTENT_KEY_SHOW_ARROWS)
                ) {
            throw new IllegalArgumentException("You must instantiate this fragment using the method newInstance");
        }

        mObjectId = bundle.getString(Video.INTENT_KEY_OBJECT_ID);

        // Get the Event module
        mEventTrackingModule = mOnEventTrackingModuleRequestedListener.getEventsTrackingModule();

        // Load images
        mThumbnailUrl = bundle.getString(Video.INTENT_KEY_THUMBNAIL_URL);
        mPicasso = Picasso.with(mActivity);
        mPicasso.load(mThumbnailUrl).into(mThumbnailImageView);

        // Title
        mTitle = bundle.getString(Video.INTENT_KEY_TITLE);
        mTitleTextView.setText(mTitle);

        // Description
        mDescription = bundle.getString(Video.INTENT_KEY_DESCRIPTION);
        mDescriptionTextView.setText(mDescription);

        // Arrow
        mShowArrows = bundle.getBoolean(INTENT_KEY_SHOW_ARROWS);
        if (mShowArrows) {
            mLeftArrowImageView.setVisibility(View.VISIBLE);
            mRightArrowImageView.setVisibility(View.VISIBLE);
        } else {
            mLeftArrowImageView.setVisibility(View.GONE);
            mRightArrowImageView.setVisibility(View.GONE);
        }

        mVideoPreviewRelativeLayout.setOnClickListener(onClickListener);
    }

    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.video_preview_relative_layout:
                    mOnVideosPreviewFragmentClickedListener.onClickOnVideoPreviewFragment(mObjectId);
                    break;
            }
        }
    };
}
