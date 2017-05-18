package com.poptech.poptalk.drawer;

import com.poptech.poptalk.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sontt on 02/05/2017.
 */

public class DrawerMenuDataFactory {

    public static List<TopLevelMenuDrawer> makeDrawerMenu() {
        return Arrays.asList(makeViewMenu(), makeSortByMenu(), makeStoryBoardMenu(), makeShareMenu());
    }

    public static TopLevelMenuDrawer makeViewMenu() {
        return new TopLevelMenuDrawer("View", makeViewSubmenu(), R.drawable.ic_view);
    }

    public static TopLevelMenuDrawer makeSortByMenu() {
        return new TopLevelMenuDrawer("Sort By", makeSortBySubmenu(), R.drawable.ic_sort);
    }

    public static TopLevelMenuDrawer makeStoryBoardMenu() {
        return new TopLevelMenuDrawer("Story Board", makeStoryBoardSubmenu(), R.drawable.ic_storyboard);
    }

    public static TopLevelMenuDrawer makeShareMenu() {
        return new TopLevelMenuDrawer("Share", makeShareSubMenu(), R.drawable.ic_view);
    }

    private static List<SubMenuDrawer> makeStoryBoardSubmenu() {
        return Arrays.asList(new SubMenuDrawer("Map & Datetime"), new SubMenuDrawer("Frequency"));
    }

    private static List<SubMenuDrawer> makeSortBySubmenu() {
        return Arrays.asList(new SubMenuDrawer("Description"), new SubMenuDrawer("Language"), new SubMenuDrawer("Recent"));
    }

    private static List<SubMenuDrawer> makeViewSubmenu() {
        return Arrays.asList(new SubMenuDrawer("Collection"), new SubMenuDrawer("List"), new SubMenuDrawer("Location"));
    }

    private static List<SubMenuDrawer> makeShareSubMenu() {
        return Arrays.asList(new SubMenuDrawer("WiFi Direct"));
    }

}
