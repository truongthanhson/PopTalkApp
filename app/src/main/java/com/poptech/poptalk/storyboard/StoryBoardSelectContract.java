package com.poptech.poptalk.storyboard;

import com.poptech.poptalk.BasePresenter;
import com.poptech.poptalk.BaseView;
import com.poptech.poptalk.bean.SpeakItem;

import java.util.List;

/**
 * Created by sontt on 09/05/2017.
 */

public interface StoryBoardSelectContract {

    interface View extends BaseView<Presenter>{
        void onSpeakItemLoaded(List<SpeakItem> speakItems);
    }

    interface Presenter extends BasePresenter{
        void loadAllSpeakItems();
    }
}
