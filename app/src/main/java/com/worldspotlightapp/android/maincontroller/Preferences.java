package com.worldspotlightapp.android.maincontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.HashSet;
import java.util.List;

/**
 * Class utilized to save the user elemental data persistently.
 * 
 * @version 1.0 Added Remove method for all the primitive types
 * 
 * @author Jiahao Liu
 */
public class Preferences {

    // The id of the booleans
    // Update the method clearUserData when modified
    public enum BooleanId {

        // The default value of the boolean id
        DEFAULT_BOOLEAN_ID;

        public static BooleanId toBooleanId(String booleanId) {
            try {
                return valueOf(booleanId);
            } catch (Exception ex) {
                return DEFAULT_BOOLEAN_ID;
            }
        }
    }

    // The id of the strings
    // Update the method clearUserData when modified
    public enum StringId {
        // The universal unique id of the device
        UUID,

        // The default value of the string id
        DEFAULT_STRING_ID;

        public static StringId toStringId(String stringId) {
            try {
                return valueOf(stringId);
            } catch (Exception ex) {
                return DEFAULT_STRING_ID;
            }
        }
    }

    public enum IntId {
        // The default id
        DEFAULT_INT_ID;
        public static IntId toIntId(String intId) {
            try {
                return valueOf(intId);
            } catch (Exception ex) {
                return DEFAULT_INT_ID;
            }
        }
    }

    public enum DoubleId {

        LAST_KNOWN_LOCATION_LATITUDE, LAST_KNOWN_LOCATION_LONGITUDE,

        // The default id
        DEFAULT_DOUBLE_ID;

        public static DoubleId toDoubleId(String doubleId) {
            try {
                return valueOf(doubleId);
            } catch (Exception ex) {
                return DEFAULT_DOUBLE_ID;
            }
        }
    }

    // The id of the long
    // It is used mainly to store the date
    public enum LongId {
        // The default value of the boolean id
        DEFAULT_LONG_ID;

        public static LongId toLongId(String longId) {
            try {
                return valueOf(longId);
            } catch (Exception ex) {
                return DEFAULT_LONG_ID;
            }
        }

    }

    // The id of the list Strings
    // Update the method clearUserData when modified
    public enum ListStringId {
        // The default value of the list string id
        DEFAULT_LIST_STRING_ID;
        public static ListStringId toListStringId(String listStringId) {
            try {
                return valueOf(listStringId);
            } catch (Exception ex) {
                return DEFAULT_LIST_STRING_ID;
            }
        }
    }

    public enum HashMapIntegerId {
        DEFAULT_HASH_MAP_INTEGER_ID;
        public static HashMapIntegerId toHashMahIntegerId(String hashMapIntegerId) {
            try {
                return valueOf(hashMapIntegerId);
            } catch (Exception ex) {
                return DEFAULT_HASH_MAP_INTEGER_ID;
            }
        }
    }

    public enum HashMapListStringId {
        DEFAULT_HASH_MAP_LIST_STRING_ID;
        public static HashMapListStringId toHashMapListStringId(String hashMapListStringId) {
            try {
                return valueOf(hashMapListStringId);
            } catch (Exception ex) {
                return DEFAULT_HASH_MAP_LIST_STRING_ID;
            }
        }
    }

    /**
     * The tag utilized for the log.
     */
    private static final String LOG_TAG = Preferences.class.getSimpleName();

    /**
     * The name of the file utilized to store the data.
     */
    private static final String FILE_NAME = "BuyIt.Preferences";

    // The default values
    private static final boolean DEFAULT_BOOLEAN = false;
    private static final String DEFAULT_STRING = null;

    // It doesn't matter what value has been set, it will never be used
    private static final Integer DEFAULT_INT = -1;

    // It doesn't matter what value has been set, it will never be used
    // Because the double is saved as long.
    private static final Double DEFAULT_DOULBE = -1.0;

    // It doesn't matter what value has been set, it will never be used.
    private static final Long DEFAULT_LONG = Long.valueOf(-1);

    // The default data for static set
    private static final HashSet<String> DEFAULT_HASH_SET = null;

    private static final List<String> DEFAULT_LIST_STRING = null;

    private static final String SEPARATOR = "_";

    /**
     * The context passed by any Android's component.
     */
    private final Context context;

    /**
     * The shared preferences to save/restore the data.
     */
    private final SharedPreferences sharedPreferences;

    /**
     * The editor to save the data.
     */
    private final SharedPreferences.Editor editor;

