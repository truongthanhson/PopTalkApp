package com.poptech.poptalk.drawer.data;

import java.util.ArrayList;
import java.util.List;
public class CustomDataProvider {

    private static final int MAX_LEVELS = 3;

    private static final int LEVEL_1 = 1;

    private static List<BaseItem> mMenu = new ArrayList<>();

    public static List<BaseItem> getInitialItems() {
        List<BaseItem> rootMenu = new ArrayList<>();
        rootMenu.add(new GroupItem("View"));
        rootMenu.add(new GroupItem("Sort by"));
        rootMenu.add(new GroupItem("Storyboard"));
        return rootMenu;
    }

    public static List<BaseItem> getSubItems(BaseItem baseItem) {

        List<BaseItem> result = new ArrayList<>();
        int level = ((GroupItem) baseItem).getLevel() + 1;
        String menuItem = baseItem.getName();

        if (!(baseItem instanceof GroupItem)) {
            throw new IllegalArgumentException("GroupItem required");
        }

        GroupItem groupItem = (GroupItem)baseItem;
        if(groupItem.getLevel() >= MAX_LEVELS){
            return null;
        }

        switch (level){
            case LEVEL_1 :
                switch (menuItem.toUpperCase()){
                    case "VIEW" :
                        result = getListView();
                        break;
                    case "SORT BY" :
                        result = getListSortBy();
                        break;

                    case "STORYBOARD" :
                        result = getListStoryboard();
                        break;
                }
                break;
        }

        return result;
    }

    public static boolean isExpandable(BaseItem baseItem) {
        return baseItem instanceof GroupItem;
    }

    private static List<BaseItem> getListView(){
        List<BaseItem> list = new ArrayList<>();
        list.add(new Item("Collection"));
        list.add(new Item("List"));
        list.add(new Item("Location"));
        return list;
    }

    private static List<BaseItem> getListStoryboard(){
        List<BaseItem> list = new ArrayList<>();
        list.add(new Item("Map & DateTime"));
        list.add(new Item("Frequency"));
        return list;
    }

    public static List<BaseItem> getListSortBy() {
        List<BaseItem> list = new ArrayList<>();
        list.add(new Item("Description"));
        list.add(new Item("Language"));
        list.add(new Item("Recent"));
        return list;
    }
}
