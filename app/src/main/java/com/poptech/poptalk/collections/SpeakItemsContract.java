package com.poptech.poptalk.collections;

import com.poptech.poptalk.BasePresenter;
import com.poptech.poptalk.BaseView;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.SpeakItem;

import java.util.List;

/**
 * Created by sontt on 27/04/2017.
 */

public interface SpeakItemsContract {
    interface View extends BaseView<Presenter> {
        void onAllSpeakItemsLoaded(List<SpeakItem> speakItems, List<Collection> collections);

        void onSpeakItemsLoaded(List<SpeakItem> speakItems);

        void onCollectionLoaded(Collection collection);
    }

    interface Presenter extends BasePresenter {
        void loadAllSpeakItems();

        void loadSpeakItems(long collectionId);

        void loadCollection(long collectionId);
    }
}