    /**
     * The main constructor.
     * 
     * @param context
     *            The context passed by any Android's component.
     */
    Preferences(Context context) {
        this.context = context;

        // The user shared preferences
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE
                | Context.MODE_MULTI_PROCESS);
        editor = sharedPreferences.edit();

    }

    // =========================================== public methods ==============================
    // Boolean
    /**
     * Get the data from shared preference
     * 
     * @param booleanId
     *            The id of the data to get
     * @return The data if it has been saved false otherwise
     */
    public boolean get(BooleanId booleanId) {
        boolean bool = sharedPreferences.getBoolean(booleanId.toString(), DEFAULT_BOOLEAN);
        return bool;

    }

    public void set(BooleanId booleanId, boolean bool) {
        // The data will be set if it is not the default one
        if (booleanId != BooleanId.DEFAULT_BOOLEAN_ID) {
            editor.putBoolean(booleanId.toString(), bool);
            editor.commit();
        }
    }

    public boolean contains(BooleanId booleanId) {
        return sharedPreferences.contains(booleanId.toString());
    }

    public void remove(BooleanId booleanId) {
        if (booleanId != BooleanId.DEFAULT_BOOLEAN_ID) {
            editor.remove(booleanId.toString());
            editor.commit();
        }
    }

    // String
    public String get(StringId stringId) {
        String string = sharedPreferences.getString(stringId.toString(), DEFAULT_STRING);
        return string;

    }

    /**
     * Set the content of a string id. If the value to be set is equal to null or empty, the value
     * of the key will be removed from shared preferences
     * 
     * @param stringId
     *            The key of the string to be set
     * @param string
     *            The value to be set.
     */
    public void set(StringId stringId, String string) {
        // The data will be set if the id is not the default one
        if (stringId != StringId.DEFAULT_STRING_ID) {
            if (TextUtils.isEmpty(string) && sharedPreferences.contains(stringId.toString())) {
                editor.remove(stringId.toString());
            } else {
                editor.putString(stringId.toString(), string);
            }
            editor.commit();
        }
    }

    public boolean contains(StringId stringId) {
        return sharedPreferences.contains(stringId.toString());
    }

    public void remove(StringId stringId) {
        if (stringId != StringId.DEFAULT_STRING_ID) {
            editor.remove(stringId.toString());
            editor.commit();
        }
    }

    // Integer
    /**
     * Get the data in the shared preferences. If it is not set, return null
     * 
     * @param intId
     *            The id of the data saved
     * @return The data if it has been saved null otherwise
     */
    public Integer get(IntId intId) {
        if (sharedPreferences.contains(intId.toString())) {
            return sharedPreferences.getInt(intId.toString(), DEFAULT_INT);
        } else {
            return null;
        }
    }

    public void set(IntId intId, Integer integer) {
        // The data will be set if the id is not the default one
        if (intId != IntId.DEFAULT_INT_ID) {
            editor.putInt(intId.toString(), integer);
            editor.commit();
        }
    }

    public boolean contains(IntId intId) {
        return sharedPreferences.contains(intId.toString());
    }

    public void remove(IntId intId) {
        if (intId != IntId.DEFAULT_INT_ID) {
            editor.remove(intId.toString());
            editor.commit();
        }
    }

    // Double
    /**
     * Get the data in the shared preferences. If it is not set, return null
     * 
     * @param doubleId
     *            The id of the data saved
     * @return The data if it has been saved null otherwise
     */
    public Double get(DoubleId doubleId) {
        if (sharedPreferences.contains(doubleId.toString())) {
            return Double.longBitsToDouble(sharedPreferences.getLong(doubleId.toString(),
                    DEFAULT_LONG));
        } else {
            return null;
        }
    }

    public void set(DoubleId doubleId, double doubleData) {
        // The data will be set if the id is not the default one
        if (doubleId != DoubleId.DEFAULT_DOUBLE_ID) {
            editor.putLong(doubleId.toString(), Double.doubleToRawLongBits(doubleData));
            editor.commit();
        }
    }

    public boolean contains(DoubleId doubleId) {
        return sharedPreferences.contains(doubleId.toString());
    }

    public void remove(DoubleId doubleId) {
        if (doubleId != DoubleId.DEFAULT_DOUBLE_ID) {
            editor.remove(doubleId.toString());
            editor.commit();
        }
    }

    // Long
    /**
     * Get the data saved in the shared preferences.
     * 
     * @param longId
     *            The id of the data saved
     * @return The data if it has been set null otherwise
     */
    public Long get(LongId longId) {
        if (sharedPreferences.contains(longId.toString())) {
            return sharedPreferences.getLong(longId.toString(), DEFAULT_LONG);
        } else {
            return null;
        }
    }

    public void set(LongId longId, Long longData) {
        // The data will be set if it is not the default one
        if (longId != LongId.DEFAULT_LONG_ID) {
            editor.putLong(longId.toString(), longData);
            editor.commit();
        }
    }

    public boolean contains(LongId longId) {
        return sharedPreferences.contains(longId.toString());
    }

    public void remove(LongId longId) {
        if (longId != LongId.DEFAULT_LONG_ID) {
            editor.remove(longId.toString());
            editor.commit();
        }
    }

    /**
     * Remove all the content of the shared preferences
     */
    public void clearAll() {
        editor.clear();
        editor.commit();
    }
}
