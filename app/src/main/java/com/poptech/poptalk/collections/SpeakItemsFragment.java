package com.poptech.poptalk.collections;


import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

import static com.poptech.poptalk.collections.SpeakItemsFragment.GroupSpeakItemViewType.GRID;
import static com.poptech.poptalk.collections.SpeakItemsFragment.GroupSpeakItemViewType.LIST;

/**
 * Created by sontt on 26/04/2017.
 */

public class SpeakItemsFragment extends Fragment implements SpeakItemsContract.View {

    public interface SpeakItemsFragmentCallback {
        void onClickSpeakItem(long speakItemId, long collectionId);
    }

    public enum GroupSpeakItemSortType {
        NONE,
        DESCRIPTION,
        LANGUAGE,
        RECENT,
        LOCATION
    }

    public enum GroupSpeakItemViewType {
        GRID,
        LIST
    }

    @Inject
    SpeakItemPresenter mPresenter;

    private View mView;
    private RecyclerView mSpeakItemsView;
    private long mCollectionId;
    GroupSpeakItemSortType mSortType;
    GroupSpeakItemViewType mViewType;
    private SectionedRecyclerViewAdapter mSectionedSpeakItemAdapter;
    private SpeakItemsAdapter mSpeakItemAdapter;
    private SpeakItemsFragmentCallback mCallback;

