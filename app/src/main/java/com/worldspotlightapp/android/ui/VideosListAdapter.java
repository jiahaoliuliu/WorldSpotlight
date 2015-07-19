package com.worldspotlightapp.android.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.Video;

import java.util.List;

/**
 * The adapter for the videos list
 */
public class VideosListAdapter extends RecyclerView.Adapter<VideosListAdapter.ViewHolder> {

    private static final String TAG = "VideosListAdapter";

    private Context mContext;
    private List<Video> mVideosList;

    private Picasso mPicasso;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this class
        public ImageView mVideoPreviewImageView;
        public ImageView mVideoPreviewErrorImageView;
        public TextView mVideoTitleTextView;
        public ViewHolder(View view) {
            super(view);
            mVideoPreviewImageView = (ImageView)view.findViewById(R.id.video_preview_image_view);
            mVideoPreviewErrorImageView = (ImageView)view.findViewById(R.id.video_preview_error_image_view);
            mVideoTitleTextView = (TextView)view.findViewById(R.id.video_title_text_view);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public VideosListAdapter(Context context, List<Video> videosList) {
        mContext = context;
        mVideosList = videosList;

        mPicasso = Picasso.with(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view =
                LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.video_list_element_layout, viewGroup, false);

        // Set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Video video = mVideosList.get(position);

        // Set the elements
        mPicasso.load(video.getVideoListThumbnailUrl()).into(holder.mVideoPreviewImageView, new Callback() {
            @Override
            public void onSuccess() {
                Log.v(TAG, "Correctly loading the image");
                holder.mVideoPreviewImageView.setVisibility(View.VISIBLE);
                holder.mVideoPreviewErrorImageView.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                Log.e(TAG, "Error loading the image with the url " + video.getVideoListThumbnailUrl());
                holder.mVideoPreviewImageView.setVisibility(View.GONE);
                holder.mVideoPreviewErrorImageView.setVisibility(View.VISIBLE);
            }
        });
        holder.mVideoTitleTextView.setText(video.getTitle());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mVideosList.size();
    }

}
