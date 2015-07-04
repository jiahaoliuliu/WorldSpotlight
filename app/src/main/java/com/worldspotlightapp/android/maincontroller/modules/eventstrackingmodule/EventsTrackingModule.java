package com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule;

import android.content.Context;

import com.facebook.appevents.AppEventsLogger;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.ui.MainApplication;
import com.worldspotlightapp.android.utils.LocalConstants;

import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class EventsTrackingModule implements IEventsTrackingModule {

    private Context mContext;
    private UUID mUUID;

    private MixpanelAPI mMixpanel;

    public EventsTrackingModule(Context context, UUID uuid) {
        mContext = context;
        mUUID = uuid;

        // Initialize Mixpanel
        String mixPanelToken =
                MainApplication.IS_PRODUCTION?
                        LocalConstants.MIX_PANEL_API_PRODUCTION :
                        LocalConstants.MIX_PANEL_API_DEBUG;
        mMixpanel = MixpanelAPI.getInstance(context, mixPanelToken);

        // Identify the user
        mMixpanel.identify(mUUID.toString());
    }

    @Override
    public void trackAppInitialization() {
        mMixpanel.track(mContext.getString(R.string.mp_login), new JSONObject());

        // Facebook logs
        // Log 'install' and 'app activate' App Events
        AppEventsLogger.activateApp(mContext);
    }

    @Override
    public void trackAppFinalization() {
        mMixpanel.track(mContext.getString(R.string.mp_logout), new JSONObject());
        mMixpanel.flush();

        // Facebook logs
        // Logs 'app deactivate' App Event
        AppEventsLogger.deactivateApp(mContext);
    }
}
