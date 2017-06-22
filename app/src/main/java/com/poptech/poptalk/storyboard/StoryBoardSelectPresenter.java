package com.poptech.poptalk.storyboard;

import com.google.common.collect.Lists;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.bean.StoryBoard;
import com.poptech.poptalk.provider.SpeakItemModel;
import com.poptech.poptalk.provider.StoryBoardModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

/**
 * Created by sontt on 09/05/2017.
 */

public class StoryBoardSelectPresenter implements StoryBoardSelectContract.Presenter {
    private StoryBoardSelectContract.View mView;
    private SpeakItemModel mModel;
    private StoryBoardModel mStoryBoardModel;

    @Inject
    public StoryBoardSelectPresenter(StoryBoardSelectContract.View view, SpeakItemModel model, StoryBoardModel storyBoardModel) {
        this.mView = view;
        this.mModel = model;
        this.mStoryBoardModel = storyBoardModel;
    }

    @Inject
    public void setupListeners(){
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void loadAllSpeakItems() {
        List<SpeakItem> speakItems = mModel.getSpeakItems();
        mView.onSpeakItemLoaded(speakItems);
    }

    @Override
    public void buildStoryBoard(List<SpeakItem> fromSpeakItems, String storyboardName) {
        StoryBoard storyBoard = new StoryBoard();
        storyBoard.setId(new Random().nextInt(Integer.MAX_VALUE));
        storyBoard.setSpeakItems(fromSpeakItems);
        storyBoard.setName(storyboardName);
        storyBoard.setCreatedTime(System.currentTimeMillis());
        mStoryBoardModel.addNewStoryBoard(storyBoard);
        mView.onStoryBoardBuilt(storyBoard);
    }
}
