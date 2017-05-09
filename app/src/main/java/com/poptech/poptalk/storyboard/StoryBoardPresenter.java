package com.poptech.poptalk.storyboard;

import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.provider.SpeakItemModel;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 09/05/2017.
 */

public class StoryBoardPresenter implements StoryBoardContract.Presenter {
    private StoryBoardContract.View mView;
    private SpeakItemModel mModel;

    @Inject
    public StoryBoardPresenter(StoryBoardContract.View view, SpeakItemModel model) {
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
    public void loadData(int column) {
        List<SpeakItem> speakItems = mModel.getSpeakItems(4);
        mView.onGenerateData(speakItems);
    }
}
