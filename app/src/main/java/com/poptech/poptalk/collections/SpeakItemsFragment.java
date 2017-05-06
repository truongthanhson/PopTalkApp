package com.poptech.poptalk.collections;


import android.Manifest;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.SpeakItem;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 26/04/2017.
 */

public class SpeakItemsFragment extends Fragment implements SpeakItemsContract.View{

    private View mView;
    private RecyclerView mSpeakItemsView;
    @Inject
    SpeakItemPresenter mPresenter;

    public SpeakItemsFragment() {
        // Requires empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the presenter

        DaggerSpeakItemComponent.builder().appComponent(((PopTalkApplication)PopTalkApplication.applicationContext).getAppComponent()).
                speakItemsPresenterModule(new SpeakItemsPresenterModule(this)).build().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_speak_items_layout,container,false);
        initView();
        return mView;
    }

    private void initView() {
        mSpeakItemsView = (RecyclerView) mView.findViewById(R.id.speak_item_list);
        mSpeakItemsView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
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
                mPresenter.loadSpeakItems();
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
    public void onSpeakItemsLoaded(List<SpeakItem> speakItems) {
        SpeakItemsAdapter speakItemsAdapter = new SpeakItemsAdapter(speakItems,getActivity());
        mSpeakItemsView.setAdapter(speakItemsAdapter);
    }

    @Override
    public void setPresenter(SpeakItemsContract.Presenter presenter) {
        this.mPresenter = (SpeakItemPresenter) presenter;
    }

    public class SpeakItemsAdapter extends RecyclerView.Adapter<SpeakItemViewHolder>{
        private List<SpeakItem> mCollections;
        private Context mContext;

        public SpeakItemsAdapter(List<SpeakItem> collections, Context context) {
            this.mCollections = collections;
            this.mContext = context;
        }

        @Override
        public SpeakItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_collection_layout, parent, false);
            return new SpeakItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(SpeakItemViewHolder holder, int position) {
            holder.mDescriptionTv.setText(mCollections.get(position).getDescription());
            Glide.with(mContext)
                    .load(mCollections.get(position).getPhotoPath())
                    .centerCrop()
                    .thumbnail(0.5f)
                    .placeholder(R.color.colorAccent)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mThumbnailIv);
        }

        @Override
        public int getItemCount() {
            return mCollections.size();
        }
    }

    public class SpeakItemViewHolder extends RecyclerView.ViewHolder{

        private View mRootView;
        private TextView mLanguageTv;
        private ImageView mThumbnailIv;
        private TextView mDescriptionTv;
        public SpeakItemViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
            mDescriptionTv = (TextView)mRootView.findViewById(R.id.tv_description_id);
            mLanguageTv = (TextView)mRootView.findViewById(R.id.tv_lang_id);
            mThumbnailIv = (ImageView)mRootView.findViewById(R.id.iv_thumb_id);
        }
    }
}

