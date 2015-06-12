package com.worldspotlightapp.android.ui;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.viewpagerindicator.UnderlinePageIndicator;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.VideosModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideosListResponse;
import com.worldspotlightapp.android.model.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class MainActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "MainActivity";

    private FragmentManager mFragmentManager;
    private ClusterManager<Video> mClusterManager;

    private List<Video> mVideosList;

    // The last parse response before processed.
    // If a parse response has been processed, it will be null
    private ParseResponse mParseResponse;

    // Views
    private GoogleMap mMap;
    private FloatingActionButton mMyLocationFloatingActionButton;

    // ViewPager for preview
    private ViewPager mVideosPreviewViewPager;
    private UnderlinePageIndicator mVideosPreviewViewPagerIndicator;
    private VideosPreviewViewPagerAdapter mVideosPreviewViewPagerAdapter;

    // Variable used to record if the camera update is automatic or manual
    private boolean isAutomaticCameraUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getSupportFragmentManager();

        registerForLocalizationService();

        // Link the views
        mMyLocationFloatingActionButton = (FloatingActionButton) findViewById(R.id.my_location_floating_action_button);
        mMyLocationFloatingActionButton.setOnClickListener(onClickListener);

        mVideosPreviewViewPager = (ViewPager) findViewById(R.id.videos_preview_view_pager);
        mVideosPreviewViewPagerIndicator = (UnderlinePageIndicator) findViewById(R.id.videos_preview_view_pager_indicator);

        setupMapIfNeeded();
    }

    private void setupMapIfNeeded() {
        // If the map was already set, exit
        if (mMap != null) {
            return;
        }

        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        mClusterManager = new ClusterManager<Video>(this, mMap);
        VideosRenderer videosRenderer = new VideosRenderer(mContext, mMap, mClusterManager);
        mClusterManager.setRenderer(videosRenderer);
//        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mClusterManager.onCameraChange(cameraPosition);
                Log.v(TAG, "Camera position changed. Is it automatic update? " + isAutomaticCameraUpdate);
                if (!isAutomaticCameraUpdate) {
                    // Hide the viewpager
                    mVideosPreviewViewPager.setVisibility(View.GONE);
                    mVideosPreviewViewPagerIndicator.setVisibility(View.GONE);
                }
            }
        });
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Video>() {
            @Override
            public boolean onClusterItemClick(Video video) {
                Log.v(TAG, "Cluster item clicked. Starting automatic camera update");
                isAutomaticCameraUpdate = true;

                // Move to the point
                mMap.animateCamera(CameraUpdateFactory.newLatLng(video.getPosition()), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        Log.v(TAG, "Animating the camera finished");
                        // Show the viewpager
                        mVideosPreviewViewPager.setVisibility(View.VISIBLE);
                        mVideosPreviewViewPagerIndicator.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancel() {
                        Log.v(TAG, "Animating the camera canceled");
                        // Show the viewpager
                        mVideosPreviewViewPager.setVisibility(View.VISIBLE);
                        mVideosPreviewViewPagerIndicator.setVisibility(View.VISIBLE);
                    }
                });

                List<Video> videosListToShow = new ArrayList<Video>();
                videosListToShow.add(video);

                mVideosPreviewViewPagerAdapter = new VideosPreviewViewPagerAdapter(mFragmentManager, videosListToShow);
                mVideosPreviewViewPager.setAdapter(mVideosPreviewViewPagerAdapter);

                // Set the view pager in the view pager indicator
                mVideosPreviewViewPagerIndicator.setViewPager(mVideosPreviewViewPager);
                mVideosPreviewViewPagerIndicator.setFades(false);

                return true;
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Video>() {
            @Override
            public boolean onClusterClick(Cluster<Video> cluster) {
                isAutomaticCameraUpdate = true;

                // Move to the point
                mMap.animateCamera(CameraUpdateFactory.newLatLng(cluster.getPosition()));

                // Show the viewpager
                mVideosPreviewViewPager.setVisibility(View.VISIBLE);
                mVideosPreviewViewPagerIndicator.setVisibility(View.VISIBLE);

                List<Video> videosListToShow = new ArrayList<Video>();
                for (Video video: cluster.getItems()) {
                    videosListToShow.add(video);
                }

                mVideosPreviewViewPagerAdapter = new VideosPreviewViewPagerAdapter(mFragmentManager, videosListToShow);
                mVideosPreviewViewPager.setAdapter(mVideosPreviewViewPagerAdapter);

                // Set the view pager in the view pager indicator
                mVideosPreviewViewPagerIndicator.setViewPager(mVideosPreviewViewPager);
                mVideosPreviewViewPagerIndicator.setFades(false);

                return true;
            }
        });

        mVideosModule.requestVideosList(this);
        mNotificationModule.showLoadingDialog(mContext);
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.v(TAG, "Data received from " + observable);
        if (observable instanceof VideosModuleObserver) {
            if (o instanceof VideosModuleVideosListResponse) {
                VideosModuleVideosListResponse videosModuleVideosListResponse = (VideosModuleVideosListResponse)o;
                mParseResponse = videosModuleVideosListResponse.getParseResponse();
                mVideosList = videosModuleVideosListResponse.getVideosList();

                if (mIsInForeground) {
                    processDataIfExists();
                }

                observable.deleteObserver(this);
            }
        }
    }

    @Override
    protected void processDataIfExists() {
        setupMapIfNeeded();

        // 1. Check if the data exists
        // If there were not data received from backend, then
        // Not do anything
        if (mParseResponse == null) {
            return;
        }

        // 2. Process the data
        if (!mParseResponse.isError()) {
            // The sign up was correct. Go to the Main activity
            mClusterManager.addItems(mVideosList);
            mClusterManager.cluster();
        } else {
            // Some error happend
            mNotificationModule.showToast(mParseResponse.getHumanRedableResponseMessage(mContext), true);
        }

        mNotificationModule.dismissLoadingDialog();

        // 3. Remove the answers
        mParseResponse = null;

    }

    private class VideosRenderer extends DefaultClusterRenderer<Video> {

        public VideosRenderer(Context context, GoogleMap map, ClusterManager<Video> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(Video item, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_default_maps_marker));
        }
    }

    @Override
    public void onBackPressed() {
        // If the user clicks on back and the viewpager is visible, then hide it
        if (mVideosPreviewViewPager != null && mVideosPreviewViewPager.getVisibility() == View.VISIBLE) {
            mVideosPreviewViewPager.setVisibility(View.GONE);

            if (mVideosPreviewViewPagerIndicator != null) {
                mVideosPreviewViewPagerIndicator.setVisibility(View.GONE);
            }
            return;
        }
        super.onBackPressed();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.my_location_floating_action_button:
                    centerMapToUser();
                    break;
            }
        }
    };

    /**
     * Show my actual location.
     * If the map is null, don't do anything
     */
    private void centerMapToUser() {
        if (mMap == null) {
            return;
        }

        // Move to the point
        Location myLastKnownLocation = mGpsLocalizationModule.getLastKnownLocation();
        LatLng myLastKnownLatLng = new LatLng(myLastKnownLocation.getLatitude(), myLastKnownLocation.getLongitude());

        mMap.animateCamera(CameraUpdateFactory.newLatLng(myLastKnownLatLng));
    }
}
