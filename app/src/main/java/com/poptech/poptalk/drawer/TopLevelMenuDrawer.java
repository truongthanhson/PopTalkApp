package com.poptech.poptalk.drawer;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by sont on 02/05/2017.
 */

public class TopLevelMenuDrawer extends ExpandableGroup<SubMenuDrawer> {

    private int iconResId;

    public TopLevelMenuDrawer(String title, List<SubMenuDrawer> items, int iconResId) {
        super(title, items);
        this.iconResId = iconResId;
    }

    public int getIconResId() {
        return iconResId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopLevelMenuDrawer)) return false;

        TopLevelMenuDrawer genre = (TopLevelMenuDrawer) o;

        return getIconResId() == genre.getIconResId();

    }

    @Override
    public int hashCode() {
        return getIconResId();
    }
}
