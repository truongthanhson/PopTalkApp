package com.poptech.poptalk.collections;


import android.support.v4.app.Fragment;

/**
 * Created by sontt on 26/04/2017.
 */

public class CollectionsFragment extends Fragment implements CollectionsContract.View {

    private CollectionsContract.Presenter mPresenter;
    public CollectionsFragment() {
        // Requires empty public constructor
    }

    public static CollectionsFragment newInstance() {
        return new CollectionsFragment();
    }

    @Override
    public void setPresenter(CollectionsContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
