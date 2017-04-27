package com.poptech.poptalk.collections;

import com.poptech.poptalk.BasePresenter;
import com.poptech.poptalk.BaseView;
import com.poptech.poptalk.bean.Collection;

import java.util.List;

/**
 * Created by Administrator on 27/04/2017.
 */

public interface CollectionsContract {
    interface View extends BaseView<Presenter>{
      void  onCollectionsLoaded(List<Collection> collections);
    }

    interface Presenter extends BasePresenter{
       void loadCollections();
    }
}
