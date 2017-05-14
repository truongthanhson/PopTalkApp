package com.poptech.poptalk.speakitem;

import com.poptech.poptalk.BasePresenter;
import com.poptech.poptalk.BaseView;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.SpeakItem;

import java.util.List;

public interface SpeakItemDetailContract {
    interface View extends BaseView<Presenter> {
        void onSpeakItemLoaded(SpeakItem speakItem);

        void onCollectionLoaded(Collection collection);
    }

    interface Presenter extends BasePresenter {
        void loadSpeakItem(long itemId);

        void loadCollection(long collectionId);

        void updateSpeakItem(SpeakItem speakItem);

        void updateCollection(Collection collection);
    }
}
