package com.poptech.poptalk.storyboard;

import dagger.Module;
import dagger.Provides;

/**
 * Created by sontt on 09/05/2017.
 */
@Module
public class StoryBoardPresenterModule {
    private StoryBoardContract.View mView;

    public StoryBoardPresenterModule(StoryBoardContract.View view) {
        this.mView = view;
    }

    @Provides
    public StoryBoardContract.View providesStoryBoardView(){
        return this.mView;
    }
}
