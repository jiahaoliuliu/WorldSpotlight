package com.worldspotlightapp.android.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Class used to store the list of hashtags for each one of the
 * videos
 *
 * Created by jiahaoliuliu on 15/8/29.
 */
@ParseClassName("HashTag")
public class HashTag extends ParseObject{

    // The name of the hash tag
    public static final String PARSE_TABLE_COLUMN_HASH_TAG = "name";

    // Empty constructor for Parse
    public HashTag() {
    }

    public HashTag(String name) {
        super();
        put(PARSE_TABLE_COLUMN_HASH_TAG, name);
    }

    public String getName() {
        return getString(PARSE_TABLE_COLUMN_HASH_TAG);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (!(o instanceof Report)) {
            return false;
        }

        Report that = (Report) o;

        // Check the user id
        if (getName() != null ?
                !getName().equals(that.getUserId()) : that.getUserId() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "Report{" +
                "ObjectId='" + getObjectId() + '\'' +
                ", Name='" + getName() + '\'' +
                "}";
    }
}
