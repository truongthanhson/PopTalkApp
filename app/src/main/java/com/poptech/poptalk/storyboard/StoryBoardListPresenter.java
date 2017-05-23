package com.poptech.poptalk.storyboard;

import com.google.common.collect.Lists;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.bean.StoryBoard;
import com.poptech.poptalk.provider.SpeakItemModel;
import com.poptech.poptalk.provider.StoryBoardModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 09/05/2017.
 */

public class StoryBoardListPresenter implements StoryBoardListContract.Presenter {
    private StoryBoardListContract.View mView;
    private StoryBoardModel mModel;

    @Inject
    public StoryBoardListPresenter(StoryBoardListContract.View view, StoryBoardModel model) {
        this.mView = view;
        this.mModel = model;
    }

    @Inject
    public void setupListeners(){
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }


    @Override
    public void loadAllStoryBoards() {
        List<StoryBoard> storyBoards = mModel.getStoryBoards();
        mView.onStoryBoardsLoaded(storyBoards);
    }
}
