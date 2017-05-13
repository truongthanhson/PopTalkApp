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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 09/05/2017.
 */

public class StoryBoardFragment extends Fragment implements StoryBoardContract.View {

    public static StoryBoardFragment newInstance() {
        return new StoryBoardFragment();
    }

    private static final int STORY_COLUMN = 3;

    @Inject
    StoryBoardPresenter mPresenter;

    private View mRootView;

    private RecyclerView mStoryBoardView;

    private StoryBoardAdapter mAdapter;

    private ArrayList<SpeakItem> speakItems;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerStoryBoardComponent.builder()
                .appComponent(((PopTalkApplication) PopTalkApplication.applicationContext).getAppComponent())
                .storyBoardPresenterModule(new StoryBoardPresenterModule(this))
                .build().inject(this);

        speakItems = getArguments().getParcelableArrayList("selected_speak_items");
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
        mPresenter.loadData(STORY_COLUMN, speakItems);
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
        mStoryBoardView.setLayoutManager(new GridLayoutManager(getActivity(), STORY_COLUMN));
        mStoryBoardView.addItemDecoration(new PathItemDecoration(STORY_COLUMN, MetricUtils.dpToPx(10)));
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


    private final int VIEW_TYPE_STORY_BOARD_ITEM = 1;
    private final int VIEW_TYPE_STORY_BOARD_PADDING_ITEM = 2;
    public class StoryBoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private List<SpeakItem> mSpeakItems;
        private Context mContext;

        public StoryBoardAdapter(List<SpeakItem> collections, Context context) {
            this.mSpeakItems = collections;
            this.mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == VIEW_TYPE_STORY_BOARD_ITEM){
                View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_story_board_layout, parent, false);
                return new StoryBoardItemViewHolder(rootView);
            }else if(viewType == VIEW_TYPE_STORY_BOARD_PADDING_ITEM){
                View rootView = new View(getContext());
                return new StoryBoardItemFakeDataViewHolder(rootView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if(holder.getItemViewType() == VIEW_TYPE_STORY_BOARD_ITEM){
                StoryBoardItemViewHolder storyBoardItemViewHolder = (StoryBoardItemViewHolder) holder;
                Glide.with(mContext)
                        .load(mSpeakItems.get(position).getPhotoPath())
                        .centerCrop()
                        .thumbnail(0.5f)
                        .placeholder(R.color.colorAccent)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(storyBoardItemViewHolder.mThumbnailIv);
            }

        }

        @Override
        public int getItemCount() {
            return mSpeakItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(mSpeakItems.get(position).getId() < 0)
                return VIEW_TYPE_STORY_BOARD_PADDING_ITEM;
            return VIEW_TYPE_STORY_BOARD_ITEM;
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

    public class StoryBoardItemFakeDataViewHolder extends RecyclerView.ViewHolder{

        private View mRootView;
        public StoryBoardItemFakeDataViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
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

            for (int i = 0; i < childCount; i++) {

                if(childCount <= 1)
                    return;

                View child = parent.getChildAt(i);
                int position = parent.getChildAdapterPosition(child);

                if(parent.getAdapter().getItemViewType(position) == VIEW_TYPE_STORY_BOARD_ITEM) {
                    boolean lastItem = isLastItem(position,parent.getAdapter().getItemCount(), spanColumn, parent.getAdapter());
                    int lineTop =  child.getTop() + (child.getHeight() - pathWidth) / 2;
                    int lintBottom = child.getTop() + (child.getHeight() - pathWidth) / 2 + pathWidth;
                    int lineLeft = child.getLeft();
                    int lineRight = child.getRight();
                    if(position == 0){
                        lineLeft += itemPadding;
                        if(lastItem)
                            lineRight -= itemPadding;
                    }else if(lastItem){
                        if((position / spanColumn) % 2 == 0){
                            lineRight -= itemPadding;
                        }else{
                            lineLeft += itemPadding;
                        }
                    }
                    temp.set(lineLeft, lineTop, lineRight, lintBottom);
                    c.drawRect(temp, pathPaint);

                    if (position % spanColumn == 0) {
                        if (position != 0) {
                            if ((position / spanColumn) % 2 == 0) {
                                temp.set(child.getLeft(), child.getTop(), child.getLeft() + pathWidth, (child.getTop() + (child.getHeight() - pathWidth) / 2) + pathWidth);
                                c.drawRect(temp, pathPaint);
                            } else if ((position / spanColumn) % 2 == 1 && !lastItem) {
                                temp.set(child.getLeft(), child.getTop() + (child.getHeight() - pathWidth) / 2, child.getLeft() + pathWidth, child.getBottom());
                                c.drawRect(temp, pathPaint);
                            }
                        }
                    }

                    if (position % spanColumn == (spanColumn - 1)) {
                        if ((((position + 1) / spanColumn)) % 2 == 1 && !lastItem) {
                            temp.set(child.getRight() - pathWidth, child.getTop() + (child.getHeight() - pathWidth) / 2, child.getRight(), child.getBottom());
                            c.drawRect(temp, pathPaint);
                        } else if ((((position + 1) / spanColumn)) % 2 == 0) {
                            temp.set(child.getRight() - pathWidth, child.getTop(), child.getRight(), (child.getTop() + (child.getHeight() - pathWidth) / 2 + pathWidth));
                            c.drawRect(temp, pathPaint);
                        }
                    }
                }
            }
        }

        private boolean isLastItem(int position, int total, int nbColumn, RecyclerView.Adapter adapter){
            int rowOfItem = position / nbColumn + 1; // 1-based-index
            int lastRow = total / nbColumn + ((total % nbColumn != 0)?1:0);

            if(rowOfItem < lastRow){
                return false;
            }
            if(rowOfItem % 2 == 0){
//                int itemOfLastRow = total - (rowOfItem - 1) * nbColumn;
//                if(position == total - itemOfLastRow){
//                    return true;
//                }
                if(position == total - nbColumn){
                    return true;
                }

                if(adapter.getItemViewType(position - 1) == VIEW_TYPE_STORY_BOARD_PADDING_ITEM){
                    return true;
                }
            }else{
                if(position == total - 1)
                    return true;
            }
            return false;
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
