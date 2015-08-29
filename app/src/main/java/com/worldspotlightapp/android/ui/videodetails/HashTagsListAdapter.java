package com.worldspotlightapp.android.ui.videodetails;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.HashTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiahaoliuliu on 15/8/29.
 */
public class HashTagsListAdapter extends RecyclerView.Adapter<HashTagsListAdapter.ViewHolder> {

    private static final String TAG = "HashTagsListAdapter";

    private List<HashTag> mHashTagsList;

    // The list of the id of the selected tags
    private ArrayList<String> mSelectedHashTagsList;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this class
        public CheckBox mHashTagCheckBox;
        public ViewHolder(View view) {
            super(view);
            mHashTagCheckBox = (CheckBox)view.findViewById(R.id.hash_tag_check_box);
        }
    }

    //Simple constructor
    public HashTagsListAdapter(List<HashTag> hashTagsList) {
        mHashTagsList = hashTagsList;
        mSelectedHashTagsList = new ArrayList<String>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // Create a new view
        View view =
                LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.hash_tags_list_item_layout, viewGroup, false);

        // Set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mHashTagCheckBox.setText(mHashTagsList.get(position).getName());
        holder.mHashTagCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HashTag hashTag = mHashTagsList.get(position);
                Log.v(TAG, "The hashtag clicked is " + hashTag);
                String hashTagId = hashTag.getObjectId();
                // If the checkbox has been checked
                if (isChecked) {
                    // Only if it was not contained before
                    if (!mSelectedHashTagsList.contains(hashTagId)) {
                        mSelectedHashTagsList.add(hashTagId);
                    }
                // If the checkbox has been unchecked
                } else {
                    // Only if it was contained before
                    if (mSelectedHashTagsList.contains(hashTagId)) {
                        mSelectedHashTagsList.remove(hashTagId);
                    }
                }

                Log.d(TAG, "The content of the selected items is " + mSelectedHashTagsList);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mHashTagsList.size();
    }

    public ArrayList<String> getSelectedHashTagsList() {
        return mSelectedHashTagsList;
    }

}