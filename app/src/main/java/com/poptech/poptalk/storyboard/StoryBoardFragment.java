package com.poptech.poptalk.storyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.utils.MetricUtils;
import com.poptech.poptalk.view.ItemDecorationColumns;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 09/05/2017.
 */

public class StoryBoardFragment extends Fragment implements StoryBoardContract.View {

    public static StoryBoardFragment newInstance() {
        return new StoryBoardFragment();
    }

    @Inject
    StoryBoardPresenter mPresenter;

    private View mRootView;

    private RecyclerView mStoryBoardView;

    private StoryBoardAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerStoryBoardComponent.builder()
                .appComponent(((PopTalkApplication) PopTalkApplication.applicationContext).getAppComponent())
                .storyBoardPresenterModule(new StoryBoardPresenterModule(this))
                .build().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_story_board_layout,container,false);
        initView();
        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.loadData(3);
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

    private void initView() {
        mStoryBoardView = (RecyclerView)mRootView.findViewById(R.id.story_board);
        mStoryBoardView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
//        ItemDecorationColumns itemDecorationColumns = new ItemDecorationColumns(2, MetricUtils.dpToPx(50), true);
//        mStoryBoardView.addItemDecoration(itemDecorationColumns);
        mStoryBoardView.addItemDecoration(new PathItemDecoration(3, MetricUtils.dpToPx(10)));
    }

    @Override
    public void setPresenter(StoryBoardContract.Presenter presenter) {
        this.mPresenter = (StoryBoardPresenter) presenter;
    }

    @Override
    public void onGenerateData(List<SpeakItem> sortedList) {
        mAdapter = new StoryBoardAdapter(sortedList, getActivity());
        mStoryBoardView.setAdapter(mAdapter);
    }

    public class StoryBoardAdapter extends RecyclerView.Adapter<StoryBoardItemViewHolder>{
        private List<SpeakItem> mSpeakItems;
        private Context mContext;

        public StoryBoardAdapter(List<SpeakItem> collections, Context context) {
            this.mSpeakItems = collections;
            this.mContext = context;
        }

        @Override
        public StoryBoardItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_story_board_layout, parent, false);
            return new StoryBoardItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(StoryBoardItemViewHolder holder, final int position) {
            Glide.with(mContext)
                    .load(mSpeakItems.get(position).getPhotoPath())
                    .centerCrop()
                    .thumbnail(0.5f)
                    .placeholder(R.color.colorAccent)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mThumbnailIv);
        }

        @Override
        public int getItemCount() {
            return mSpeakItems.size();
        }
    }

    public class StoryBoardItemViewHolder extends RecyclerView.ViewHolder{

        private View mRootView;
        private ImageView mThumbnailIv;
        public StoryBoardItemViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
            mThumbnailIv = (ImageView)mRootView.findViewById(R.id.iv_thumb_id);
        }
    }

    private class PathItemDecoration extends RecyclerView.ItemDecoration{
        private Paint pathPaint;
        private int pathWidth;
        private int itemPadding = MetricUtils.dpToPx(20);
        private int spanColumn;
        private Rect temp = new Rect();

        public PathItemDecoration(int spanColumn, int pathWidth) {
            this.spanColumn = spanColumn;
            this.pathWidth = pathWidth;
            init();
        }

        private void init() {
            pathPaint = new Paint();
            pathPaint.setColor(Color.RED);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            int childCount = parent.getChildCount();
            int totalItem = parent.getAdapter().getItemCount();

            for (int i = 0; i < childCount; i++) {

                if(childCount <= 1)
                    return;

                View child = parent.getChildAt(i);
                int pos = parent.getChildAdapterPosition(child);

                c.drawRect(new Rect(pos == 0?itemPadding : child.getLeft(), child.getTop() + (child.getHeight() - pathWidth)/2, child.getRight(),child.getTop() + (child.getHeight() - pathWidth)/2 + pathWidth), pathPaint);

                if(pos % spanColumn == 0){
                    if (pos != 0) {
                        if ((pos / spanColumn) % 2 == 0) {
                            c.drawRect(new Rect(child.getLeft(), child.getTop(), child.getLeft() + pathWidth, (child.getTop() + (child.getHeight() - pathWidth) / 2) + pathWidth), pathPaint);
                        } else if ((pos / spanColumn) % 2 == 1) {
                            c.drawRect(new Rect(child.getLeft(), child.getTop() + (child.getHeight() - pathWidth) / 2, child.getLeft() + pathWidth, child.getBottom()), pathPaint);
                        }
                    }
                }

                if(pos % spanColumn == (spanColumn - 1)){
                    if((((pos + 1) / spanColumn)) % 2  == 1){
                        c.drawRect(new Rect(child.getRight() - pathWidth, child.getTop() + (child.getHeight() - pathWidth)/2,child.getRight(),child.getBottom()), pathPaint);
                    }else if((((pos + 1) / spanColumn)) % 2  == 0){
                        c.drawRect(new Rect(child.getRight() - pathWidth, child.getTop(),child.getRight(),(child.getTop() + (child.getHeight() - pathWidth)/2 + pathWidth)), pathPaint);
                    }
                }

            }
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);

        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
        }
    }

}
