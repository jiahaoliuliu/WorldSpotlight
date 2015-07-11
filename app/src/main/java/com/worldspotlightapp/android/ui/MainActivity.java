package com.worldspotlightapp.android.ui;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.support.v7.widget.SearchView;
import android.widget.EditText;
import android.widget.ImageView;

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
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.Stack;

public class MainActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "MainActivity";
    private static final int MENU_ITEM_SEARCH_ID = 1000;

    private FragmentManager mFragmentManager;
    private ClusterManager<Video> mClusterManager;

    private List<Video> mVideosList;

    /**
     * The set of response retrieved from the modules
     */
    private Stack<Object> mResponsesStack;

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

    private MenuItem menuItemSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Data initialization
        mFragmentManager = getSupportFragmentManager();
        mResponsesStack = new Stack<Object>();
        mVideosList = new ArrayList<Video>();

        // Delete all the possible observers
        mVideosModule.deleteObserver(this);

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
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mClusterManager.onCameraChange(cameraPosition);
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

                // TODO: Remove this
                VideosModuleVideosListResponse videosModuleVideosListResponse = (VideosModuleVideosListResponse)o;
                ParseResponse parseResponse =videosModuleVideosListResponse.getParseResponse();
                Log.d(TAG, "Parse response received " + parseResponse);
                if (!parseResponse.isError()) {
                    List<Video> videoListReceived = videosModuleVideosListResponse.getVideosList();
                    int numberVideos = videoListReceived == null? 0 : videoListReceived.size();
                    Log.v(TAG, "The list of videos has " + numberVideos + " videos. Are extra videos? " + videosModuleVideosListResponse.areExtraVideos());
                }

                // Add the data to the list of responses
                mResponsesStack.push(o);

                if (isInForeground()) {
                    Log.v(TAG, "This activity is in foreground. Processing data if exists");
                    processDataIfExists();
                } else {
                    Log.v(TAG, "This activity is not in foregorund. Not do anything");
                }

                // The MainActivity will listen constantly to the changes on the list of videos
                //observable.deleteObserver(this);
            }
        }
    }

    @Override
    protected void processDataIfExists() {
        Log.v(TAG, "Processing data if exists. Is the activity in foreground " + isInForeground());
        setupMapIfNeeded();

        // 1. Check if the data exists
        // If there were not data received from backend, then
        // Not do anything
        if (mResponsesStack.isEmpty()) {
            return;
        }

        // Special condition. At this point if the map is null
        // the mClusterManager could not be initialized.
        // Of course when the Map is null, there is nothing to do
        if (mClusterManager == null) {
            return;
        }

        // 2. Process the data
        while (!mResponsesStack.isEmpty()) {
            Object response = mResponsesStack.pop();
            // Checking the type of data
            if (response instanceof VideosModuleVideosListResponse) {
                VideosModuleVideosListResponse videosModuleVideosListResponse = (VideosModuleVideosListResponse) response;
                // If the list of videos received are extra videos to be added to the list of existence videos
                ParseResponse parseResponse = videosModuleVideosListResponse.getParseResponse();
                if (videosModuleVideosListResponse.areExtraVideos()) {
                    if (!parseResponse.isError()) {
                        List<Video> extraVideos = videosModuleVideosListResponse.getVideosList();
                        int numberVideoRetrieved = extraVideos == null? 0 : extraVideos.size();
                        Log.v(TAG, "The list of extra videos received contains " + numberVideoRetrieved + " videos");
                        mVideosList.addAll(extraVideos);
                        mClusterManager.addItems(extraVideos);
                        mClusterManager.cluster();
                    } else {
                        Log.v(TAG, "Error updating the list of videos");
                    }
                // if the list of videos received should replace the existence list of videos
                } else {
                    if (!parseResponse.isError()) {
                        List<Video> videoList = videosModuleVideosListResponse.getVideosList();
                        int numberVideoRetrieved = videoList == null? 0 : videoList.size();
                        Log.v(TAG, "The list of videos received contains " + numberVideoRetrieved + " videos");
                        mVideosList = new ArrayList<>(videosModuleVideosListResponse.getVideosList());
                        mClusterManager.clearItems();
                        mClusterManager.addItems(mVideosList);
                        mClusterManager.cluster();
                    } else {
                        // Some error happend
                        mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
                    }
                }
            }
        }

        Log.v(TAG, "Dismissing the loading dialog");
        mNotificationModule.dismissLoadingDialog();

        // 3. Remove the responses
        // Not do anything. Because the list of the response is a stack. Once all the responses has been pop out,
        // there is not need to clean them
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v(TAG, "Is this activity in foreground? " + isInForeground());

        // Hide the softkeyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        // If the map does not have the list of videos, request it
        // to the backend
        if (mVideosList == null || mVideosList.isEmpty()) {
            Log.v(TAG, "The list of videos is empty. Requesting it to the videos module");
            mNotificationModule.showLoadingDialog(mContext);
            mVideosModule.requestAllVideos(this);
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
            // Reset data
            getIntent().setData(null);
            return dataSplitted[dataSplitted.length - 1];
        }
        return null;
    }

    /**
     * Center the map to a specific video
     * @param videoId
     */
    private void centerVideo(String videoId) {
        Log.v(TAG, "Trying to center the map to the video " + videoId);
        Video videoToBeCentered = null;
        for (Video video: mVideosList) {
            if (video.getObjectId().equals(videoId)) {
                videoToBeCentered = video;
                Log.v(TAG, "Video Found " + video);
                break;
            }
        }

        // If the video cannot be found, try to look for it in the
        // database
        if (videoToBeCentered == null) {
            Log.v(TAG, "Video not found in the memory. Looking for it in the database");
            videoToBeCentered = mVideosModule.getVideoInfo(videoId);
        }

        // If the video was found
        if (videoToBeCentered == null) {
            Log.w(TAG, "Video to be centered not found");
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Update user profile
        menuItemSearch = menu.add(Menu.NONE, MENU_ITEM_SEARCH_ID, Menu
                .NONE, R.string.action_bar_search)
                .setIcon(R.drawable.ic_action_search)
                .setActionView(R.layout.search_layout);
        menuItemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
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
        final SearchView searchActionView = (SearchView) MenuItemCompat.getActionView(menuItemSearch);
        searchActionView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.v(TAG, "Searching the videos with the keyword " + query);
                mNotificationModule.showLoadingDialog(mContext);
                mVideosModule.searchByKeyword(MainActivity.this, query);
                searchActionView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        ImageView closeButton = (ImageView) searchActionView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "The search has been cancelled. Requesting the list of all the videos to the module");
                mNotificationModule.showLoadingDialog(mContext);
                // Retrieve the list of all the videos
                mVideosModule.requestAllVideos(MainActivity.this);
                EditText et = (EditText) findViewById(R.id.search_src_text);
                et.setText("");
                searchActionView.setQuery("", false);
                searchActionView.onActionViewCollapsed();
                menuItemSearch.collapseActionView();
            }
        });

        MenuItemCompat.setOnActionExpandListener(menuItemSearch, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mVideosModule.requestAllVideos(MainActivity.this);
                searchActionView.setQuery("", false);
                searchActionView.onActionViewCollapsed();
                return true;
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return o instanceof MainActivity;

    }

    @Override
    public int hashCode() {
        return 0;
    }
}
