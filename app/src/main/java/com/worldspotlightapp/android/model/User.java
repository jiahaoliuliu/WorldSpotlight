package com.worldspotlightapp.android.model;

import com.parse.ParseClassName;
import com.parse.ParseUser;

/**
 * Created by jiahaoliuliu on 7/4/15.
 */
@ParseClassName("_User")
public class User extends ParseUser {

    /**
     * The name of the user
     */
    public static final String PARSE_TABLE_COLUMN_NAME = "name";

    public static final String PARSE_TABLE_COLUMN_PHOTO_URL = "photoUrl";

    public static final String PARSE_TABLE_COLUMN_IS_GOOGLE_PLUS_USER = "isGooglePlusUser";

    public static final String PARSE_TABLE_COLUMN_PROFILE_URL = "profileUrl";

    /**
     * The empty constructor required by Parse
     */
    public User(){
        super();
    }

    public User(String name, String username, String email, String password, String photoUrl,
                boolean isGooglePlusUser, String profileUrl) {
        super();
        setName(name);
        setUsername(username);
        setEmail(email);
        setPassword(password);
        setPhotoUrl(photoUrl);
        setIsGooglePlusUser(isGooglePlusUser);
        setProfileUrl(profileUrl);
    }

    public User(ParseUser parseUser) {
        super();
        setName(parseUser.getString(PARSE_TABLE_COLUMN_NAME));
        setPhotoUrl(parseUser.getString(PARSE_TABLE_COLUMN_PHOTO_URL));
        setIsGooglePlusUser(parseUser.getBoolean(PARSE_TABLE_COLUMN_IS_GOOGLE_PLUS_USER));
        setProfileUrl(parseUser.getString(PARSE_TABLE_COLUMN_PROFILE_URL));
    }

    // Name
    public String getName() {
        return getString(PARSE_TABLE_COLUMN_NAME);
    }

    /**
     * Set the name of the user
     * This method should remain private to not allow it to be modified
     * other place than the constructors
     * @param name
     *      The name of the user
     */
    private void setName(String name) {
        if (name != null) {
            put(PARSE_TABLE_COLUMN_NAME, name);
        }
    }

    // Photo url
    public String getPhotoUrl() {
        return getString(PARSE_TABLE_COLUMN_PHOTO_URL);
    }

    /**
     * Set the photo url of the user
     * This method should remain private to not allow it to be modified
     * other place than the constructors
     * @param photoUrl
     *      The photo url of the user
     */
    private void setPhotoUrl(String photoUrl) {
        if (photoUrl != null) {
            put(PARSE_TABLE_COLUMN_PHOTO_URL, photoUrl);
        }
    }

    // Is Google PlusUser
    public Boolean isGooglePlusUser() {
        return getBoolean(PARSE_TABLE_COLUMN_IS_GOOGLE_PLUS_USER);
    }

    /**
     * Set if the user is google plus user
     * This method should remain private to not allow it to be modified
     * other place than the constructors
     * @param isGooglePlusUser
     *      if the user is google plus user
     */
    private void setIsGooglePlusUser(boolean isGooglePlusUser) {
        put(PARSE_TABLE_COLUMN_IS_GOOGLE_PLUS_USER, isGooglePlusUser);
    }

    // Profile url
    public String getProfileUrl() {
        return getString(PARSE_TABLE_COLUMN_PROFILE_URL);
    }

    public boolean hasProfileUrl() {
        return has(PARSE_TABLE_COLUMN_PROFILE_URL);
    }

    /**
     * Set the profile url of the user
     * This method should remain private to not allow it to be modified
     * other place than the constructors
     * @param profileUrl
     *      The profile url of the user
     */
    private void setProfileUrl(String profileUrl) {
        if (profileUrl != null) {
            put(PARSE_TABLE_COLUMN_PROFILE_URL, profileUrl);
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + getName() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", photo url='" + getPhotoUrl() + '\'' +
                ", isGooglePlusUser='" + isGooglePlusUser() + '\'' +
                ", profileUrl='" + getProfileUrl() + '\'' +
                "}";
    }
}
