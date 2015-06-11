package com.worldspotlightapp.android.maincontroller.modules.gpslocalizationmodule;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Interface for localization module.
 *
 * To use it, the activity must be register as OnConnectionFailedListener
 *
 * Created by Jiahao on 6/3/15.
 */
public interface IGpsLocalizationModule {

    /**
     * Register
     * @param onConnectionFailedListener
     */
    void registerForLocalizationService(GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener);

    /**
     * Get he last know location.
     *
     * To use this the activity must be registered for the localization service by calling
     * {@link #registerForLocalizationService(GoogleApiClient.OnConnectionFailedListener)}
     * @return
     */
    Location getLastKnownLocation();

    /**
     * Unregister an activity for localization service
     */
    void unregisterForLocalizationService();

    /**
     * Ask the Google API Client to connect. Before use this method make sure that
     * Google Play Service is available by calling
     * {@link #registerForLocalizationService(GoogleApiClient.OnConnectionFailedListener)}}
     */
    void connectWithLocalizationService();

    /**
     * Ask the Google API Client to disconnect. Before use this method make sure that
     * Google Play Service is available by calling
     * {@link #registerForLocalizationService(GoogleApiClient.OnConnectionFailedListener)}}
     */
    void disconnectWithLocalizationService();
}
