package com.poptech.poptalk.storyboard;

import com.poptech.poptalk.BasePresenter;
import com.poptech.poptalk.BaseView;
import com.poptech.poptalk.bean.SpeakItem;

import java.util.List;

/**
 * Created by sontt on 09/05/2017.
 */

public interface StoryBoardContract {

    interface View extends BaseView<Presenter>{
        void onGenerateData(List<SpeakItem> sortedList);
    }

    interface Presenter extends BasePresenter{
        void loadData(int column);
    }
}
