
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

    private Activity activity;
    private Picasso picasso;

    /**
     * The unique id of the video
     */
    private int mObjectId;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Get the offer Id from the arguments
        Bundle bundle = getArguments();
        if (bundle == null
                || !bundle.containsKey(Video.INTENT_KEY_OBJECT_ID)
                || !bundle.containsKey(Video.INTENT_KEY_OBJECT_ID)
                || !bundle.containsKey(Video.INTENT_KEY_OBJECT_ID)
                || !bundle.containsKey(Video.INTENT_KEY_OBJECT_ID)

                ) {
            throw new IllegalArgumentException("You must instantiate this fragment using the method newInstance");
        }

        if (!bundle.containsKey(Video.OFFER_ID_KEY))
        offerId = bundle.getInt(ShowOfferDetailsActivity.OFFER_ID_KEY);

        // Get the navigation
        if (!bundle.containsKey(ShowOfferDetailsActivity.NAVIGATION_KEY)) {
            throw new IllegalArgumentException("The navigation key must be passed to the fragment");
        }
        mNavigation = bundle.getString(ShowOfferDetailsActivity.NAVIGATION_KEY);

        picasso = Picasso.with(activity);
        actionBar = onActionBarRequested.requestActionBar();
        session = onSessoinRequestListener.requestSession();
        // Get the details of the offer
        onProgressBarShowListener.showProgressBar(true);
        session.getOfferDetails(offerId, new RequestOfferDatailsCallback() {

            @Override
            public void done(RequestStatus requestStatus, final OfferDetails offerDetailsReceived,
                    String errorMessage) {
                if (!ErrorHandler.isError(requestStatus)) {
                    offerDetails = offerDetailsReceived;

                    BuyitApplication.setBrandId(offerDetails.getBrandId());

                    // Set the data
                    title = offerDetails.getTitle();
                    updateUi();

                    picasso.load(offerDetails.getBrandLogoUrl()).into(brandLogoImageView,
                            new Callback() {

                                @Override
                                public void onSuccess() {
                                    try {
                                        // Get the bitmap
                                        Drawable drawable = brandLogoImageView.getDrawable();
                                        int height = drawable.getIntrinsicHeight();
                                        int width = drawable.getIntrinsicWidth();
                                        double ratio = (double) height / width;
                                        // Change image margin if the ratio is bigger than 0.5
                                        if (ratio > 0.5) {
                                            int brandLogoImageHeight = (int) getResources()
                                                    .getDimension(
                                                            R.dimen.offer_details_brand_logo_square_height);
                                            brandLogoImageView.getLayoutParams().height = brandLogoImageHeight;
                                            // Restore the default margin
                                        } else {
                                            int brandLogoImageHeight = (int) getResources()
                                                    .getDimension(
                                                            R.dimen.offer_details_brand_logo_height);
                                            brandLogoImageView.getLayoutParams().height = brandLogoImageHeight;
                                        }
                                        // Because it is a callback, it could be that when the
                                        // callback returns, the offer details is not
                                        // longer shown and illegalStateException will appears
                                    } catch (IllegalStateException illegalStateException) {
                                        Ln.e("Error loading brand logo image. When it has been finished the offer details is not longer on screen",
                                                illegalStateException);
                                    }
                                }

                                @Override
                                public void onError() {
                                    Ln.e("Error getting the image for the logo");
                                }
                            });

                    picasso.load(offerDetails.getOfferImageUrl()).into(offerImageView,
                            new Callback() {
                                @Override
                                public void onSuccess() {
                                    try {
                                        // Get the bitmap
                                        Drawable drawable = offerImageView.getDrawable();
                                        int height = drawable.getIntrinsicHeight();
                                        int width = drawable.getIntrinsicWidth();
                                        double ratio = (double) height / width;
                                        offerImageView.setHeightRatio(ratio);
                                    } catch (IllegalStateException illegalStateException) {
                                        Ln.e("Error loading offer image. When it has been finished the offer details is not longer on screen",
                                                illegalStateException);
                                    }
                                }

                                @Override
                                public void onError() {
                                    Ln.e("Error getting the image for the offer image");
                                }
                            });
                    // End date
                    Date endDate = offerDetails.getEndDate();
                    if (endDate == null) {
                        String endDateText = getResources().getString(
                                R.string.offer_details_end_date_default_title);
                        // Show the start date if exists
                        Date startDate = offerDetails.getStartDate();
                        if (startDate != null) {
                            String startedDaysAgoPartialText = getResources().getString(
                                    R.string.offer_details_started_x_days_ago);
                            String startedDaysAgoFinalText = String.format(
                                    startedDaysAgoPartialText, daysFromNow(startDate));
                            endDateText += "\n" + startedDaysAgoFinalText;
                        }

                        endDateTextView.setText(endDateText);
                    } else {
                        String textToShow = getString(R.string.offer_details_end_date);
                        String day = new SimpleDateFormat("d",
                                getResources().getConfiguration().locale).format(endDate);
                        String month = new SimpleDateFormat("MMMM", getResources()
                                .getConfiguration().locale).format(endDate);
                        String finalTextToShow = String.format(textToShow, day, month);
                        endDateTextView.setText(finalTextToShow);
                    }

                    shortDescriptionTextView.setText(offerDetails.getShortDescription());
                    // Get the type of the offer
                    switch (offerDetails.getOfferType()) {
                        case CATALOGO:
                            offerTypeTextView.setText(getResources().getString(
                                    R.string.offer_details_type_catalog));
                            break;
                        case TIENDA:
                            offerTypeTextView.setText(getResources().getString(
                                    R.string.offer_details_type_shop));
                            break;
                        case SOLO_TIENDA:
                            offerTypeTextView.setText(getResources().getString(
                                    R.string.offer_details_type_only_shop));
                            break;
                        case SOLO_ONLINE:
                            offerTypeTextView.setText(getResources().getString(
                                    R.string.offer_details_type_only_online));
                            break;
                        case TIENDA_ONLINE:
                            offerTypeTextView.setText(getResources().getString(
                                    R.string.offer_details_type_shop_online));
                            break;
                        // By default it uses coupons
                        default:
                        case CUPON:
                            offerTypeTextView.setText(getResources().getString(
                                    R.string.offer_details_type_coupon));
                            break;
                    }
                    completeDescriptionTextView.setText(offerDetails.getDescription());
                    /*
                     * // Check if the text has been ellipsized if
                     * (hasBeenEllipsized(completeDescriptionTextView)) {
                     * expandCompleteDescriptionTextView.setVisibility(View.VISIBLE); }
                     */

                    String locations = offerDetails.getLocations();
                    if (locations != null && !locations.equals("")) {
                        locationsTextView.setText(getResources().getString(
                                R.string.offer_details_localizations_title)
                                + offerDetails.getLocations());
                        locationsTextView.setVisibility(View.VISIBLE);
                    }
                    refreshFavoriteState();
                } else {
                    Toast.makeText(activity, errorMessage, ErrorHandler.ERROR_MESSAGE_DURATION)
                            .show();
                }
                onProgressBarShowListener.showProgressBar(false);
            }
        });

    }

    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.favoriteTextView:
                    BuyitApplication.tracker.trackEvent("Analytics",
                            "pulsado favoritos desde detalle de oferta", "Favorito: "
                                    + offerDetails.isFavorite(), (long) 0);
                    // Check if the offer has been marked as favorite or not
                    session.setOfferAsFavorite(offerDetails.getId(), !offerDetails.isFavorite(),
                            new RequestStringCallback() {

                                @Override
                                public void done(RequestStatus requestStatus, String stringObtained) {
                                    if (!ErrorHandler.isError(requestStatus)) {
                                        // Refresh the favorite state
                                        offerDetails.setFavorite(!offerDetails.isFavorite());
                                        offerDetails.setFavorite(Integer.valueOf(stringObtained));
                                        refreshFavoriteState();
                                        // Show the text
                                    } else {
                                        Toast.makeText(activity, stringObtained,
                                                ErrorHandler.ERROR_MESSAGE_DURATION).show();
                                    }
                                }
                            });

                    // Track this action
                    JSONObject propertiesFavorite = new JSONObject();
                    try {
                        propertiesFavorite.put(getString(R.string.property_brand),
                                offerDetails.getBrandId());
                        propertiesFavorite.put(getString(R.string.property_offer_id), offerId);
                        propertiesFavorite.put(getString(R.string.property_filter), session
                                .getOffersGender().toString());
                        propertiesFavorite.put(getString(R.string.property_origin),
                                getString(R.string.property_origin_details));

                        if (!offerDetails.isFavorite()) {
                            BuyitApplication.mixpanel.track(
                                    getString(R.string.favorite_offer_event), propertiesFavorite);
                        } else {
                            BuyitApplication.mixpanel
                                    .track(getString(R.string.no_favorite_offer_event),
                                            propertiesFavorite);
                        }
                    } catch (JSONException e) {
                        Ln.e("Error tracking the favorite", e);
                    }

                    break;
                case R.id.shareImageView:
                    BuyitApplication.tracker.trackEvent("Analytics",
                            "Compartir desde detalle de oferta", "compartir", (long) 0);
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getResources()
                            .getString(R.string.offer_details_share_subject));
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, getResources()
                            .getString(R.string.offer_details_share_body));
                    startActivity(Intent.createChooser(sharingIntent,
                            getResources().getString(R.string.offer_details_share_title)));

                    // Track this action
                    JSONObject propertiesShare = new JSONObject();
                    try {
                        propertiesShare.put(getString(R.string.property_brand),
                                offerDetails.getBrandId());
                        propertiesShare.put(getString(R.string.property_offer_id), offerId);
                        propertiesShare.put(getString(R.string.property_filter), session
                                .getOffersGender().toString());
                        propertiesShare.put(getString(R.string.property_origin),
                                getString(R.string.property_origin_details));

                        BuyitApplication.mixpanel.track(getString(R.string.share_offer),
                                propertiesShare);
                    } catch (JSONException e) {
                        Ln.e("Error tracking the Share", e);
                    }

                    break;
                case R.id.webImageView:
                    // If the offer details does not exists, do nothing
                    if (offerDetails == null) {
                        break;
                    }

                    // if the web url does not exists, do nothing
                    String webUrl = offerDetails.getUrl();
                    if (webUrl == null) {
                        break;
                    }

                    // Add http if it was not added before
                    if (!webUrl.startsWith("http://") || (!webUrl.startsWith("https://"))) {
                        webUrl = "http://" + webUrl;
                    }

                    BuyitApplication.tracker.trackEvent("Analytics",
                            "Oferta pulsada desde detalle de oferta", webUrl, (long) 0);
                    Intent openBrowserIntent = new Intent(Intent.ACTION_VIEW);
                    openBrowserIntent.setData(Uri.parse(webUrl));
                    startActivity(openBrowserIntent);

                    // Track this action
                    JSONObject propertiesWeb = new JSONObject();
                    try {
                        propertiesWeb.put(getString(R.string.property_brand),
                                offerDetails.getBrandId());
                        propertiesWeb.put(getString(R.string.property_offer_id), offerId);
                        BuyitApplication.mixpanel.track(getString(R.string.go_to_web),
                                propertiesWeb);
                    } catch (JSONException e) {
                        Ln.e("Error tracking the Web", e);
                    }

                    break;
                case R.id.completeDescriptionTextView:
                    /*
                     * case R.id.expandCompleteDescriptionTextView:
                     * completeDescriptionTextView.setMaxLines(Integer.MAX_VALUE); // The ellipsize
                     * must be set as null for android 2.3
                     * completeDescriptionTextView.setEllipsize(null);
                     * expandCompleteDescriptionTextView.setVisibility(View.GONE); break;
                     */
            }
        }
    };

    private int daysFromNow(Date date) {
        Date now = new Date();
        long diff = now.getTime() - date.getTime();
        return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    private boolean hasBeenEllipsized(TextView textView) {
        Layout layout = completeDescriptionTextView.getLayout();
        // If it contains layouts
        if (layout != null) {
            int lines = layout.getLineCount();
            // Check if it contains text
            if (lines > 0) {
                // Check if the last line has been elipsized
                if (layout.getEllipsisCount(lines - 1) > 0)
                    return true;
            }
        }

        return false;
    }

    /**
     * Refresh the favorite state of the offer. This is: - Show the right favorite/unfavorite icon -
     * Update the number of persons who has mark it as favorite
     */
    private void refreshFavoriteState() {
        // Favorites
        favoriteTextView.setText(String.valueOf(offerDetails.getFavorite()));
        if (offerDetails.isFavorite()) {
            favoriteTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_filled,
                    0, 0, 0);
        } else {
            favoriteTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite, 0, 0,
                    0);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        startTime();
    }

    @Override
    public void onStop() {
        super.onStop();

        // send mixpanel event
        // TODO: Improve it
        // BuyitApplication.mixpanel.track(getString(R.string.close_offer), new JSONObject());
        // stop timer
        timer.cancel();
        // stop application
        BuyitApplication.tracker.trackEvent("Analytics", "tiempo de residencia en la oferta",
                "tiempo:" + offerTime, (long) 0);
    }

    private void startTime() {
        // create timer
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                offerTime = offerTime + 1;
            }
        }, 1000);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        updateUi();
        if (!isVisibleToUser && session != null && offerDetails != null) {
            Ln.v("The user has closed the offer");
        }
    }

    private void updateUi() {
        if (!isVisibleToUser) {
            return;
        }

        if (!TextUtils.isEmpty(title) && actionBar != null) {
            actionBar.setTitle(title);
        }

        if (session != null && offerDetails != null) {
            // mixpanel event
            JSONObject props = new JSONObject();
            try {
                props.put(getString(R.string.property_brand), offerDetails.getBrandId());
                props.put(getString(R.string.property_offer_id), offerId);
                props.put(getString(R.string.property_filter), session.getOffersGender().toString());
                props.put(getString(R.string.property_navigation), mNavigation);
            } catch (JSONException e) {
                Ln.e("Error setting the properties to event", e);
            }

            BuyitApplication.mixpanel.track(getString(R.string.open_offer), props);
            Ln.v("Sending offer open event to mixPanel");

            // Track the activity viewed
            session.getActivityTrackerModule().onViewOffer();
        }

    }
}
