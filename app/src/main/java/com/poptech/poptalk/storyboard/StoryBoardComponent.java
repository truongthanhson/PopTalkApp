package com.poptech.poptalk.storyboard;

import com.poptech.poptalk.di.AppComponent;
import com.poptech.poptalk.di.FragmentScoped;

import dagger.Component;

/**
 * Created by sontt on 09/05/2017.
 */

@FragmentScoped
@Component(dependencies = {AppComponent.class}, modules = {StoryBoardPresenterModule.class})
public interface StoryBoardComponent {
    void inject(StoryBoardFragment storyBoardFragment);
}
