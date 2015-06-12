package com.worldspotlightapp.android.maincontroller.modules.activitytrackermodule;

import android.util.Log;

/**
 * This activity keeps track of the state of all the activities of the project
 *
 * TODO: The reliability of this module is not perfect, because it lies on that when Android closes
 * an app, it kills all the activities, so, the method onDestroy of the activities will be called.
 * This is not true, sometimes Android kills an activity without calling onDestroy. The unique
 * method which is guaranteed to be called is onPause.
 *
 * @author jliu
 *
 */
public class ActivityTrackerModule implements IActivityTrackerModule {

    private static final String LOG_TAG = "ActivityTrackerModule";

    private int mNumberActivities;

    @Override
    public void notifyActivityCreated() {
        mNumberActivities++;
        Log.d(LOG_TAG, "New activity created. The total number of activities is " + mNumberActivities);
    }

    @Override
    public void notifyActivityDestroyed() {
        mNumberActivities--;
        Log.d(LOG_TAG, "activity destroyed. The total number of activities is " + mNumberActivities);
    }

    @Override
    public boolean isThereOnlyOneActivityRunning() {
        return mNumberActivities == 1;
    }

    @Override
    public boolean isThereAnyActivityRunning() {
        return mNumberActivities > 0;
    }

}
