package com.worldspotlightapp.android.maincontroller.modules.gpslocalizationmodule;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.worldspotlightapp.android.maincontroller.Preferences;
import com.worldspotlightapp.android.maincontroller.Preferences.DoubleId;

/**
 * Module create for localization service.
 *
 * If you wish to get the last know location, you must register the activity at least once
 * with {@link #registerForLocalizationService(GoogleApiClient.OnConnectionFailedListener)}
 * otherwise the value returned by {@link #getLastKnownLocation()} will be always null
 *
 * Created by Jiahao on 6/3/15.
 */
public class GpsLocalizationModule implements IGpsLocalizationModule,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static final String TAG = "GpsLocalizationModule";

    private GoogleApiClient mGoogleApiClient;

    private Context mContext;

    // The last location
    private Location mLastKnownLocation;
    // In the extreme case when the user does not have location
    // Set the default location to Dubai
    private static final double DEFAULT_LOCATION_LATITUDE = 25.089521;
    private static final double DEFAULT_LOCATION_LONGITUDE = 55.152843;

    private boolean mRequestingLocationUpdates = true;

    // The request for the location
    private LocationRequest mLocationRequest;
    private static final int LOCATION_REQUEST_INTERVAL = 300000; // 5min
    // The fastest interval that the localization module can handle
    private static final int LOCATION_REQUEST_FASTEST_INTERVAL = 60000;
    // The priority for update. The balanced one has an accuracy about 100m
    private static final int LOCATION_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

    // An instance of the preferences to save the last known location persistently
    private Preferences mPreferences;

    public GpsLocalizationModule(Context context, Preferences preferences) {
        this.mContext = context;
        this.mPreferences = preferences;

        // Get the last known location from shared preferences if exists
        if (preferences.contains(DoubleId.LAST_KNOWN_LOCATION_LATITUDE)
                && preferences.contains(DoubleId.LAST_KNOWN_LOCATION_LONGITUDE)) {
            mLastKnownLocation = new Location("");
            mLastKnownLocation.setLatitude(preferences.get(DoubleId.LAST_KNOWN_LOCATION_LATITUDE));
            mLastKnownLocation.setLongitude(preferences.get(DoubleId.LAST_KNOWN_LOCATION_LONGITUDE));
        // If the location was not set, set it to Dubai using the default one
        } else {
            mLastKnownLocation = new Location("");
            mLastKnownLocation.setLatitude(DEFAULT_LOCATION_LATITUDE);
            mLastKnownLocation.setLongitude(DEFAULT_LOCATION_LONGITUDE);
        }
    }

    @Override
    public void registerForLocalizationService(GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener) {
        // Lazy instantiation
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(onConnectionFailedListener)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public Location getLastKnownLocation() {
        if (mGoogleApiClient == null) {
            throw new IllegalStateException("Your must register your activity for localization service " +
                    "before use this method");
        }

        return mLastKnownLocation;
    }

    private void setLastKnownLocation(Location newLocation) {
        Log.v(TAG, "Location updated " + newLocation);
        mLastKnownLocation = newLocation;

        // Save it in the preferences
        mPreferences.set(DoubleId.LAST_KNOWN_LOCATION_LATITUDE, newLocation.getLatitude());
        mPreferences.set(DoubleId.LAST_KNOWN_LOCATION_LONGITUDE, newLocation.getLongitude());
    }

    @Override
    public void unregisterForLocalizationService() {
        // Stop location updates
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        // Destroy the reference to the Google Api Client
        if (mGoogleApiClient != null) {
            mGoogleApiClient = null;
        }
    }

    @Override
    public void connectWithLocalizationService() {
        if (mGoogleApiClient == null) {
            throw new IllegalStateException("Before connecting with localization service, your activity must" +
                    "register for localization service");
        }

        // Connect only if it is not connecting or if it has connected
        if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void disconnectWithLocalizationService() {
        if (mGoogleApiClient == null) {
            throw new IllegalStateException("Before disconnect with localization service, your activity must" +
                    "register for localization service");
        }

        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "Google API client connected");
        mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        Log.v(TAG, "Starting the location updates");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LOCATION_PRIORITY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: Disable any UI components that depend on Google APIs
        // until onConnected() is called
    }

    @Override
    public void onLocationChanged(Location location) {
        setLastKnownLocation(location);
    }
}
