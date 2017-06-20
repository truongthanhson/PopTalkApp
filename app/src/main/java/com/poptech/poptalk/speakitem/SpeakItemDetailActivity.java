package com.poptech.poptalk.speakitem;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.ShareItem;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.collections.CollectionsActivity;
import com.poptech.poptalk.gallery.GalleryActivity;
import com.poptech.poptalk.location.LocationTracker;
import com.poptech.poptalk.provider.PopTalkDatabase;
import com.poptech.poptalk.provider.SpeakItemModel;
import com.poptech.poptalk.share.ShareActivity;

import java.io.File;
import java.util.List;


public class SpeakItemDetailActivity extends AppCompatActivity implements SpeakItemDetailFragment.SpeakItemDetailFragmentCallback, SpeakItemDetailDialogFragment.SpeakItemDetailDialogFragmentCallback {

    SpeakItemModel mModel;

    private Toolbar mToolBar;
    private ViewPager mSpeakItemPager;
    private SpeakItemsPagerAdapter mSpeakItemPagerAdapter;
    private long mCollectionId;
    private long mSpeakItemId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpeakItemId = getIntent().getLongExtra(Constants.KEY_SPEAK_ITEM_ID, -1);
        mCollectionId = getIntent().getLongExtra(Constants.KEY_COLLECTION_ID, -1);

        setContentView(R.layout.activity_speak_items);

        //setup Toolbar
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Speak Items");

        mModel = new SpeakItemModel(new PopTalkDatabase(PopTalkApplication.applicationContext));

        //setup viewpager
        mSpeakItemPager = (ViewPager) findViewById(R.id.speak_item_pager_id);
    }

    @Override
    public void onResume() {
        super.onResume();
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.INTERNET).withListener(new BasePermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                super.onPermissionGranted(response);
                List<SpeakItem> speakItems = mModel.getSpeakItems(mCollectionId);
                mSpeakItemPagerAdapter = new SpeakItemsPagerAdapter(getSupportFragmentManager(), speakItems);
                mSpeakItemPager.setAdapter(mSpeakItemPagerAdapter);
                mSpeakItemPager.setCurrentItem(getCurrentItem(speakItems));
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


    private int getCurrentItem(List<SpeakItem> speakItems) {
        for (int index = 0; index < speakItems.size(); index++) {
            if (speakItems.get(index).getId() == mSpeakItemId) {
                return index;
            }
        }
        return 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_more, menu);
        MenuItem moreItem = menu.findItem(R.id.action_more);
        if (moreItem != null) {
            moreItem.setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClickSpeakItemDialog(SpeakItem speakItem) {
        Bundle args = new Bundle();
        args.putParcelable(Constants.KEY_SPEAK_ITEM, speakItem);

        SpeakItemDetailDialogFragment speakItemDialogFragment = new SpeakItemDetailDialogFragment();
        speakItemDialogFragment.setArguments(args);
        speakItemDialogFragment.show(getSupportFragmentManager(), speakItemDialogFragment.getTag());
    }

    @Override
    public void onClickSpeakItemDialogDismiss(SpeakItem speakItem) {
        int currentItem = mSpeakItemPager.getCurrentItem();
        SpeakItemDetailFragment fragment = (SpeakItemDetailFragment) mSpeakItemPagerAdapter.getFragment(currentItem);
        if (fragment != null) {
            fragment.setSpeakItemFromDialog(speakItem);
        }
    }

    @Override
    public void onClickShare(SpeakItem speakItem) {
        showChooseShareMethodDialog(speakItem);
    }

    private void showChooseShareMethodDialog(SpeakItem speakItem) {
        ShareItem shareItem = new ShareItem();
        shareItem.setShareType(Constants.ShareType.SPEAK_ITEM);
        shareItem.setSpeakItem(speakItem);
        CharSequence choices[] = new CharSequence[]{"Share via Email", "Share via WiFi Direct"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose share method");
        builder.setItems(choices, (dialog, which) -> {
            if (which == 0) {
                String speakItemZipPath = ShareActivity.zipSpeakItem(shareItem);
                openSendEmail(speakItemZipPath);
            } else if (which == 1) {
                goToShareSpeakItemScreen(shareItem);
            }
        });
        builder.show();
    }

    private void openSendEmail(String speakItemZipPath) {
        File fileLocation = new File(speakItemZipPath);
        Uri path = Uri.fromFile(fileLocation);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // set the type to 'email'
        emailIntent.setType("vnd.android.cursor.dir/email");
        // the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    private void goToShareSpeakItemScreen(ShareItem shareItem) {

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.CHANGE_NETWORK_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new BaseMultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                super.onPermissionsChecked(report);
                if (report.areAllPermissionsGranted()) {
                    Intent intent = new Intent(SpeakItemDetailActivity.this, ShareActivity.class);
                    intent.putExtra(Constants.KEY_SHARE_ITEM, shareItem);
                    startActivity(intent);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                super.onPermissionRationaleShouldBeShown(permissions, token);
            }
        }).check();
    }

    public static class SpeakItemsPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> mFragments = new SparseArray<>();
        private List<SpeakItem> mSpeakItems;

        public SpeakItemsPagerAdapter(FragmentManager fragmentManager, List<SpeakItem> speakItems) {
            super(fragmentManager);
            this.mSpeakItems = speakItems;
        }

        @Override
        public int getCount() {
            return mSpeakItems.size();
        }

        @Override
        public Fragment getItem(int position) {
            return SpeakItemDetailFragment.newInstance(mSpeakItems.get(position).getId());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getFragment(int position) {
            return mFragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }
}
