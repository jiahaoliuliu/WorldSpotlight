package com.worldspotlightapp.android.maincontroller.modules.activitytrackermodule;

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
public interface IActivityTrackerModule {

    /**
     * Be notified that a new activity has been created.
     */
    public void notifyActivityCreated();

    /**
     * Be notified that an activity has been destroyed
     */
    public void notifyActivityDestroyed();

    /**
     * Check if there is only one activity running or not
     * @return True if the number of activities is 1
     *         False otherwise
     */
    public boolean isThereOnlyOneActivityRunning();

    /**
     * Check if there is any activity running
     *
     * @return True if the number of activity running is bigger than zero
     *         False if there is zero activity running
     */
    public boolean isThereAnyActivityRunning();

}
