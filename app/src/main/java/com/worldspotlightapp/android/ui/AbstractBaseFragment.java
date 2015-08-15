package com.worldspotlightapp.android.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

import com.worldspotlightapp.android.maincontroller.MainController;
import com.worldspotlightapp.android.maincontroller.modules.activitytrackermodule.IActivityTrackerModule;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule;
import com.worldspotlightapp.android.maincontroller.modules.gpslocalizationmodule.IGpsLocalizationModule;
import com.worldspotlightapp.android.maincontroller.modules.notificationmodule.INotificationModule;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.AbstractUserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.AbstractVideosModuleObservable;

/**
 * Created by jiahaoliuliu on 15/8/15.
 */
public abstract class AbstractBaseFragment extends Fragment {

    private static final String TAG = "AbstractBaseFragment";

    protected Activity mAttachedActivity;
    protected ActionBar mActionBar;

    // Modules
    protected INotificationModule mNotificationModule;
    protected AbstractUserDataModuleObservable mUserDataModule;
    protected IGpsLocalizationModule mGpsLocalizationModule;
    protected AbstractVideosModuleObservable mVideosModule;
    protected IEventsTrackingModule mEventTrackingModule;
    protected IActivityTrackerModule mActivityTrackerModule;

    // Listener
    private MainController.IOnMainControllerInstantiatedListener mOnMainControllerInstantiatedListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.mAttachedActivity = activity;

        try {
            mOnMainControllerInstantiatedListener = (MainController.IOnMainControllerInstantiatedListener) activity;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException("The attached activity must implements IOnMainControllerInstantiatedListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get each one of the modules
        mNotificationModule = mOnMainControllerInstantiatedListener.getNotificationModule();
        mUserDataModule = mOnMainControllerInstantiatedListener.getUserDataModule();
        mGpsLocalizationModule = mOnMainControllerInstantiatedListener.getGpsLocalizationModule();
        mVideosModule = mOnMainControllerInstantiatedListener.getVideosModule();
        mEventTrackingModule = mOnMainControllerInstantiatedListener.getEventTrackingModule();
        mActivityTrackerModule = mOnMainControllerInstantiatedListener.getActivityTrackerModule();
    }
}
