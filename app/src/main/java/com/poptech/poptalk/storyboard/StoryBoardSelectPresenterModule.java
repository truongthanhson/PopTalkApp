package com.poptech.poptalk.storyboard;

import dagger.Module;
import dagger.Provides;

/**
 * Created by sontt on 09/05/2017.
 */
@Module
public class StoryBoardSelectPresenterModule {
    private StoryBoardSelectContract.View mView;

    public StoryBoardSelectPresenterModule(StoryBoardSelectContract.View view) {
        this.mView = view;
    }

    @Provides
    public StoryBoardSelectContract.View providesStoryBoardView(){
        return this.mView;
    }
}
