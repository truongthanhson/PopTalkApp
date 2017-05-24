package com.poptech.poptalk.storyboard;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.StoryBoard;
import com.poptech.poptalk.collections.SpeakItemsFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by sontt on 23/05/2017.
 */

public class StoryBoardListFragment extends Fragment implements StoryBoardListContract.View {
    public interface StoryBoardListFragmentCallback {
        void onClickStoryBoard(StoryBoard storyBoard);
    }

    public static StoryBoardListFragment newInstance(){
        return new StoryBoardListFragment();
    };

    @Inject
    StoryBoardListPresenter mPresenter;

    private View mRootView;

    private RecyclerView mStoryBoardListView;

    private StoryBoardListAdapter mAdapter;

    private StoryBoardListFragmentCallback mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StoryBoardListFragmentCallback) {
            mCallback = (StoryBoardListFragmentCallback) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerStoryBoardListComponent.builder()
                .appComponent(((PopTalkApplication) PopTalkApplication.applicationContext).getAppComponent())
                .storyBoardListPresenterModule(new StoryBoardListPresenterModule(this))
                .build().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_story_board_list_layout,container,false);
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
        mPresenter.loadAllStoryBoards();
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
        mStoryBoardListView = (RecyclerView)mRootView.findViewById(R.id.story_board_list);
        mStoryBoardListView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
    }

    @Override
    public void setPresenter(StoryBoardListContract.Presenter presenter) {
        this.mPresenter = (StoryBoardListPresenter) presenter;
    }

    @Override
    public void onStoryBoardsLoaded(List<StoryBoard> storyBoardList) {
        mAdapter = new StoryBoardListAdapter(storyBoardList, getActivity());
        mStoryBoardListView.setAdapter(mAdapter);
    }


    public class StoryBoardListAdapter extends RecyclerView.Adapter<StoryBoardListFragment.StoryBoardItemViewHolder> {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        private List<StoryBoard> mStoryBoards;
        private Context mContext;
        Calendar calendar = Calendar.getInstance();

        public StoryBoardListAdapter(List<StoryBoard> storyBoards, Context context) {
            this.mStoryBoards = storyBoards;
            this.mContext = context;
        }

        @Override
        public StoryBoardListFragment.StoryBoardItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_story_board_list_item_layout, parent, false);
            return new StoryBoardListFragment.StoryBoardItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(final StoryBoardListFragment.StoryBoardItemViewHolder holder, final int position) {
            Glide.with(mContext)
                    .load(mStoryBoards.get(position).getSpeakItems().get(0).getPhotoPath())
                    .centerCrop()
                    .thumbnail(0.5f)
                    .placeholder(R.color.colorAccent)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(holder.mThumbnailIv);

            calendar.setTimeInMillis(mStoryBoards.get(position).getCreatedTime());
            holder.mCreatedTimeTv.setText("Built " + dateFormat.format(calendar.getTime()));

            holder.mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mCallback!= null){
                        mCallback.onClickStoryBoard(mStoryBoards.get(position));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mStoryBoards.size();
        }


    }

    public class StoryBoardItemViewHolder extends RecyclerView.ViewHolder {

        public View mRootView;
        public ImageView mThumbnailIv;
        public TextView mCreatedTimeTv;

        public StoryBoardItemViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
            mThumbnailIv = (ImageView) mRootView.findViewById(R.id.iv_thumb_id);
            mCreatedTimeTv = (TextView)mRootView.findViewById(R.id.tv_created_time);
        }
    }
}
