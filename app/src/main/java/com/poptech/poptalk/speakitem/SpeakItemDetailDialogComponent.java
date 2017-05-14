package com.poptech.poptalk.speakitem;

import com.poptech.poptalk.di.AppComponent;
import com.poptech.poptalk.di.FragmentScoped;

import dagger.Component;


@FragmentScoped
@Component(dependencies = {AppComponent.class})
public interface SpeakItemDetailDialogComponent {
    void inject(SpeakItemDetailDialogFragment speakItemsDetailFragment);
}
