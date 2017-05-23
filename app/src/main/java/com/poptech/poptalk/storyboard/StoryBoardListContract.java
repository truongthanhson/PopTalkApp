package com.poptech.poptalk.storyboard;

import com.poptech.poptalk.BasePresenter;
import com.poptech.poptalk.BaseView;
import com.poptech.poptalk.bean.StoryBoard;

import java.util.List;

/**
 * Created by sontt on 23/05/2017.
 */

public interface StoryBoardListContract {

    interface View extends BaseView<StoryBoardListContract.Presenter>{
        void onStoryBoardsLoaded(List<StoryBoard> storyBoardList);
    }

    interface Presenter extends BasePresenter{
        void loadAllStoryBoards();
    }
}
