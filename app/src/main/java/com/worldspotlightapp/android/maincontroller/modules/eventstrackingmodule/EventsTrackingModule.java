package com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule;

import android.content.Context;
import android.util.Log;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.maps.model.LatLng;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.ui.MainApplication;
import com.worldspotlightapp.android.utils.Secret;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class EventsTrackingModule implements IEventsTrackingModule {

    private static final String TAG = "EventsTrackingModule";

    private Context mContext;
    private UUID mUUID;

    private MixpanelAPI mMixpanel;

    public EventsTrackingModule(Context context, UUID uuid) {
        mContext = context;
        mUUID = uuid;

        // Initialize Mixpanel
        String mixPanelToken =
                MainApplication.IS_PRODUCTION?
                        Secret.MIX_PANEL_API_PRODUCTION :
                        Secret.MIX_PANEL_API_DEBUG;
        mMixpanel = MixpanelAPI.getInstance(context, mixPanelToken);

        // Identify the user
        mMixpanel.identify(mUUID.toString());
    }

    @Override
    public void trackUserAction(ScreenId screenId, EventId eventId, Object... objects) {
        switch (screenId) {
            case LOGIN_SCREEN:
                trackLoginScreenAction(eventId, objects);
                break;
            case MAIN_SCREEN:
                trackMainScreenAction(eventId, objects);
                break;
            case VIDEO_DETAILS_SCREEN:
                trackVideoDetailsScreenAction(eventId, objects);
                break;
            default:
                throw new IllegalArgumentException("The event " + eventId.toString() + " of the screen "
                        + screenId.toString() + " cannot be tracked");
        }
    }

    /**
     * Track the actions from the login screen
     * @param eventId
     *      The id of the event happened
     * @param objects
     *      The details to be tracked
     */
    private void trackLoginScreenAction(EventId eventId, Object... objects) {
        String prefix = mContext.getString(R.string.mp_login_activity_prefix);
        switch (eventId) {
            case LOGIN_WITH_FACEBOOK:
                mMixpanel.track(prefix + " " +
                        mContext.getString(R.string.mp_login_activity_facebook_login), new JSONObject());
                break;
            case LOGIN_WITH_GOOGLE_PLUS:
                mMixpanel.track(prefix + " " +
                        mContext.getString(R.string.mp_login_activity_google_plus_login), new JSONObject());
                break;
            case SIGN_UP_WITH_PARSE:
                mMixpanel.track(prefix + " " +
                        mContext.getString(R.string.mp_login_activity_parse_sign_up), new JSONObject());
                break;
            case LOGIN_WITH_PARSE:
                mMixpanel.track(prefix + " " +
                        mContext.getString(R.string.mp_login_activity_parse_login), new JSONObject());
                break;
            case SKIP_LOGIN:
                mMixpanel.track(prefix + " " +
                        mContext.getString(R.string.mp_login_activity_skip_login), new JSONObject());
                break;
            default:
                throw new IllegalArgumentException("The event " + eventId.toString() + " does not belongs" +
                        "to Main screen, so it cannot be tracked");
        }
    }

    /**
     * Track the actions from the main screen
     * @param eventId
     *      The id of the event happened
     * @param objects
     *      The details to be tracked
     */
    private void trackMainScreenAction(EventId eventId, Object... objects) {
        String prefix = mContext.getString(R.string.mp_main_activity_prefix);
        switch (eventId) {
            case SEARCH_STARTED:
                mMixpanel.track(mContext.getString(R.string.mp_main_activity_prefix) + " " +
                        mContext.getString(R.string.mp_main_activity_search_started), new JSONObject());
                break;
            case SEARCH_BY_KEYWORD:
                if (objects.length < 1) {
                    throw new IllegalArgumentException("You must provide at least one argument for this event");
                }

                String keyword = null;
                try {
                    keyword = (String) objects[0];
                } catch (ClassCastException classCastException) {
                    throw new ClassCastException("The first argument must be an instance of String or an extension of it");
                }

                try {
                    JSONObject attributes = new JSONObject();
                    attributes.put(mContext.getString(R.string.mp_main_activity_search_by_keyword_keyword), keyword);
                    mMixpanel.track(prefix + " " +
                            mContext.getString(R.string.mp_main_activity_search_by_keyword_event), attributes);
                } catch (JSONException e) {
                    Log.e(TAG, "Error sending event to mixpanel", e);
                }

                break;
            case SEARCH_FINISHED:
                mMixpanel.track(prefix + " " +
                        mContext.getString(R.string.mp_main_activity_search_finished), new JSONObject());
                break;
            case VIDEOS_PREVIEW:
                if (objects.length < 2) {
                    throw new IllegalArgumentException("You must provide at least one argument for this event");
                }

                List<Video> videosIdList = new ArrayList<Video>();
                try {
                    videosIdList = (List) objects[0];
                } catch (ClassCastException classCastException) {
                    throw new ClassCastException("The first argument must be an instance of List<Video> or an extension of it");
                }

                LatLng position = new LatLng(0,0);
                try {
                    position = (LatLng) objects[1];
                } catch (ClassCastException classCastException) {
                    throw new ClassCastException("The first argument must be an instance of LatLng or an extension of it");
                }

                try {
                    JSONObject attributes = new JSONObject();
                    // Create the list of videos as String
                    JSONArray videosIdListJsonArray = new JSONArray();
                    for (Video video : videosIdList) {
                        videosIdListJsonArray.put(video.getObjectId());
                    }

                    attributes.put(mContext.getString(R.string.mp_main_activity_videos_preview_videos_id), videosIdListJsonArray);
                    attributes.put(mContext.getString(R.string.mp_main_activity_videos_preview_position_latitude), position.latitude);
                    attributes.put(mContext.getString(R.string.mp_main_activity_videos_preview_position_longitude), position.longitude);
                    Log.d(TAG, "List of attributes to be send to MixPanel " + attributes.toString());
                    mMixpanel.track(prefix + " " +
                            mContext.getString(R.string.mp_main_activity_videos_preview_event), attributes);
                } catch (JSONException e) {
                    Log.e(TAG, "Error sending event to mixpanel", e);
                }
                break;
            case VIDEO_PREVIEW_CLICK:
                if (objects.length < 1) {
                    throw new IllegalArgumentException("You must provide at least one argument for this event");
                }

                String videoId = null;
                try {
                    videoId = (String) objects[0];
                } catch (ClassCastException classCastException) {
                    throw new ClassCastException("The first argument must be an instance of String or an extension of it");
                }

                try {
                    JSONObject attributes = new JSONObject();
                    attributes.put(mContext.getString(R.string.mp_main_activity_videos_preview_click_video_id), videoId);
                    mMixpanel.track(prefix + " " +
                            mContext.getString(R.string.mp_main_activity_videos_preview_click_event), attributes);
                } catch (JSONException e) {
                    Log.e(TAG, "Error sending event to mixpanel", e);
                }

                break;
            case LOGIN:
                mMixpanel.track(prefix + " " +
                        mContext.getString(R.string.mp_main_activity_login), new JSONObject());
                break;
            case FAVOURITES:
                mMixpanel.track(prefix + " " +
                        mContext.getString(R.string.mp_main_activity_favourites), new JSONObject());
                break;
            case LOGOUT:
                mMixpanel.track(prefix + " " +
                        mContext.getString(R.string.mp_main_activity_logout), new JSONObject());
                break;
            case ADD_A_VIDEO:
                mMixpanel.track(prefix + " " +
                        mContext.getString(R.string.mp_main_activity_add_a_video), new JSONObject());
                break;
            default:
                throw new IllegalArgumentException("The event " + eventId.toString() + " does not belongs" +
                        "to Main screen, so it cannot be tracked");
        }
    }

    /**
     * Track the actions from the video details screen
     * @param eventId
     *      The id of the event happened
     * @param objects
     *      The details to be tracked
     */
    private void trackVideoDetailsScreenAction(EventId eventId, Object... objects) {
        String prefix = mContext.getString(R.string.mp_video_details_activity_prefix);
        switch (eventId) {
            case REPORT_A_VIDEO:
                if (objects.length < 1) {
                    throw new IllegalArgumentException("You must provide at least two argument for this event");
                }

                String videoId = null;
                try {
                    videoId = (String) objects[0];
                } catch (ClassCastException classCastException) {
                    throw new ClassCastException("The first argument must be an instance of String or an extension of it");
                }

                try {
                    JSONObject attributes = new JSONObject();
                    attributes.put(mContext.getString(R.string.mp_video_details_activity_report_video_id), videoId);
                    mMixpanel.track(prefix+ " " +
                            mContext.getString(R.string.mp_video_details_activity_report_video_event), attributes);
                } catch (JSONException e) {
                    Log.e(TAG, "Error sending event to mixpanel", e);
                }

                break;
            case LIKE_A_VIDEO:
                if (objects.length < 2) {
                    throw new IllegalArgumentException("You must provide at least two argument for this event");
                }

                videoId = null;
                try {
                    videoId = (String) objects[0];
                } catch (ClassCastException classCastException) {
                    throw new ClassCastException("The first argument must be an instance of String or an extension of it");
                }

                boolean likeThisVideo;
                try {
                    likeThisVideo = (boolean) objects[1];
                } catch (ClassCastException classCastException) {
                    throw new ClassCastException("The first argument must be an instance of boolean or an extension of it");
                }

                try {
                    JSONObject attributes = new JSONObject();
                    attributes.put(mContext.getString(R.string.mp_video_details_activity_like_video_id), videoId);
                    attributes.put(mContext.getString(R.string.mp_video_details_activity_like_it), likeThisVideo);
                    mMixpanel.track(prefix+ " " +
                            mContext.getString(R.string.mp_video_details_activity_like_video_event), attributes);
                } catch (JSONException e) {
                    Log.e(TAG, "Error sending event to mixpanel", e);
                }

                break;
            case FULL_SCREEN:
                if (objects.length < 1) {
                    throw new IllegalArgumentException("You must provide at least one argument for this event");
                }

                String videoIdFullScreen = null;
                try {
                    videoIdFullScreen = (String) objects[0];
                } catch (ClassCastException classCastException) {
                    throw new ClassCastException("The first argument must be an instance of String or an extension of it");
                }

                try {
                    JSONObject attributes = new JSONObject();
                    attributes.put(mContext.getString(R.string.mp_video_details_activity_full_screen_video_id), videoIdFullScreen);
                    mMixpanel.track(prefix+ " " +
                            mContext.getString(R.string.mp_video_details_activity_full_screen_event), attributes);
                } catch (JSONException e) {
                    Log.e(TAG, "Error sending event to mixpanel", e);
                }

                break;
            case SHARE:
                if (objects.length < 1) {
                    throw new IllegalArgumentException("You must provide at least one argument for this event");
                }

                String videoIdShare = null;
                try {
                    videoIdShare = (String) objects[0];
                } catch (ClassCastException classCastException) {
                    throw new ClassCastException("The first argument must be an instance of String or an extension of it");
                }

                try {
                    JSONObject attributes = new JSONObject();
                    attributes.put(mContext.getString(R.string.mp_video_details_activity_share_video_id), videoIdShare);
                    mMixpanel.track(prefix + " " +
                            mContext.getString(R.string.mp_video_details_activity_share_event), attributes);
                } catch (JSONException e) {
                    Log.e(TAG, "Error sending event to mixpanel", e);
                }

                break;
            default:
                throw new IllegalArgumentException("The event " + eventId.toString() + " does not belongs" +
                        "to Main screen, so it cannot be tracked");
        }
    }

    @Override
    public void trackAppInitialization() {
        Log.v(TAG, "App initialized.");
        mMixpanel.track(mContext.getString(R.string.mp_app_initialized), new JSONObject());

        // Facebook logs
        // Log 'install' and 'app activate' App Events
        AppEventsLogger.activateApp(mContext);
    }

    @Override
    public void trackAppFinalization() {
        Log.v(TAG, "App finalized");
        mMixpanel.track(mContext.getString(R.string.mp_app_finished), new JSONObject());
        mMixpanel.flush();

        // Facebook logs
        // Logs 'app deactivate' App Event
        AppEventsLogger.deactivateApp(mContext);
    }
}
