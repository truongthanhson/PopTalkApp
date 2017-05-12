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
        if(speakItems.size() <= column){
            mView.onGenerateData(speakItems);
            return;
        }

        List<SpeakItem> resultSpeakItems = new ArrayList<>();
        List<SpeakItem> tempList = new ArrayList<>();
        int dataAdded = addData(speakItems.size(), column);

        if(dataAdded > 0){
            for(int i = 1; i <= dataAdded; i++){
                SpeakItem paddingItem = new SpeakItem();
                paddingItem.setId(-999);
                speakItems.add(paddingItem);
            }
        }

        int numberOfLines = speakItems.size() / column;
        if(speakItems.size() % column != 0){
            numberOfLines ++;
        }

        for(int i=0; i < numberOfLines; i++){
            tempList.clear();
            for(int j = 0; j < column; j++){
                if(i * column + j > speakItems.size() - 1)
                    break;
                tempList.add(speakItems.get(i * column + j));
            }

            if(i % 2 == 1){
                resultSpeakItems.addAll(Lists.reverse(tempList));
            }else{
                resultSpeakItems.addAll(tempList);
            }
        }
        mView.onGenerateData(resultSpeakItems);
    }

    private int addData(int listSize, int spanColumn) {
        if(listSize <= spanColumn)
            return 0;

        if (listSize % spanColumn == 0) {
            return 0;
        }

        if((listSize / spanColumn) % 2 == 1){
            return spanColumn - listSize % spanColumn;
        }

        return 0;
    }

}
