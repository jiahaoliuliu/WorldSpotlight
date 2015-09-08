package com.worldspotlightapp.android.ui.videodetails;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.Organizer;

import java.util.List;

/**
 *
 * Adapter for the Organizers recycler view in the Video details.
 *
 * Created by jiahaoliuliu on 15/8/29.
 */
public class OrganizersRecyclerViewAdapter extends RecyclerView.Adapter<OrganizersRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "OrganizersListAdapter";

    private Context mContext;
    private Picasso mPicasso;

    private List<Organizer> mOrganizersList;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this class
        public ImageView mLogoImageView;
        public TextView mNameTextView;
        public TextView mDescriptionTextView;

        public ViewHolder(View view) {
            super(view);
            mLogoImageView = (ImageView) view.findViewById(R.id.logo_image_view);
            mNameTextView = (TextView) view.findViewById(R.id.name_text_view);
            mDescriptionTextView = (TextView) view.findViewById(R.id.description_text_view);
        }
    }

    //Simple constructor
    public OrganizersRecyclerViewAdapter(Context context, List<Organizer> organizersList) {
        this.mContext = context;
        this.mPicasso = Picasso.with(context);
        this.mOrganizersList = organizersList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // Create a new view
        View view =
                LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.organizers_list_item_layout, viewGroup, false);

        // Set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Organizer organizer = mOrganizersList.get(position);

        //Logo
        if (organizer.hasLogoUrl()) {
            mPicasso.load(organizer.getLogoUrl()).into(holder.mLogoImageView);
        }

        // Name
        holder.mNameTextView.setText(organizer.getName());

        // Description
        holder.mDescriptionTextView.setText(organizer.getDescription());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mOrganizersList.size();
    }
}