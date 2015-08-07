package com.worldspotlightapp.android.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.support.v7.widget.SearchView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.UnderlinePageIndicator;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.ScreenId;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.EventId;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.UserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleLikesListResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.VideosModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleAddAVideoResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleLikedVideosListResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideosListResponse;
import com.worldspotlightapp.android.model.UserData;
import com.worldspotlightapp.android.model.Video;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Stack;

public class MainActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "MainActivity";
    private static final int MENU_ITEM_SEARCH_ID = 1000;

    // Internal structured data
    private FragmentManager mFragmentManager;
    private ClusterManager<Video> mClusterManager;

    private List<Video> mVideosList;

    /**
     * The set of response retrieved from the modules
     */
    private Stack<Object> mResponsesStack;

    // Views
    private GoogleMap mMap;
    //      Drawer
    private DrawerLayout mDrawerLayout;
    private ImageView mUserProfileImageView;
    private TextView mUserNameTextView;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mDrawer;
    private MenuItem mDrawerItemLogin;
    private MenuItem mDrawerItemFavourites;
    private MenuItem mDrawerItemLogout;

    // By default drawer is not open
    private boolean mIsDrawerOpen;

    private FloatingActionButton mAddVideoFloatingActionButton;

    // ViewPager for preview
    private ViewPager mVideosPreviewViewPager;
    private UnderlinePageIndicator mVideosPreviewViewPagerIndicator;
    private VideosPreviewViewPagerAdapter mVideosPreviewViewPagerAdapter;

    // Variable used to record if the camera update is automatic or manual
    private boolean isAutomaticCameraUpdate;

    private boolean mIsShowingFavouriteListVideos;

    // Action bar items
    private Menu mMenu;
    private MenuItem mMenuItemSearch;

    private Picasso mPicasso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launch Add A Video activity if the user has shared the text
        if (hasActivityStartedBySharingText()) {
            launchAddAVideoActivity();
        // Launch login activity if the user has not logged in
        } else if (!mUserDataModule.hasUserData()) {
            launchSignUpLoginActivity();
        }

        setContentView(R.layout.activity_main);

        // Data initialization
        mFragmentManager = getSupportFragmentManager();
        mResponsesStack = new Stack<Object>();
        mVideosList = new ArrayList<Video>();

        // Delete all the possible observers
        mVideosModule.deleteObserver(this);
        mUserDataModule.deleteObserver(this);

        mPicasso = Picasso.with(mContext);

        // Link the views
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mUserProfileImageView = (ImageView) findViewById(R.id.user_profile_image_view);
        mUserNameTextView = (TextView) findViewById(R.id.user_name_text_view);

        mDrawer = (NavigationView) findViewById(R.id.drawer);
        mDrawerItemLogin = mDrawer.getMenu().findItem(R.id.drawer_item_login);
        mDrawerItemFavourites = mDrawer.getMenu().findItem(R.id.drawer_item_favourites);
        mDrawerItemLogout = mDrawer.getMenu().findItem(R.id.drawer_item_logout);

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.v(TAG, "Drawer opened");
                mIsDrawerOpen = true;
                updateActionBarItems();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.v(TAG, "Drawer closed");
                mIsDrawerOpen = false;
                updateActionBarItems();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.drawer_item_login:
                        Log.v(TAG, "Login clicked");
                        mEventTrackingModule.trackUserAction(ScreenId.MAIN_SCREEN, EventId.LOGIN);
                        launchSignUpLoginActivity();
                        return true;
                    case R.id.drawer_item_favourites:
                        Log.v(TAG, "Favourites clicked");
                        mEventTrackingModule.trackUserAction(ScreenId.MAIN_SCREEN, EventId.FAVOURITES);
                        showFavouritesVideosToUser();
                        // Close the drawer
                        mDrawerLayout.closeDrawers();
                        return true;
                    case R.id.drawer_item_logout:
                        Log.v(TAG, "Logout clicked");
                        mEventTrackingModule.trackUserAction(ScreenId.MAIN_SCREEN, EventId.LOGOUT);
                        mUserDataModule.logout();
                        launchSignUpLoginActivity();
                        // Close the drawer
                        mDrawerLayout.closeDrawers();
                        return true;
                    default:
                        return false;
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mAddVideoFloatingActionButton = (FloatingActionButton) findViewById(R.id.add_video_floating_action_button);
        mAddVideoFloatingActionButton.setOnClickListener(onClickListener);

        mVideosPreviewViewPager = (ViewPager) findViewById(R.id.videos_preview_view_pager);
        mVideosPreviewViewPagerIndicator = (UnderlinePageIndicator) findViewById(R.id.videos_preview_view_pager_indicator);

        // Update data
        setupMapIfNeeded();
    }

    private boolean hasActivityStartedBySharingText() {
        Log.v(TAG, "Intent get " + mIntent);
        String action = mIntent.getAction();
        String type = mIntent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                return true;
            }
        }
        return false;
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

                showVideoPreview(video);
                return true;
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Video>() {
            @Override
            public boolean onClusterClick(Cluster<Video> cluster) {
                isAutomaticCameraUpdate = true;
                if (cluster == null) {
                    return true;
                }

                Collection<Video> clusterVideos = cluster.getItems();
                if (clusterVideos == null || clusterVideos.size() == 0) {
                    return true;
                }

                List<Video> videosListToShow = new ArrayList<Video>();
                for (Video video: clusterVideos) {
                    videosListToShow.add(video);
                }

                showVideosPreview(videosListToShow, cluster.getPosition());
                return true;
            }
        });
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.v(TAG, "Data received from " + observable + ", Object:" + o);
        if (observable instanceof VideosModuleObserver || observable instanceof UserDataModuleObservable) {
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
            // Since VideosModuleLikedVideosListResponse is child of VideosModuleVideosListResponse, this check should be
            // done before checking the instance of VideosModuleVideosListResponse. This is because instanceof in Java
            // will return also true for subclasses. This is:
            // - A is Parent
            // - B is Child of A
            // - b is instance of B
            // Then b instanceOf A == true
            //      b instanceOf B == true
            if (response instanceof VideosModuleLikedVideosListResponse) {
                VideosModuleLikedVideosListResponse videosModuleLikedVideosListResponse = (VideosModuleLikedVideosListResponse)response;
                ParseResponse parseResponse = videosModuleLikedVideosListResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    List<Video> favouriteVideosList = videosModuleLikedVideosListResponse.getVideosList();
                    int numberVideoRetrieved = favouriteVideosList == null ? 0 : favouriteVideosList.size();
                    Log.v(TAG, "Retrieved " + numberVideoRetrieved + " favourite videos");
                    mVideosList = new ArrayList<>(favouriteVideosList);
                    mClusterManager.clearItems();
                    mClusterManager.addItems(mVideosList);
                    mClusterManager.cluster();

                    // Prepare action bar
                    mIsShowingFavouriteListVideos = true;
                    mDrawerToggle.setDrawerIndicatorEnabled(false);
                    mActionBar.setDisplayHomeAsUpEnabled(true);
                    mActionBar.setTitle(getString(R.string.main_activity_show_fav_videos_list_title));
                    updateActionBarItems();
                } else {
                    // Some error happend
                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
                }
            } else if (response instanceof VideosModuleVideosListResponse) {
                VideosModuleVideosListResponse videosModuleVideosListResponse = (VideosModuleVideosListResponse) response;
                // If the list of videos received are extra videos to be added to the list of existence videos
                ParseResponse parseResponse = videosModuleVideosListResponse.getParseResponse();
                if (videosModuleVideosListResponse.areExtraVideos()) {
                    if (!parseResponse.isError()) {
                        List<Video> extraVideos = videosModuleVideosListResponse.getVideosList();
                        int numberVideoRetrieved = extraVideos == null ? 0 : extraVideos.size();
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
                        int numberVideoRetrieved = videoList == null ? 0 : videoList.size();
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
            } else if (response instanceof UserDataModuleLikesListResponse) {
                UserDataModuleLikesListResponse userDataModuleLikesListResponse = (UserDataModuleLikesListResponse) response;
                // If the list of videos received are extra videos to be added to the list of existence videos
                ParseResponse parseResponse = userDataModuleLikesListResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    mVideosModule.requestLikedVideosInfo(this, userDataModuleLikesListResponse.getLikesList());
                } else {
                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
                }
            } else if (response instanceof VideosModuleAddAVideoResponse) {
                final VideosModuleAddAVideoResponse videosModuleAddAVideoResponse = (VideosModuleAddAVideoResponse) response;
                final ParseResponse parseResponse = videosModuleAddAVideoResponse.getParseResponse();
                // This is called from the another thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    // Can't create handler inside thread that has not called Looper.prepare()
                        if (!parseResponse.isError()) {
                            Log.v(TAG, "Video added correctly");
                            mNotificationModule.showToast(R.string.add_a_video_activity_video_added_correctly, true);
                            Video video = videosModuleAddAVideoResponse.getVideo();
                            // Add the video to the current list
                            mVideosList.add(video);
                            mClusterManager.addItem(video);
                            mClusterManager.cluster();

                            // Center the map to the video and show the preview
                            showVideoPreview(video);
                        } else {
                            Log.v(TAG, "Error adding the video " + parseResponse.getCode());
                            mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
                        }
                    }
                });
            }

            Log.v(TAG, "Dismissing the loading dialog");
            mNotificationModule.dismissLoadingDialog();

            // 3. Remove the responses
            // Not do anything. Because the list of the response is a stack. Once all the responses has been pop out,
            // there is not need to clean them
        }
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
            showVideoPreview(videoId);
        }

        updateUserProfileIfPossibleAndNeeded();
        updateDrawerItems();
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
     * Update the user profile data in the drawer if possible
     */
    private void updateUserProfileIfPossibleAndNeeded() {
        // If the user data does not exist, exit
        if (!mUserDataModule.hasUserData()) {
            mUserProfileImageView.setImageDrawable(getResources().getDrawable(R.drawable.main_logo));
            mUserNameTextView.setVisibility(View.GONE);
            return;
        }

        UserData userData = mUserDataModule.getUserData();

        // Updating the views. For now it is only possible for google plus users
        if (!userData.isGooglePlusUser()) {
            return;
        }

        // The image is loaded each time the MainActivity is resumed. Since Picasso uses
        // local cache, this shouldn't be any problem
        // Profile photo
        mPicasso.load(userData.getPhotoUrl()).into(mUserProfileImageView);

        // User name
        mUserNameTextView.setText(userData.getName());
        mUserNameTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Update the items in drawer.
     */
    private void updateDrawerItems() {
        if (mUserDataModule.hasUserData()) {
            mDrawerItemLogin.setVisible(false);
            mDrawerItemLogout.setVisible(true);
        } else {
            mDrawerItemLogin.setVisible(true);
            mDrawerItemLogout.setVisible(false);
        }
    }

    /**
     * Center the map to a specific video
     * @param videoId
     */
    private void showVideoPreview(String videoId) {
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

        showVideoPreview(videoToBeCentered);
    }

    /**
     * Show the preview of a specific video
     * @param video
     *      The video to be centered. If it is null, don't do anything
     */
    private void showVideoPreview(Video video) {
        if (video == null) {
            return;
        }

        List<Video> videosListToShow = new ArrayList<Video>();
        videosListToShow.add(video);

        showVideosPreview(videosListToShow, video.getPosition());
    }

    private void showVideosPreview(List<Video> videosList, LatLng position) {
        Log.v(TAG, "Centering the videos on the position " + position);

        // Tracking user action
        mEventTrackingModule.trackUserAction(ScreenId.MAIN_SCREEN, EventId.VIDEOS_PREVIEW, videosList, position);

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
                case R.id.add_video_floating_action_button:
                    // Track user action
                    addVideo();
                    break;
            }
        }
    };

    // Action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        updateActionBarItems();
        return true;
    }

    /**
     * Updates the items in the action bar. This depends if the drawer
     * is open or not
     */
    private void updateActionBarItems() {
        // If the menu is not ready, not do anything
        if (mMenu == null) {
            return;
        }

        if (mIsDrawerOpen || mIsShowingFavouriteListVideos) {
            // Remove the search option
            if (mMenuItemSearch != null) {
                mMenu.removeItem(mMenuItemSearch.getItemId());
            }
        } else {
            // Update user profile
            mMenuItemSearch = mMenu.add(Menu.NONE, MENU_ITEM_SEARCH_ID, Menu
                    .NONE, R.string.action_bar_search)
                    .setIcon(R.drawable.ic_action_search)
                    .setActionView(R.layout.search_layout);
            mMenuItemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                // if it was showing the favourite list videos, disable it and show the list
                // fo all the videos
                if (mIsShowingFavouriteListVideos) {
                    Log.v(TAG, "Favourite list video enabled. Disabling it");
                    mIsShowingFavouriteListVideos = false;
                    updateActionBarItems();
                    mActionBar.setTitle(getString(R.string.app_name));
                    mNotificationModule.showLoadingDialog(mContext);
                    mVideosModule.requestAllVideos(MainActivity.this);

                    // Enable the drawer icons
                    mDrawerToggle.setDrawerIndicatorEnabled(true);
                    return true;
                }
            case MENU_ITEM_SEARCH_ID:
                mEventTrackingModule.trackUserAction(ScreenId.MAIN_SCREEN, EventId.SEARCH_STARTED);
                searchByKeyword();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void searchByKeyword() {
        final SearchView searchActionView = (SearchView) MenuItemCompat.getActionView(mMenuItemSearch);
        searchActionView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.v(TAG, "Searching the videos with the keyword " + query);
                mEventTrackingModule.trackUserAction(ScreenId.MAIN_SCREEN, EventId.SEARCH_BY_KEYWORD, query);
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
                mEventTrackingModule.trackUserAction(ScreenId.MAIN_SCREEN, EventId.SEARCH_FINISHED);
                mNotificationModule.showLoadingDialog(mContext);
                // Retrieve the list of all the videos
                mVideosModule.requestAllVideos(MainActivity.this);
                EditText et = (EditText) findViewById(R.id.search_src_text);
                et.setText("");
                searchActionView.setQuery("", false);
                searchActionView.onActionViewCollapsed();
                mMenuItemSearch.collapseActionView();
            }
        });

        MenuItemCompat.setOnActionExpandListener(mMenuItemSearch, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mEventTrackingModule.trackUserAction(ScreenId.MAIN_SCREEN, EventId.SEARCH_FINISHED);
                mNotificationModule.showLoadingDialog(mContext);
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

    // Drawer
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Start the login activity
     */
    private void launchSignUpLoginActivity() {
        Intent startSignUpLoginActivityIntent = new Intent(mContext, SignUpLoginActivity.class);
        startActivity(startSignUpLoginActivityIntent);
    }

    private void showFavouritesVideosToUser() {
        // Check if the user has logged in
        if (!showAlertIfUserHasNotLoggedIn()) {
            // The user has not logged in. Not do anything
            return;
        }

        mUserDataModule.retrieveFavouriteVideosList(this);
    }

    /**
     * Method used to add a video in the database.
     * if it is the first time the user is doing it, the app shows a small tutorial for the user.
     * If not and the user has set "show tutorial" option as false, then not showing the tutorial.
     *
     * The next step is show the YouTube app and ask the user to share the video.
     *
     * Once the user has came back with the video shared, the app will ask the user to fill the location,
     * the country and the city of the video.
     *
     * // TODO: Automatic fill the country and the city once the user has created the location.
     *
     */
    private void addVideo() {
        // 1. Check if it is the first time the user is doing it or if the user has done it, he has set
        // the flag of show_add_video_tutorial as false
        if (!mUserDataModule.shouldTheAppNotShowAddAVideoTutorial()) {
            // Launch the add video tutorial screen
            Intent startAddAVideoTutorialActivityIntent = new Intent(mContext, AddAVideoTutorialActivity.class);
            startActivity(startAddAVideoTutorialActivityIntent);
            return;
        }

        // Launch YouTube app
        if (!launchYouTubeApp()) {
            mNotificationModule.showToast(R.string.error_message_not_possible_launching_you_tube_app, true);
        }
    }

    /**
     * This method launches the Add a video activity. It parses the intent and send the video id
     * to the activity
     */
    private void launchAddAVideoActivity() {
        Log.v(TAG, "The activity started because user has shared the text " + mIntent);
        if (mIntent == null || !mIntent.hasExtra(Intent.EXTRA_TEXT)) {
            Log.e(TAG, "Error checking text extra for the shared intent. It must be there");
            return;
        }

        String sharedText = mIntent.getStringExtra(Intent.EXTRA_TEXT);
        Log.v(TAG, "Text shared? \"" + sharedText + "\"");

        String videoId = parseVideoId(sharedText);
        if (videoId == null) {
            Log.e(TAG, "The video id is not contained in the shared link. Not do anything");
            return;
        }

        // Start Add a video activity
        Intent startAddAVideoActivityIntent = new Intent(mContext, AddAVideoActivity.class);
        startAddAVideoActivityIntent.putExtra(Video.INTENT_KEY_VIDEO_ID, videoId);
        startActivity(startAddAVideoActivityIntent);
    }

    /**
     * Parse the possible youtube video link and returns the video id
     * TODO: Check the format of the YouTube links
     * http://stackoverflow.com/questions/31776646/what-is-the-format-of-the-video-shared-by-youtube-in-android/31776773#31776773
     * @param youTubeLink
     *      The possible video link from YouTube
     * @return
     *      VideoId if the link belongs to YouTube
     *      Null if the video id is not found
     */
    private String parseVideoId(String youTubeLink) {
        if (!isTheLinkBelongsToYouTube(youTubeLink)) {
            return null;
        }

        // Get the last item. This works for YouTube v10.28.59
        String[] youtubeLinkComponents = youTubeLink.split("/");
        return youtubeLinkComponents[youtubeLinkComponents.length-1];
    }


    /**
     * Check if a specific video belongs to YouTube or not.
     * TODO: The format of the video still need to be checked here:
     * http://stackoverflow.com/questions/31776646/what-is-the-format-of-the-video-shared-by-youtube-in-android/31776773#31776773
     * @param youTubeLink
     *      The link to be checked
     * @return
     *      True if the link belongs to YouTube
     *      False otherwise
     */
    private boolean isTheLinkBelongsToYouTube(String youTubeLink) {
        if (TextUtils.isEmpty(youTubeLink)) {
            return false;
        }

        // Remove all the https or http data
        if (youTubeLink.startsWith("https://")) {
            youTubeLink = youTubeLink.replaceFirst("https://", "");
        }

        if (youTubeLink.startsWith("http://")) {
            youTubeLink = youTubeLink.replaceFirst("http://", "");
        }

        if (youTubeLink.startsWith("www")) {
            youTubeLink = youTubeLink.replaceFirst("www", "");
        }

        return (youTubeLink.startsWith("youtu.be") || youTubeLink.startsWith("youtube."));
    }
}
