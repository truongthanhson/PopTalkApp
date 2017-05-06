package com.poptech.poptalk.collections;

import com.poptech.poptalk.di.AppComponent;
import com.poptech.poptalk.di.FragmentScoped;

import dagger.Component;

/**
 * Created by sontt on 27/04/2017.
 */

@FragmentScoped
@Component(dependencies = AppComponent.class, modules = CollectionsPresenterModule.class)
public interface CollectionsComponent {
    void inject(CollectionsFragment collectionsFragment);
}
