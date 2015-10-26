package com.worldspotlightapp.android.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * This class is used to allow the user send feedback to the company
 *
 * Created by jiahaoliuliu on 7/16/15.
 */
@ParseClassName("Feedback")
public class Feedback extends ParseObject {

    // Feedback
    public static final String PARSE_TABLE_COLUMN_FEEDBACK = "feedback";

    public Feedback(){};

    public Feedback(String feedback) {
        super();
        put(PARSE_TABLE_COLUMN_FEEDBACK, feedback);
    }

    public String getFeedback() {
        return getString(PARSE_TABLE_COLUMN_FEEDBACK);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (!(o instanceof Feedback)) {
            return false;
        }

        Feedback that = (Feedback) o;

        // Check the feedback
        if (getFeedback() != null ?
                !getFeedback().equals(that.getFeedback()) : that.getFeedback() != null) {
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
        return "Likes{" +
                "ObjectId='" + getObjectId() + '\'' +
                ", Feedback='" + getFeedback() + '\'' +
                "}";
    }
}
