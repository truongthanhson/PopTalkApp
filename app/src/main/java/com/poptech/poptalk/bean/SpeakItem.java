package com.poptech.poptalk.bean;

/**
 * Created by sontt on 30/04/2017.
 */

public class SpeakItem {
    private long id;
    private String description;
    private String mark;
    private String photoPath;
    private long collectionId;

    public SpeakItem() {
    }

    public SpeakItem(long id, String description, String mark, String photoPath, long collectionId) {
        this.id = id;
        this.description = description;
        this.mark = mark;
        this.photoPath = photoPath;
        this.collectionId = collectionId;
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

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(long collectionId) {
        this.collectionId = collectionId;
    }
}
