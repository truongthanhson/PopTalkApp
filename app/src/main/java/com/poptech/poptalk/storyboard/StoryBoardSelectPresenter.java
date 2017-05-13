package com.poptech.poptalk.storyboard;

import com.google.common.collect.Lists;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.provider.SpeakItemModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 09/05/2017.
 */

public class StoryBoardSelectPresenter implements StoryBoardSelectContract.Presenter {
    private StoryBoardSelectContract.View mView;
    private SpeakItemModel mModel;

    @Inject
    public StoryBoardSelectPresenter(StoryBoardSelectContract.View view, SpeakItemModel model) {
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
    public void loadAllSpeakItems() {
        List<SpeakItem> speakItems = mModel.getSpeakItems();
        mView.onSpeakItemLoaded(speakItems);
    }
}
