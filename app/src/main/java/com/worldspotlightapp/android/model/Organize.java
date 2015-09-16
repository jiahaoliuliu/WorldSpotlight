package com.worldspotlightapp.android.model;

import android.text.TextUtils;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Relation model for Organization.
 *
 * This keeps the record for a specific organizer in a city
 *
 * Created by jiahaoliuliu on 15/9/8.
 */
@ParseClassName("Organize")
public class Organize extends ParseObject {

    // City
    public static final String PARSE_COLUMN_CITY = "city";

    // Organizer
    public static final String PARSE_COLUMN_ORGANIZER = "organizer";

    public Organize() {
        super();
    }

    // Object id
    public boolean hasObjectId() {
        return !TextUtils.isEmpty(getObjectId());
    }

    // TODO: Test this method
    public City getCity() {
        return (City)get(PARSE_COLUMN_CITY);
    }

    // TODO: Test this method
    public Organizer getOrganizer() {
        return (Organizer)get(PARSE_COLUMN_ORGANIZER);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Organize anotherOrganize = (Organize) o;

        // Check if the object id exists.
        if (anotherOrganize.hasObjectId()) {
            return getObjectId().equals(anotherOrganize.getObjectId());
            // If the object id does not exists, check the video id
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = getCity().hashCode();
        result = 31 * result + (getOrganizer().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Organize{" +
                "objectId='" + getObjectId() + "\'" +
                "city='" + getCity() + '\'' +
                "organizer='" + getOrganizer() + '\'' +
                '}';
    }
}
