package com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public interface IEventsTrackingModule {

    enum ScreenId {
        LOGIN_SCREEN, MAIN_SCREEN, VIDEO_DETAILS_SCREEN;
    }

    enum EventId {
        // Login screen
        LOGIN_WITH_FACEBOOK, LOGIN_WITH_GOOGLE_PLUS, SIGN_UP_WITH_PARSE, LOGIN_WITH_PARSE, SKIP_LOGIN,

        // Main screen
        //      SEARCH
        SEARCH_STARTED, SEARCH_FINISHED, SEARCH_BY_KEYWORD,

        //      Self localization
        USER_LOCALIZED,

        //      Videos Preview
        VIDEOS_PREVIEW, VIDEO_PREVIEW_CLICK,

        //      Drawer
        LOGIN, FAVOURITES, LOGOUT,

        // Video details screen
        REPORT_A_VIDEO, LIKE_A_VIDEO, FULL_SCREEN, SHARE;

    }

    /**
     * Track the user action event. Note there could be a list of objects as parameters
     * @param screenId
     *      The id of the screen. See {@link com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.ScreenId}
     * @param eventId
     *      The id of the Event. See {@link com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.EventId}
     * @param objects
     *      Optionally the event is associated with a set of objects. It depends on the screen id and the event id.
     *
     *      This is the list
     *      ScreenId            EventId             Objects
     *      --------------------------------------------------------
     *      MAIN_SCREEN         SELECT_DISH         DishSimple
     *
     */
    void trackUserAction(ScreenId screenId, EventId eventId, Object... objects);

    /**
     * Track the app has been initialized
     */
    void trackAppInitialization();

    /**
     * Track the app has been finalized
     */
    void trackAppFinalization();
}
