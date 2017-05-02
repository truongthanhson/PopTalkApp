package com.poptech.poptalk.drawer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sontt on 02/05/2017.
 */

public class SubMenuDrawer implements Parcelable {
    private String name;

    public SubMenuDrawer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "TopLevelMenu{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubMenuDrawer that = (SubMenuDrawer) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    protected SubMenuDrawer(Parcel in){
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    public static final Creator<SubMenuDrawer> CREATOR = new Creator<SubMenuDrawer>() {
        @Override
        public SubMenuDrawer createFromParcel(Parcel source) {
            return new SubMenuDrawer(source);
        }

        @Override
        public SubMenuDrawer[] newArray(int size) {
            return new SubMenuDrawer[size];
        }
    };
}
