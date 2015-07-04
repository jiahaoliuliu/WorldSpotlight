package com.worldspotlightapp.android.model;

/**
 *
 * This class represents an author of a video.
 * For now this is retrieved directly from YouTube.
 * In a nearly future, this should be retrieved from the backend server
 * Created by jiahaoliuliu on 7/4/15.
 */
public class Author {

    /**
     * The id of the author in the YouTube Server
     */
    private String mId;

    /**
     * The name of the author
     */
    private String mName;

    /**
     * The url for the thumbnail image
     */
    private String mThumbnailUrl;

    public Author(String id, String name, String thumbnailUrl) {
        this.mId = id;
        this.mName = name;
        this.mThumbnailUrl = thumbnailUrl;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.mThumbnailUrl = thumbnailUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Author author = (Author) o;

        if (mId != null ? !mId.equals(author.mId) : author.mId != null) return false;
        if (mName != null ? !mName.equals(author.mName) : author.mName != null) return false;
        return !(mThumbnailUrl != null ? !mThumbnailUrl.equals(author.mThumbnailUrl) : author.mThumbnailUrl != null);

    }

    @Override
    public int hashCode() {
        int result = mId != null ? mId.hashCode() : 0;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mThumbnailUrl != null ? mThumbnailUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id='" + mId + '\'' +
                ", name='" + mName + '\'' +
                ", thumbnailUrl='" + mThumbnailUrl + '\'' +
                '}';
    }
}
