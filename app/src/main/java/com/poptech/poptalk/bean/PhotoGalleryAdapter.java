package com.poptech.poptalk.bean;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andexert.library.RippleView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.poptech.poptalk.R;
import com.poptech.poptalk.gallery.GalleryActivity;
import com.poptech.poptalk.utils.StringUtils;
import com.poptech.poptalk.utils.Utils;

import java.util.ArrayList;

public class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoGalleryAdapter.ViewHolder> {
    private ArrayList<String> mPhotoList;
    private Context mContext;
    private String mPhotoId;

    public PhotoGalleryAdapter(Context context, String photo_id, ArrayList<String> photoList) {
        this.mPhotoList = photoList;
        this.mPhotoId = photo_id;
        this.mContext = context;
    }

    @Override
    public PhotoGalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_photo_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoGalleryAdapter.ViewHolder viewHolder, int position) {
        GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams) viewHolder.view.getLayoutParams();
        if(params != null){
            viewHolder.view.setLayoutParams(new GridLayoutManager.LayoutParams(Utils.getDisplayWidth((Activity) mContext) / 3,Utils.getDisplayWidth((Activity) mContext) / 3));
        }else{
            params = new GridLayoutManager.LayoutParams(Utils.getDisplayWidth((Activity) mContext) / 3,Utils.getDisplayWidth((Activity) mContext) / 3);
            viewHolder.view.setLayoutParams(params);
        }

        if (!StringUtils.isNullOrEmpty(mPhotoList.get(position))) {
            Glide.with(mContext)
                    .load(mPhotoList.get(position))
                    .centerCrop()
                    .override(Utils.getDisplayWidth((Activity) mContext) / 3, Utils.getDisplayWidth((Activity) mContext) / 3)
                    .centerCrop()
                    .thumbnail(0.5f)
                    .placeholder(R.color.white)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(viewHolder.mPhotoView);
        }
    }

    @Override
    public int getItemCount() {
        return mPhotoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View view;
        private ImageView mPhotoView;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            mPhotoView = (ImageView) view.findViewById(R.id.photo_img_id);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String selectedPhoto = mPhotoList.get(getAdapterPosition());
            if(mContext instanceof GalleryActivity){
                ((GalleryActivity)mContext).startActivityCrop(selectedPhoto);
            }
        }
    }
}
