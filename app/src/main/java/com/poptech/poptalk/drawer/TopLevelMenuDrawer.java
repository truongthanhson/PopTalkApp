package com.poptech.poptalk.drawer;

import android.os.Parcel;

import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablecheckrecyclerview.models.SingleCheckExpandableGroup;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by sont on 02/05/2017.
 */

public class TopLevelMenuDrawer extends SingleCheckExpandableGroup {

    private int iconResId;

    public TopLevelMenuDrawer(String title, List<SubMenuDrawer> items, int iconResId) {
        super(title, items);
        this.iconResId = iconResId;
    }


    protected TopLevelMenuDrawer(Parcel in) {
        super(in);
        iconResId = in.readInt();
    }
    public int getIconResId() {
        return iconResId;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(iconResId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TopLevelMenuDrawer> CREATOR = new Creator<TopLevelMenuDrawer>() {
        @Override
        public TopLevelMenuDrawer createFromParcel(Parcel in) {
            return new TopLevelMenuDrawer(in);
        }

        @Override
        public TopLevelMenuDrawer[] newArray(int size) {
            return new TopLevelMenuDrawer[size];
        }
    };

}
