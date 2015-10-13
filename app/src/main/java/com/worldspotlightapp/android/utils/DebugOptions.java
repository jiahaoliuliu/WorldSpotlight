package com.worldspotlightapp.android.utils;

/**
 * Created by jiahaoliuliu on 15/9/12.
 */
public class DebugOptions {

    public static final boolean IS_PRODUCTION = false;

    // --------------- Print the list of keywords ------------------
    /**
     * Track if all the keywords that comes from the videos should be
     * printed via logs
     */
    private static final boolean SHOULD_PRINT_KEYWORDS = false;

    /**
     * Check if all the keywords that comes from the video should be printed.
     * This is, by each words that exists in the video title and in the
     * video details, count them and sort them by the number of ocurrences
     * among all the videos
     *
     * @return
     *      True if all the keywords should be printed
     *      False otherwise
     */
    public static boolean shouldPrintKeywords() {
        return !IS_PRODUCTION && SHOULD_PRINT_KEYWORDS;
    }

    // ------------------ Update the hash tags of the videos ------------

    /**
     * Track if the list of hashtags of the videos should be updated
     */
    private static final boolean SHOULD_UPDATE_HASH_TAGS_FOR_ALL_THE_VIDEOS = false;

    /**
     * Check if all the hashtags of the videos should be updated depending
     * on the hash tags list
     * @return
     *      True if all the hashtags should be updated
     *      False if not
     */
    public static boolean shouldUpdateHashTagsForAllTheVideos() {
        return !IS_PRODUCTION && SHOULD_UPDATE_HASH_TAGS_FOR_ALL_THE_VIDEOS;
    }

    // ------------------ Update the list of cities ------------

    /**
     * Track if the list of cities should be updated
     */
    private static final boolean SHOULD_UPDATE_CITIES_LIST = false;

    /**
     * Check if all the cities should be updated
     * @return
     *      True if all the cities should be updated
     *      False if not
     */
    public static boolean shouldUpdateCitiesList() {
        return !IS_PRODUCTION && SHOULD_UPDATE_CITIES_LIST;
    }

    // ------------------ Use the production database ------------

    /**
     * Track if the app should use the production data or not
     */
    private static final boolean SHOULD_USE_PRODUCTION_DATA = IS_PRODUCTION;

    /**
     * Check if the app should use the production data
     * @return
     *      True if the app should use the production data
     *      False otherwise
     */
    public static boolean shouldUseProductionData() {
        return !IS_PRODUCTION && SHOULD_USE_PRODUCTION_DATA;
    }


}
