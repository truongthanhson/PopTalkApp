package com.poptech.poptalk.bean;

/**
 * Created by sontt on 28/04/2017.
 */

public class Collection {
    private long id;
    private String description;
    private String language;
    private String thumbPath;

    public Collection() {
    }

    public Collection(int id, String description, String language, String thumbPath) {
        this.id = id;
        this.description = description;
        this.language = language;
        this.thumbPath = thumbPath;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }
}
