package com.poptech.poptalk.bean;

/**
 * Created by sontt on 30/04/2017.
 */

public class SpeakItem {
    private String id;
    private String photoPath;

    public SpeakItem(String id, String photoPath) {
        this.id = id;
        this.photoPath = photoPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public String toString() {
        return "SpeakItem{" +
                "id='" + id + '\'' +
                ", photoPath='" + photoPath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpeakItem speakItem = (SpeakItem) o;

        if (id != null ? !id.equals(speakItem.id) : speakItem.id != null) return false;
        return photoPath != null ? photoPath.equals(speakItem.photoPath) : speakItem.photoPath == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (photoPath != null ? photoPath.hashCode() : 0);
        return result;
    }
}
