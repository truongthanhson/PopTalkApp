package com.poptech.poptalk.collections;


import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.poptech.poptalk.Constants;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Collection;

import java.util.List;

/**
 * Created by sontt on 26/04/2017.
 */

public class CollectionsFragment extends Fragment implements CollectionsContract.View {

    private CollectionsContract.Presenter mPresenter;
    private View mView;
    private RecyclerView mCollectionsView;

    public CollectionsFragment() {
        // Requires empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_collections_layout,container,false);
        initView();
        return mView;
    }

    private void initView() {
        mCollectionsView = (RecyclerView) mView.findViewById(R.id.colletions_list);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), Constants.GRID_COLUMN_COUNT);
        ItemDecorationColumns itemDecoration = new ItemDecorationColumns(Constants.GRID_COLUMN_COUNT, getResources().getDimensionPixelSize(R.dimen.grid_divider), true);
        mCollectionsView.setHasFixedSize(true);
        mCollectionsView.setLayoutManager(layoutManager);
        mCollectionsView.addItemDecoration(itemDecoration);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static CollectionsFragment newInstance() {
        return new CollectionsFragment();
    }

    @Override
    public void setPresenter(CollectionsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onCollectionsLoaded(List<Collection> collections) {

    }

    public class ItemDecorationColumns extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public ItemDecorationColumns(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;
                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }
}
