package com.poptech.poptalk.collections;

import com.poptech.poptalk.di.AppComponent;
import com.poptech.poptalk.di.FragmentScoped;

import dagger.Component;

/**
 * Created by truongthanhson on 5/6/17.
 */

@FragmentScoped
@Component(dependencies = {AppComponent.class}, modules = {SpeakItemsPresenterModule.class})
public interface SpeakItemComponent {
    void inject (SpeakItemsFragment speakItemsFragment);
}
