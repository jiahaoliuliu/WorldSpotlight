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
    public static final boolean SHOULD_PRINT_KEYWORDS = false;

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
    public static final boolean SHOULD_UPDATE_HASH_TAGS_FOR_ALL_THE_VIDEOS = false;

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

}
