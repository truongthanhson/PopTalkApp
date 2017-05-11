
package com.poptech.poptalk.gallery;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.poptech.poptalk.Constants;
import com.poptech.poptalk.R;
import com.poptech.poptalk.utils.MetricUtils;
import com.poptech.poptalk.view.ItemDecorationColumns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PhotoLGalleryFragment extends Fragment implements OnClickListener {
    private View mView;
    private String mItemId;
    RecyclerView recyclerView;
    CompositeDisposable disposable = new CompositeDisposable();
    private static final String TAG = "PhotoGalleryFragment";

    public static PhotoLGalleryFragment newInstance() {
        return new PhotoLGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mItemId = args.getString(Constants.KEY_PHOTO_GALLERY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_photo_list_layout, container, false);
        initView();
        return mView;
    }

    private void initView() {
        recyclerView = (RecyclerView) mView.findViewById(R.id.photo_recycle_id);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), Constants.GRID_COLUMN_COUNT);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new ItemDecorationColumns(3, MetricUtils.dpToPx(5), true));
    }

    @Override
    public void onResume() {
        super.onResume();
        Disposable subscription = queryPhotos()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Exception {
                        PhotoGalleryAdapter adapter = new PhotoGalleryAdapter(getActivity(), mItemId, (ArrayList<String>) strings);
                        recyclerView.setAdapter(adapter);
                    }
                });
        disposable.add(subscription);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    public void onClick(View v) {
    }

    public ArrayList<String> getAllImages(Context context) {
        ArrayList<Pair<Long, String>> imagePairs = new ArrayList<>();
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA
        };
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(images, projection, null, null, null);
        if (cursor.moveToFirst()) {
            String date, data;
            int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                date = cursor.getString(dateColumn);
                data = cursor.getString(dataColumn);
                imagePairs.add(new Pair<>(Long.valueOf(date), data));
            } while (cursor.moveToNext());
        }

        Collections.sort(imagePairs, new Comparator<Pair<Long, String>>() {
            @Override
            public int compare(Pair<Long, String> lhs, Pair<Long, String> rhs) {
                return Long.valueOf(rhs.first).compareTo(Long.valueOf(lhs.first));
            }
        });

        ArrayList<String> imageUrls = new ArrayList<>();
        for (int i = 0; i < imagePairs.size(); i++) {
            imageUrls.add(imagePairs.get(i).second);
        }
        return imageUrls;
    }

    public Observable<List<String>> queryPhotos() {
        return Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> e) throws Exception {
                e.onNext(getAllImages(getActivity()));
                e.onComplete();
            }
        });
    }
}
