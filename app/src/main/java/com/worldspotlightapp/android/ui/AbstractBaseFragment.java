package com.worldspotlightapp.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.interfaces.IOnActionBarRequestListener;
import com.worldspotlightapp.android.maincontroller.MainController;
import com.worldspotlightapp.android.maincontroller.modules.activitytrackermodule.IActivityTrackerModule;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.AbstractCityModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule;
import com.worldspotlightapp.android.maincontroller.modules.gpslocalizationmodule.IGpsLocalizationModule;
import com.worldspotlightapp.android.maincontroller.modules.notificationmodule.INotificationModule;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.AbstractUserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.AbstractVideosModuleObservable;

/**
 *
 * Abstract base fragment is the class used to communicate with Activities. By default it gets the list
 * of modules from the activity, which should implements IOnMainControllerInstantiatedListener
 * Created by jiahaoliuliu on 15/8/15.
 */
public abstract class AbstractBaseFragment extends Fragment {

    private static final String TAG = "AbstractBaseFragment";

    protected Activity mAttachedActivity;
    protected boolean mIsActivityCreated;

    // Modules
    protected INotificationModule mNotificationModule;
    protected AbstractUserDataModuleObservable mUserDataModule;
    protected IGpsLocalizationModule mGpsLocalizationModule;
    protected AbstractVideosModuleObservable mVideosModule;
    protected IEventsTrackingModule mEventTrackingModule;
    protected IActivityTrackerModule mActivityTrackerModule;
    protected AbstractCityModuleObservable mCityModuleObservable;

    // Listener
    private MainController.IOnMainControllerInstantiatedListener mOnMainControllerInstantiatedListener;
    private IOnActionBarRequestListener mOnActionBarRequestListener;
    protected ActionBar mActionBar;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.mAttachedActivity = activity;

        try {
            mOnMainControllerInstantiatedListener = (MainController.IOnMainControllerInstantiatedListener) activity;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException("The attached activity must implements IOnMainControllerInstantiatedListener");
        }

        try {
            mOnActionBarRequestListener = (IOnActionBarRequestListener) activity;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException("The attached activity must implements IOnActionBarRequestListener");
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mIsActivityCreated = true;

        // Get the action bar
        mActionBar = mOnActionBarRequestListener.getActionBarFromActivity();

        // Get each one of the modules
        mNotificationModule = mOnMainControllerInstantiatedListener.getNotificationModule();
        mUserDataModule = mOnMainControllerInstantiatedListener.getUserDataModule();
        mGpsLocalizationModule = mOnMainControllerInstantiatedListener.getGpsLocalizationModule();
        mVideosModule = mOnMainControllerInstantiatedListener.getVideosModule();
        mEventTrackingModule = mOnMainControllerInstantiatedListener.getEventTrackingModule();
        mActivityTrackerModule = mOnMainControllerInstantiatedListener.getActivityTrackerModule();
        mCityModuleObservable = mOnMainControllerInstantiatedListener.getCityModuleObservable();
    }

    /**
     * Invoking the same method in the attached activity
     * @param message
     * @return
     */
    protected boolean showAlertIfUserHasNotLoggedIn(String message) {
        return ((AbstractBaseActivity)mAttachedActivity).showAlertIfUserHasNotLoggedIn(message);
    }

    protected boolean showAlertIfUserHasNotLoggedIn() {
        return ((AbstractBaseActivity)mAttachedActivity).showAlertIfUserHasNotLoggedIn();
    }
}
