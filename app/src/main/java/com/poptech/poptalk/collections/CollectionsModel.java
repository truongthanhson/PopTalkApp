package com.poptech.poptalk.collections;

import com.poptech.poptalk.BaseModel;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.provider.PopTalkDatabase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 27/04/2017.
 */

public class CollectionsModel implements BaseModel {

    PopTalkDatabase mDatabase;

    @Inject
    public CollectionsModel(PopTalkDatabase database) {
        this.mDatabase = database;
    }

    public List<Collection> getCollections() {
        return new ArrayList<>();
    }
}
