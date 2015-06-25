package com.worldspotlightapp.android.ui;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import java.util.Collection;
import java.util.List;
import java.util.Observable;

public class MainActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "MainActivity";
    private static final int MENU_ITEM_SEARCH_ID = 1000;

    private FragmentManager mFragmentManager;
    private ClusterManager<Video> mClusterManager;

    private List<Video> mVideosList;

    // The last parse response before processed.
    // If a parse response has been processed, it will be null
    private ParseResponse mParseResponse;

    // Views
    private GoogleMap mMap;
    // Marker on the map
    private Marker mMyPositionMarker;

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

        // Center the map to the user
        centerMapToUser();
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
                isAutomaticCameraUpdate = true;

                centerMapToVideo(video);
                return true;
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Video>() {
            @Override
            public boolean onClusterClick(Cluster<Video> cluster) {
                isAutomaticCameraUpdate = true;

                centerMaptoCluster(cluster);
                return true;
            }
        });
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.v(TAG, "Data received from " + observable + ", Object:" + o);
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
            // Clean the existence items
            mClusterManager.clearItems();
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

    @Override
    protected void onResume() {
        super.onResume();

        // Hide the softkeyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        // If the map does not have the list of videos, request it
        // to the backend
        if (mVideosList == null) {
            mVideosModule.requestVideosList(this);
            mNotificationModule.showLoadingDialog(mContext);
            return;
        }

        // Check if the app has started because url link
        String videoId = getTriggeredVideoId();
        if (videoId != null) {
            centerVideo(videoId);
            return;
        }
    }

    /**
     * Check if the app started because the url related
     * @return
     *      True if so
     *      False if not
     */
    private String getTriggeredVideoId() {
        //get uri data
        Uri data = getIntent().getData();
        Log.v(TAG, "Data contained is " + data);
        if (data != null) {
            String[] dataSplitted = data.toString().split("/");
            return dataSplitted[dataSplitted.length - 1];
        }
        return null;
    }

    /**
     * Center the map to a specific video
     * @param videoId
     */
    private void centerVideo(String videoId) {
        Video videoToBeCentered = null;
        for (Video video: mVideosList) {
            if (video.getObjectId().equals(videoId)) {
                videoToBeCentered = video;
                break;
            }
        }

        // If the video was found
        if (videoToBeCentered == null) {
            return;
        }

        centerMapToVideo(videoToBeCentered);
    }

    /**
     * Center the map to a specific video
     * @param video
     *      The video to be centered. If it is null, don't do anything
     */
    private void centerMapToVideo(Video video) {
        if (video == null) {
            return;
        }

        List<Video> videosListToShow = new ArrayList<Video>();
        videosListToShow.add(video);

        centerMapToVideos(videosListToShow, video.getPosition());
    }

    /**
     * Center the map to a specific cluster
     * @param cluster
     *      The cluster with the data to be centered
     */
    private void centerMaptoCluster(Cluster<Video> cluster) {
        if (cluster == null) {
            return;
        }

        Collection<Video> clusterVideos = cluster.getItems();
        if (clusterVideos == null || clusterVideos.size() == 0) {
            return;
        }

        List<Video> videosListToShow = new ArrayList<Video>();
        for (Video video: clusterVideos) {
            videosListToShow.add(video);
        }

        centerMapToVideos(videosListToShow, cluster.getPosition());
    }

    private void centerMapToVideos(List<Video> videosList, LatLng position) {
        Log.v(TAG, "Centering the videos on the position " + position);
        isAutomaticCameraUpdate = true;

        // Move to the point
        mMap.animateCamera(CameraUpdateFactory.newLatLng(position));

        // Show the viewpager
        mVideosPreviewViewPager.setVisibility(View.VISIBLE);
        mVideosPreviewViewPagerIndicator.setVisibility(View.VISIBLE);

        mVideosPreviewViewPagerAdapter = new VideosPreviewViewPagerAdapter(mFragmentManager, videosList);
        mVideosPreviewViewPager.setAdapter(mVideosPreviewViewPagerAdapter);

        // Set the view pager in the view pager indicator
        mVideosPreviewViewPagerIndicator.setViewPager(mVideosPreviewViewPager);
        mVideosPreviewViewPagerIndicator.setFades(false);

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
        if (myLastKnownLocation == null) {
            return;
        }

        LatLng myLastKnownLatLng = new LatLng(myLastKnownLocation.getLatitude(), myLastKnownLocation.getLongitude());

        // Add the marker
        if (mMyPositionMarker != null) {
            mMyPositionMarker.remove();
        }

        mMyPositionMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(myLastKnownLatLng.latitude, myLastKnownLatLng.longitude))
                .title(getString(R.string.main_activity_you_are_here)));
        mMyPositionMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(myLastKnownLatLng);
        mMap.animateCamera(cameraUpdate);
    }

    // Action bar
    // Action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Update user profile
        MenuItem menuItemSearch = menu.add(Menu.NONE, MENU_ITEM_SEARCH_ID, Menu
                .NONE, R.string.action_bar_search)
                .setIcon(R.drawable.ic_action_search);
        menuItemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_SEARCH_ID:
                searchByKeyword();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void searchByKeyword() {
        // TODO: Show the editText on the action bar. Check for editText collapse
        // TODO: Capture the done button
        // TODO: Capture the keyword
        // TODO: Set a cancel button in the editText. If the user press on cancel button
        // Show all the videos.
        // TODO: Remove the follow hardcoded keyword
        String keyword = "Dubai";
        mNotificationModule.showLoadingDialog(mContext);
        mVideosModule.searchByKeyword(this, keyword);
    }
}
