package com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule;

import android.content.Context;
import android.util.Log;

import com.facebook.appevents.AppEventsLogger;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.ui.MainApplication;
import com.worldspotlightapp.android.utils.Secret;

import org.json.JSONException;
import org.json.JSONObject;

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
//                trackVideoScreenAction(eventId, objects);
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
        switch (eventId) {
            case LOGIN_WITH_FACEBOOK:
                mMixpanel.track(mContext.getString(R.string.mp_login_activity_prefix) + " " +
                        mContext.getString(R.string.mp_login_activity_facebook_login), new JSONObject());
                break;
            case LOGIN_WITH_GOOGLE_PLUS:
                mMixpanel.track(mContext.getString(R.string.mp_login_activity_prefix) + " " +
                        mContext.getString(R.string.mp_login_activity_google_plus_login), new JSONObject());
                break;
            case SKIP_LOGIN:
                mMixpanel.track(mContext.getString(R.string.mp_login_activity_prefix) + " " +
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
                    mMixpanel.track(mContext.getString(R.string.mp_main_activity_prefix) + " " +
                            mContext.getString(R.string.mp_main_activity_search_by_keyword_event), attributes);
                } catch (JSONException e) {
                    Log.e(TAG, "Error sending event to mixpanel", e);
                }

                break;
            case SEARCH_FINISHED:
                mMixpanel.track(mContext.getString(R.string.mp_main_activity_prefix) + " " +
                        mContext.getString(R.string.mp_main_activity_search_finished), new JSONObject());
                break;
            case USER_LOCALIZED:
                mMixpanel.track(mContext.getString(R.string.mp_main_activity_prefix) + " " +
                        mContext.getString(R.string.mp_main_activity_localize_user), new JSONObject());
                break;
            default:
                throw new IllegalArgumentException("The event " + eventId.toString() + " does not belongs" +
                        "to Main screen, so it cannot be tracked");
        }
    }

    @Override
    public void trackAppInitialization() {
        mMixpanel.track(mContext.getString(R.string.mp_app_initialized), new JSONObject());

        // Facebook logs
        // Log 'install' and 'app activate' App Events
        AppEventsLogger.activateApp(mContext);
    }

    @Override
    public void trackAppFinalization() {
        mMixpanel.track(mContext.getString(R.string.mp_app_finished), new JSONObject());
        mMixpanel.flush();

        // Facebook logs
        // Logs 'app deactivate' App Event
        AppEventsLogger.deactivateApp(mContext);
    }
}
