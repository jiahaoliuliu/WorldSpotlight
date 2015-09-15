package com.worldspotlightapp.android.maincontroller;

import android.content.Context;

import com.parse.ParseException;
import com.worldspotlightapp.android.maincontroller.database.VideoDataLayer;
import com.worldspotlightapp.android.maincontroller.modules.activitytrackermodule.ActivityTrackerModule;
import com.worldspotlightapp.android.maincontroller.modules.activitytrackermodule.IActivityTrackerModule;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.AbstractCityModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.CityModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.EventsTrackingModule;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule;
import com.worldspotlightapp.android.maincontroller.modules.gpslocalizationmodule.GpsLocalizationModule;
import com.worldspotlightapp.android.maincontroller.modules.gpslocalizationmodule.IGpsLocalizationModule;
import com.worldspotlightapp.android.maincontroller.modules.notificationmodule.INotificationModule;
import com.worldspotlightapp.android.maincontroller.modules.notificationmodule.NotificationModule;
import com.worldspotlightapp.android.maincontroller.modules.organizermodule.AbstractOrganizerModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.organizermodule.OrganizerModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.AbstractUserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.UserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.AbstractVideosModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.VideosModuleObserver;

import java.lang.ref.SoftReference;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Session class models a user's session. It is the intermediate level between Controllers and
 * Service. This class implements observable, which is used to notify all the class which is using
 * the list of offers that it has been changed
 */
public final class MainController {

    private Preferences preferences;

    private static MainController currentMainController = null;

    // Modules
    private ExecutorServiceHolder mExecutorServiceHolder;
    private INotificationModule mNotificationModule;
    private AbstractUserDataModuleObservable mUserDataModule;
    private IGpsLocalizationModule mGpsLocalizationModule;
    private AbstractVideosModuleObservable mVideosModuleObservable;
    private IEventsTrackingModule mEventTrackingModule;
    private IActivityTrackerModule mActivityTrackerModule;
    private AbstractCityModuleObservable mCityModuleObservable;
    private AbstractOrganizerModuleObservable mOrganizerModuleObservable;

    /**
     * The constructor of the session. Because it is a singleton, there is not parameters for the
     * constructors and it's private
     */
    private MainController() {}

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first
     * access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final MainController INSTANCE = new MainController();
    }

    // It is synchronized to avoid problems with multithreading
    // Once get, it must initialize the service and the preferences based on the context
    private static synchronized MainController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // To avoid clone problem
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Get the current session.
     * 
     * @param context
     *            The context utilized to retrieve the data
     * @return The current session
     */
    public static synchronized MainController getCurrentInstance(Context context) {
        if (MainController.currentMainController == null) {
            MainController.sessionFromCurrentSession(context);
        }

        return MainController.currentMainController;
    }

    /**
     * Creates a new session from the data saved in the persistent data storage.
     * 
     * @param context
     *            The context utilized.
     */
    private static void sessionFromCurrentSession(final Context context) {
        Preferences preferences = new Preferences(context);

        final MainController newMainController = MainController.getInstance();
        newMainController.setPreferences(preferences);

        // Create the executor pool
        newMainController.mExecutorServiceHolder = new ExecutorServiceHolder();

        // Set the modules
        newMainController.mUserDataModule = new UserDataModuleObservable(preferences);
        UUID uuid = newMainController.mUserDataModule.getUuid();
        newMainController.mNotificationModule = new NotificationModule(context);
        newMainController.mGpsLocalizationModule = new GpsLocalizationModule(context, preferences);
        newMainController.mVideosModuleObservable = new VideosModuleObserver(context,
                newMainController.mExecutorServiceHolder.createExecutorService(), new VideoDataLayer(), preferences);
        newMainController.mEventTrackingModule = new EventsTrackingModule(context, uuid);
        newMainController.mActivityTrackerModule = new ActivityTrackerModule();
        newMainController.mCityModuleObservable = new CityModuleObservable();
        newMainController.mOrganizerModuleObservable = new OrganizerModuleObserver();

        // Save the current session
        MainController.setCurrentMainController(newMainController);
    }

    // Getters & setters
    private static synchronized void setCurrentMainController(MainController mainController) {
        MainController.currentMainController = mainController;
    }

    // =========================================== Basic methods ==============================

    private Preferences getPreferences() {
        return preferences;
    }

    /**
     * Set the preferences as the preferences utilized for the session. This is private to prevent
     * other to set the preferences from outside The preferences won't be set until the user has
     * logged in.
     *
     * @param preferences
     *            The preferences to set.
     */
    private void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public INotificationModule getNotificationModule() {
        return mNotificationModule;
    }

    public AbstractUserDataModuleObservable getUserDataModule() {
        return mUserDataModule;
    }

    public IGpsLocalizationModule getGpsLocalizationModule() {
        return mGpsLocalizationModule;
    }

    public AbstractVideosModuleObservable getVideosModule() {
        return mVideosModuleObservable;
    }

    public IEventsTrackingModule getEventTrackingModule() {
        return mEventTrackingModule;
    }

    public IActivityTrackerModule getActivityTrackerModule() {
        return mActivityTrackerModule;
    }

    public AbstractCityModuleObservable getCityModuleObservable() {
        return mCityModuleObservable;
    }

    public AbstractOrganizerModuleObservable getOrganizerModuleObservable() {
        return mOrganizerModuleObservable;
    }

    /**
     * The class which creates the threadPool
     */
    public static final class ExecutorServiceHolder {

        /**
         * The number of the threads which are running in parallel.
         */
        private static final int MAXIMUM_NUM_RUNNING_THREAD = 5;

        /**
         * Creates the executor server as soft reference.
         */
        private SoftReference<ExecutorService> executorServiceReference = new SoftReference<ExecutorService>(
                createExecutorService());

        /**
         * Method used to get the executor service.
         * @return The executor service. Create it if it has not been created before
         */
        ExecutorService getExecutorService() {
            ExecutorService executorService = executorServiceReference.get();

            if (executorService == null) {
                // (the reference was cleared)
                executorService = createExecutorService();
                executorServiceReference = new SoftReference<ExecutorService>(executorService);
            }

            return executorService;
        }

        /**
         * The method which creates a threadPool with limit number of threads.
         * Those threads will be re-utilized and they should exist while the application is running.
         * @return A threadPool with always same number of threads
         */
        private ExecutorService createExecutorService() {
            return Executors.newFixedThreadPool(MAXIMUM_NUM_RUNNING_THREAD);
        }
    }

    public interface IOnMainControllerInstantiatedListener {

        public INotificationModule getNotificationModule();

        public AbstractUserDataModuleObservable getUserDataModule();

        public IGpsLocalizationModule getGpsLocalizationModule();

        public AbstractVideosModuleObservable getVideosModule();

        public IEventsTrackingModule getEventTrackingModule();

        public IActivityTrackerModule getActivityTrackerModule();

        public AbstractCityModuleObservable getCityModuleObservable();

        public AbstractOrganizerModuleObservable getOrganizerModuleObservable();
    }
}
