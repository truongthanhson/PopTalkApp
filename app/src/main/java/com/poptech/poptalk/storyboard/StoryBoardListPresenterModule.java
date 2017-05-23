package com.poptech.poptalk.storyboard;

import dagger.Module;
import dagger.Provides;

/**
 * Created by sontt on 09/05/2017.
 */
@Module
public class StoryBoardListPresenterModule {
    private StoryBoardListContract.View mView;

    public StoryBoardListPresenterModule(StoryBoardListContract.View view) {
        this.mView = view;
    }

    @Provides
    public StoryBoardListContract.View providesStoryBoardListView(){
        return this.mView;
    }
}
