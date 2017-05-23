package com.poptech.poptalk.storyboard;

import com.poptech.poptalk.di.AppComponent;
import com.poptech.poptalk.di.FragmentScoped;

import dagger.Component;

/**
 * Created by sontt on 23/05/2017.
 */

@FragmentScoped
@Component(dependencies = {AppComponent.class}, modules = {StoryBoardListPresenterModule.class})
public interface StoryBoardListComponent {
    void inject(StoryBoardListFragment storyBoardListFragment);
}