    public SpeakItemsFragment() {
        // Requires empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SpeakItemsFragmentCallback) {
            mCallback = (SpeakItemsFragmentCallback) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the presenter
        DaggerSpeakItemComponent.builder().appComponent(((PopTalkApplication) PopTalkApplication.applicationContext).getAppComponent()).
                speakItemsPresenterModule(new SpeakItemsPresenterModule(this)).build().inject(this);

        Bundle args = getArguments();
        if (args != null) {
            mCollectionId = args.getLong(Constants.KEY_COLLECTION_ID);
            mSortType = (GroupSpeakItemSortType) args.getSerializable(Constants.KEY_SPEAK_ITEM_SORT_TYPE);
            mViewType = (GroupSpeakItemViewType) args.getSerializable(Constants.KEY_SPEAK_ITEM_VIEW_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_speak_items_layout, container, false);
        initView();
        return mView;
    }

    private void initView() {
        mSpeakItemsView = (RecyclerView) mView.findViewById(R.id.speak_item_list);
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
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.INTERNET).withListener(new BasePermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                super.onPermissionGranted(response);
                if (mSortType == GroupSpeakItemSortType.NONE) {
                    mPresenter.loadSpeakItems(mCollectionId);
                } else {
                    mPresenter.loadAllSpeakItems();
                }
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                super.onPermissionDenied(response);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                super.onPermissionRationaleShouldBeShown(permission, token);
            }
        }).check();
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

    public static SpeakItemsFragment newInstance() {
        return new SpeakItemsFragment();
    }

    @Override
    public void onAllSpeakItemsLoaded(List<SpeakItem> speakItems, List<Collection> collections) {
        if (mSortType == GroupSpeakItemSortType.LOCATION) {
            List<String> locations = new ArrayList<>();
            // get all locations
            for (SpeakItem speakItem : speakItems) {
                if (!StringUtils.isContainIgnoreCase(speakItem.getLocation(), locations)) {
                    locations.add(speakItem.getLocation().trim());
                }
            }
            for (String location : locations) {
                mSectionedSpeakItemAdapter = new SectionedRecyclerViewAdapter();
                List<SpeakItem> speakItemSection = new ArrayList<>();
                for (SpeakItem speakItem : speakItems) {
                    if (location.equalsIgnoreCase(speakItem.getLocation().trim())) {
                        speakItemSection.add(speakItem);
                    }
                }
                mSectionedSpeakItemAdapter.addSection(new SpeakItemSection(speakItemSection, location));
            }
        } else {
            // TODO: Sort collection here
            for (Collection collection : collections) {
                mSectionedSpeakItemAdapter = new SectionedRecyclerViewAdapter();
                List<SpeakItem> speakItemSection = new ArrayList<>();
                for (SpeakItem speakItem : speakItems) {
                    if (speakItem.getCollectionId() == collection.getId()) {
                        speakItemSection.add(speakItem);
                    }
                }
                // TODO: Sort speak items here
                mSectionedSpeakItemAdapter.addSection(new SpeakItemSection(speakItemSection, collection.getDescription()));
            }
        }
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        mSpeakItemsView.setLayoutManager(layoutManager);
        mSpeakItemsView.setAdapter(mSectionedSpeakItemAdapter);
    }

    @Override
    public void onSpeakItemsLoaded(List<SpeakItem> speakItems) {
        mSpeakItemAdapter = new SpeakItemsAdapter(speakItems, getActivity());
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        mSpeakItemsView.setLayoutManager(layoutManager);
        mSpeakItemsView.setAdapter(mSpeakItemAdapter);
    }


    public RecyclerView.LayoutManager getLayoutManager() {
        RecyclerView.LayoutManager layoutManager = null;

        if (mViewType == GRID) {
            layoutManager = new GridLayoutManager(getContext(), 4);
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    switch (mSectionedSpeakItemAdapter.getSectionItemViewType(position)) {
                        case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
                            return 4;
                        default:
                            return 1;
                    }
                }
            });
        } else if (mViewType == LIST) {
            layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        }
        return layoutManager;
    }

    @Override
    public void setPresenter(SpeakItemsContract.Presenter presenter) {
        this.mPresenter = (SpeakItemPresenter) presenter;
    }

    public class SpeakItemsAdapter extends RecyclerView.Adapter<SpeakItemViewHolder> {
        private List<SpeakItem> mSpeakItems;
        private Context mContext;

        public SpeakItemsAdapter(List<SpeakItem> collections, Context context) {
            this.mSpeakItems = collections;
            this.mContext = context;
        }

        @Override
        public SpeakItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_speak_item_layout, parent, false);
            return new SpeakItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(SpeakItemViewHolder holder, final int position) {
            holder.mDescriptionTv.setText(mSpeakItems.get(position).getDescription());
            Glide.with(mContext)
                    .load(mSpeakItems.get(position).getPhotoPath())
                    .centerCrop()
                    .thumbnail(0.5f)
                    .placeholder(R.color.colorAccent)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mThumbnailIv);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onClickSpeakItem(mSpeakItems.get(position).getId(), mSpeakItems.get(position).getCollectionId());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mSpeakItems.size();
        }
    }

    public class SpeakItemViewHolder extends RecyclerView.ViewHolder {

        private View mRootView;
        private TextView mLanguageTv;
        private ImageView mThumbnailIv;
        private TextView mDescriptionTv;

        public SpeakItemViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
            mDescriptionTv = (TextView) mRootView.findViewById(R.id.tv_description_id);
            mLanguageTv = (TextView) mRootView.findViewById(R.id.tv_lang_id);
            mThumbnailIv = (ImageView) mRootView.findViewById(R.id.iv_thumb_id);
        }
    }

    //section adapter for speak items
    class SpeakItemSection extends StatelessSection {

        private List<SpeakItem> speakItems;
        private String header;

        public SpeakItemSection(List<SpeakItem> speakItems, String header) {
            super(R.layout.header_section_list_layout, R.layout.item_speak_item_layout);
            this.speakItems = speakItems;
            this.header = header;
        }

        @Override
        public int getContentItemsTotal() {
            return speakItems.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new SpeakItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, final int i) {
            SpeakItemViewHolder holder = (SpeakItemViewHolder) viewHolder;
            holder.mDescriptionTv.setText(speakItems.get(i).getDescription());
            Glide.with(getActivity())
                    .load(speakItems.get(i).getPhotoPath())
                    .centerCrop()
                    .thumbnail(0.5f)
                    .placeholder(R.color.colorAccent)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mThumbnailIv);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onClickSpeakItem(speakItems.get(i).getId(), speakItems.get(i).getCollectionId());
                    }
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.tvTitle.setText(header);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;

        public HeaderViewHolder(View view) {
            super(view);

            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        }
    }
}

