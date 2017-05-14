package com.poptech.poptalk.speakitem;

import com.poptech.poptalk.di.AppComponent;
import com.poptech.poptalk.di.FragmentScoped;

import dagger.Component;


@FragmentScoped
@Component(dependencies = {AppComponent.class}, modules = {SpeakItemDetailPresenterModule.class})
public interface SpeakItemDetailComponent {
    void inject (SpeakItemDetailFragment speakItemsDetailFragment);
    void inject (SpeakItemDetailDialogFragment speakItemsDetailFragment);
}
