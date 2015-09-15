package com.worldspotlightapp.android.model;

import android.text.TextUtils;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by jiahaoliuliu on 15/9/7.
 */
@ParseClassName("Organizer")
public class Organizer extends ParseObject {

    private static final String TAG = "Organizer";

    // Object Id
    public static final String INTENT_KEY_OBJECT_ID = "com.worldspotlightapp.android.model.Organizer.objectId";
    public static final String PARSE_COLUMN_OBJECT_ID = "objectId";

    // Name
    public static final String PARSE_COLUMN_NAME = "name";

    // Address
    public static final String PARSE_COLUMN_ADDRESS = "address";

    // City
    public static final String PARSE_COLUMN_CITY = "city";

    // Country
    public static final String PARSE_COLUMN_COUNTRY = "country";

    // Description
    public static final String PARSE_COLUMN_DESCRIPTION = "description";

    // Phone number 1
    public static final String PARSE_COLUMN_PHONE_NUMBER_1 = "phoneNumber1";

    // Phone number 2
    public static final String PARSE_COLUMN_PHONE_NUMBER_2 = "phoneNumber2";

    // Phone number 3
    public static final String PARSE_COLUMN_PHONE_NUMBER_3 = "phoneNumber3";

    // Web page
    public static final String PARSE_COLUMN_WEB_PAGE = "webPage";

    // Mail address
    public static final String PARSE_COLUMN_MAIL_ADDRESS = "mailAddress";

    // Logo
    public static final String PARSE_COLUMN_LOGO = "logo";

    public Organizer() {
        super();
    }

    // Name
    public boolean hasName() {
        return !TextUtils.isEmpty(getName());
    }

    public String getName() {
        return getString(PARSE_COLUMN_NAME);
    }

    // Description
    public boolean hasDescription() {
        return !TextUtils.isEmpty(getDescription());
    }

    public String getDescription() {
        return getString(PARSE_COLUMN_DESCRIPTION);
    }

    // Address
    public boolean hasAddress() {
        return !TextUtils.isEmpty(getAddress());
    }

    public String getAddress() {
        return getString(PARSE_COLUMN_ADDRESS);
    }

    // City
    public boolean hasCity() {
        return !TextUtils.isEmpty(getCity());
    }

    public String getCity() {
        return getString(PARSE_COLUMN_CITY);
    }

    // Country
    public boolean hasCountry() {
        return !TextUtils.isEmpty(getCountry());
    }

    public String getCountry() {
        return getString(PARSE_COLUMN_COUNTRY);
    }

    // Phone numbers
    public boolean hasPhoneNumbers() {
        return hasPhoneNumber1() ||
                hasPhoneNumber2() ||
                hasPhoneNumber3();
    }

    // Phone number 1
    public boolean hasPhoneNumber1() {
        return !TextUtils.isEmpty(getPhoneNumber1());
    }

    public String getPhoneNumber1() {
        return getString(PARSE_COLUMN_PHONE_NUMBER_1);
    }

    // Phone number 2
    public boolean hasPhoneNumber2() {
        return !TextUtils.isEmpty(getPhoneNumber2());
    }

    public String getPhoneNumber2() {
        return getString(PARSE_COLUMN_PHONE_NUMBER_2);
    }

    private void setPhoneNumber2(String phoneNumber2) {
        if (TextUtils.isEmpty(phoneNumber2)) {
            Log.e(TAG, "Trying to set the phone number 2 with empty string");
            return;
        }

        put(PARSE_COLUMN_PHONE_NUMBER_2, phoneNumber2);
    }

    // Phone number 3
    public boolean hasPhoneNumber3() {
        return !TextUtils.isEmpty(getPhoneNumber3());
    }

    private void setPhoneNumber3(String phoneNumber3) {
        if (TextUtils.isEmpty(phoneNumber3)) {
            Log.e(TAG, "Trying to set the phone number 3 with empty string");
            return;
        }

        put(PARSE_COLUMN_PHONE_NUMBER_3, phoneNumber3);
    }

    public String getPhoneNumber3() {
        return getString(PARSE_COLUMN_PHONE_NUMBER_3);
    }

    // Web page
    public boolean hasWebPage() {
        return !TextUtils.isEmpty(getWebPage());
    }

    public String getWebPage() {
        return getString(PARSE_COLUMN_WEB_PAGE);
    }

    // Mail address
    public boolean hasMailAddress() {
        return !TextUtils.isEmpty(getMailAddress());
    }

    public String getMailAddress() {
        return getString(PARSE_COLUMN_MAIL_ADDRESS);
    }

    // Logo
    public boolean hasLogoUrl() {
        return has(PARSE_COLUMN_LOGO);
    }

    public String getLogoUrl() {
        if (has(PARSE_COLUMN_LOGO)) {
            return getParseFile(PARSE_COLUMN_LOGO).getUrl();
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Organizer anotherOrganizer = (Organizer) o;

        // Check if the object id exists.
        if (anotherOrganizer.has(PARSE_COLUMN_OBJECT_ID)) {
            return getObjectId().equals(anotherOrganizer.getObjectId());
            // If the object id does not exists, check the video id
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 31 * getObjectId().hashCode();
        result = 31 * result + (getName().hashCode());

        // Other fields could not exists
        return result;
    }

    @Override
    public String toString() {
        return "Organizer {" +
                "objectId='" + getObjectId() + "\'" +
                "name='" + getName() + '\'' +
                "address='" + getAddress() + '\'' +
                "city='" + getCity() + '\'' +
                "country='" + getCountry() + '\'' +
                "description='" + getDescription() + '\'' +
                "phone number 1 ='" + getPhoneNumber1() + '\'' +
                "phone number 2 ='" + getPhoneNumber2() + '\'' +
                "phone number 3 ='" + getPhoneNumber3() + '\'' +
                "web page ='" + getWebPage() + '\'' +
                "mail address ='" + getMailAddress() + '\'' +
                "logo url ='" + getLogoUrl() + '\'' +
                '}';
    }
}
