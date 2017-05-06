package com.poptech.poptalk.collections;

/**
 * Created by truongthanhson on 5/6/17.
 */

public interface AppMenuOpen {
    void onNavigateToViewCollection();
    void onNavigateToViewView();
    void onNavigateToViewLocation();

    void onNavigateToSortByDescription();
    void onNavigateToSortByLanguage();
    void onNavigateToSortByRecent();

    void onNavigateStoryboardMap();
    void onNavigateStoryboardFrequency();
}
