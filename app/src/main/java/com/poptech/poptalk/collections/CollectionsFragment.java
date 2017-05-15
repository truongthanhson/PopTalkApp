package com.poptech.poptalk.collections;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
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
import com.poptech.poptalk.utils.StringUtils;
import com.poptech.poptalk.utils.Utils;
import com.poptech.poptalk.view.ItemDecorationColumns;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import javax.inject.Inject;

/**
 * Created by sontt on 26/04/2017.
 */

public class CollectionsFragment extends Fragment implements CollectionsContract.View {

    public interface CollectionsFragmentCallback {
        void onClickCollections(long collectionId);
    }

    private View mView;
    private RecyclerView mCollectionsView;
    private CollectionsFragmentCallback callback;
    @Inject
    CollectionsPresenter mPresenter;

    public CollectionsFragment() {
        // Requires empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CollectionsFragmentCallback) {
            callback = (CollectionsFragmentCallback) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the presenter
        DaggerCollectionsComponent.builder()
                .appComponent(((PopTalkApplication) PopTalkApplication.applicationContext).getAppComponent())
                .collectionsPresenterModule(new CollectionsPresenterModule(this)).build().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_collections_layout, container, false);
        initView();
        return mView;
    }

    private void initView() {
        mCollectionsView = (RecyclerView) mView.findViewById(R.id.colletions_list);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        ItemDecorationColumns itemDecoration = new ItemDecorationColumns(2, getResources().getDimensionPixelSize(R.dimen.grid_divider), true);
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
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.INTERNET).withListener(new BasePermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                super.onPermissionGranted(response);
                mPresenter.loadCollections();
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

    public static CollectionsFragment newInstance() {
        return new CollectionsFragment();
    }

    @Override
    public void setPresenter(CollectionsContract.Presenter presenter) {
        mPresenter = (CollectionsPresenter) presenter;
    }

    @Override
    public void onCollectionsLoaded(List<Collection> collections) {
        List<Collection> newCollections = new ArrayList<>();
        for (Collection collection : collections) {
            if (collection.getNumSpeakItem() > 0) {
                newCollections.add(collection);
            }
        }
        CollectionsAdapter adapter = new CollectionsAdapter(newCollections, getActivity());
        mCollectionsView.setAdapter(adapter);
    }

    public class CollectionsAdapter extends RecyclerView.Adapter<CollectionViewHolder> {
        private List<Collection> mCollections;
        private Context mContext;

        public CollectionsAdapter(List<Collection> collections, Context context) {
            this.mCollections = collections;
            this.mContext = context;
        }

        @Override
        public CollectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_collection_layout, parent, false);
            return new CollectionViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(CollectionViewHolder holder, final int position) {
            holder.mDescriptionTv.setText(mCollections.get(position).getDescription());
            holder.mLanguageTv.setText(mCollections.get(position).getLanguage());

            Glide.with(mContext)
                    .load(mCollections.get(position).getThumbPath())
                    .centerCrop()
                    .thumbnail(0.5f)
                    .placeholder(R.color.colorAccent)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mThumbnailIv);

            holder.mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) {
                        callback.onClickCollections(mCollections.get(position).getId());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mCollections.size();
        }
    }

    public class CollectionViewHolder extends RecyclerView.ViewHolder {

        private View mRootView;
        private TextView mLanguageTv;
        private ImageView mThumbnailIv;
        private TextView mDescriptionTv;

        public CollectionViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
            mDescriptionTv = (TextView) mRootView.findViewById(R.id.tv_description_id);
            mLanguageTv = (TextView) mRootView.findViewById(R.id.tv_lang_id);
            mThumbnailIv = (ImageView) mRootView.findViewById(R.id.iv_thumb_id);
        }
    }
}

