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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.bean.StoryBoard;
import com.poptech.poptalk.collections.SpeakItemsFragment;
import com.poptech.poptalk.utils.MetricUtils;
import com.poptech.poptalk.view.ItemDecorationColumns;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 09/05/2017.
 */

public class StoryBoardSelectFragment extends Fragment implements StoryBoardSelectContract.View {

    public interface StoryBoardSelectFragmentCallBack{
        void onStoryBoardBuilt(StoryBoard storyBoard);
    }

    public static StoryBoardSelectFragment newInstance() {
        return new StoryBoardSelectFragment();
    }

    @Inject
    StoryBoardSelectPresenter mPresenter;

    private View mRootView;

    private RecyclerView mStoryBoardSelectView;

    private StoryBoardSelectAdapter mAdapter;

    private StoryBoardSelectFragmentCallBack mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof StoryBoardSelectFragmentCallBack){
            mCallback = (StoryBoardSelectFragmentCallBack)context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerStoryBoardSelectComponent.builder()
                .appComponent(((PopTalkApplication) PopTalkApplication.applicationContext).getAppComponent())
                .storyBoardSelectPresenterModule(new StoryBoardSelectPresenterModule(this))
                .build().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_story_board_select_layout,container,false);
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
        mPresenter.loadAllSpeakItems();
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
        mStoryBoardSelectView = (RecyclerView)mRootView.findViewById(R.id.speak_item_list);
        mStoryBoardSelectView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mStoryBoardSelectView.addItemDecoration(new ItemDecorationColumns(3, getResources().getDimensionPixelSize(R.dimen.grid_divider), true));
    }

    @Override
    public void setPresenter(StoryBoardSelectContract.Presenter presenter) {
        this.mPresenter = (StoryBoardSelectPresenter) presenter;
    }

    @Override
    public void onSpeakItemLoaded(List<SpeakItem> speakItems) {
        mAdapter = new StoryBoardSelectAdapter(speakItems, getActivity());
        mStoryBoardSelectView.setAdapter(mAdapter);
    }

    public void buildStoryBoard(String storyboardName){
        if(getSelectedSpeakItems() == null || getSelectedSpeakItems().size() == 0){
            Toast.makeText(getActivity(), "You did not select any speak item to build storyboard", Toast.LENGTH_SHORT).show();
        }else{
            mPresenter.buildStoryBoard(getSelectedSpeakItems(),storyboardName);
        }
    }

    @Override
    public void onStoryBoardBuilt(StoryBoard storyBoard) {
        if(mCallback != null){
            mCallback.onStoryBoardBuilt(storyBoard);
        }
    }

    public ArrayList<SpeakItem> getSelectedSpeakItems(){
        return mAdapter.getSelectedSpeakItems();
    }

    public class StoryBoardSelectAdapter extends RecyclerView.Adapter<StoryBoardSelectFragment.SpeakItemViewHolder> {
        private List<SpeakItem> mSpeakItems;
        private Context mContext;
        private ArrayList<Integer> mSelectedPos = new ArrayList<>();

        public StoryBoardSelectAdapter(List<SpeakItem> collections, Context context) {
            this.mSpeakItems = collections;
            this.mContext = context;
        }

        @Override
        public StoryBoardSelectFragment.SpeakItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_story_board_select_layout, parent, false);
            return new StoryBoardSelectFragment.SpeakItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(final StoryBoardSelectFragment.SpeakItemViewHolder holder, final int position) {
            Glide.with(mContext)
                    .load(mSpeakItems.get(position).getPhotoPath())
                    .centerCrop()
                    .thumbnail(0.5f)
                    .placeholder(R.color.colorAccent)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(holder.mThumbnailIv);

            if(mSelectedPos.contains(position)){
                holder.mOrderTv.setVisibility(View.VISIBLE);
                holder.mOrderTv.setText(getOrderOfPosition(position) + 1 + "");
            }else{
                holder.mOrderTv.setVisibility(View.GONE);
            }

            holder.mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mSelectedPos.contains(position)){
                        mSelectedPos.remove((Integer) position);
                        holder.mOrderTv.setVisibility(View.GONE);
                        notifyDataSetChanged();
                    }else{
                        mSelectedPos.add(position);
                        holder.mOrderTv.setVisibility(View.VISIBLE);
                        holder.mOrderTv.setText(getOrderOfPosition(position) + 1 + "");
                    }
                }
            });
        }

//        public void invalidateSelectedData(){
//            for(int i=0;i< mSelectedPos.size();i++){
//                int key = mSelectedPos.keyAt(i);
//                int position = mSelectedPos.get(key);
//                notifyItemChanged(position);
//            }
//        }

        @Override
        public int getItemCount() {
            return mSpeakItems.size();
        }

        public void clearSelect(){
            mSelectedPos.clear();
        }

        public int getOrderOfPosition(int position){
            for(int i=0; i<mSelectedPos.size();i++){
                if(mSelectedPos.get(i) == position){
                    return i;
                }
            }
            return -1;
        }

        public ArrayList<SpeakItem> getSelectedSpeakItems(){
            ArrayList<SpeakItem> speakItems = new ArrayList<>();
            for(int i=0; i<mSelectedPos.size();i++){
                speakItems.add(mSpeakItems.get(mSelectedPos.get(i)));
            }
            return speakItems;
        }

    }

    public class SpeakItemViewHolder extends RecyclerView.ViewHolder {

        public View mRootView;
        public ImageView mThumbnailIv;
        public TextView mOrderTv;

        public SpeakItemViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
            mThumbnailIv = (ImageView) mRootView.findViewById(R.id.iv_thumb_id);
            mOrderTv = (TextView)mRootView.findViewById(R.id.order_tv_id);
        }
    }
}
