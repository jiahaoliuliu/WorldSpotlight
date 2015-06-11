package com.worldspotlightapp.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.internal.BitmapDescriptorParcelable;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class MainActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "MainActivity";

    private ClusterManager<Video> mClusterManager;

    private List<Video> mVideosList;

    // Views
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link the views
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
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Video>() {
            @Override
            public boolean onClusterItemClick(Video video) {
                Log.v(TAG, "Cluster item clicked " + video);
                return true;
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Video>() {
            @Override
            public boolean onClusterClick(Cluster<Video> cluster) {
                Log.v(TAG, "Cluster clicked ");
                for (Video video : cluster.getItems()) {
                    Log.v(TAG, video.toString());
                }
                return true;
            }
        });

    }

    @Override
    public void update(Observable observable, Object o) {
        // TODO: Implement this
    }

    @Override
    protected void processDataIfExists() {
        setupMapIfNeeded();
        if (mVideosList == null) {
            //Retrive element from background
            ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
            query.findInBackground(new FindCallback<Video>() {
                @Override
                public void done(List<Video> videosList, ParseException e) {
                    if (e == null) {
                        Log.v(TAG, "The list of object has been retrieved");
                        mVideosList = videosList;
                        mClusterManager.addItems(mVideosList);
                        // Force it to recluster
                        mClusterManager.cluster();
                        for (Video video: videosList) {
                            Log.v(TAG, video.toString());
                        }
                    } else {
                        Log.e(TAG, "Error retrieving data from backend");
                    }
                }
            });
        }
    }

    private class VideosRenderer extends DefaultClusterRenderer<Video> {

        public VideosRenderer(Context context, GoogleMap map, ClusterManager<Video> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(Video item, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_default_maps_marker));
//            super.onBeforeClusterItemRendered(item, markerOptions);
        }
    }
}
