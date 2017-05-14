package com.poptech.poptalk.speakitem;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.provider.PopTalkDatabase;
import com.poptech.poptalk.provider.SpeakItemModel;

import java.util.List;


public class SpeakItemDetailActivity extends AppCompatActivity implements SpeakItemDetailFragment.SpeakItemDetailFragmentCallback, SpeakItemDetailDialogFragment.SpeakItemDetailDialogFragmentCallback {

    SpeakItemModel mModel;

    private Toolbar mToolBar;
    private ViewPager mSpeakItemPager;
    private SpeakItemsPagerAdapter mSpeakItemPagerAdapter;
    private int mPagerIndex;
    private long mCollectionId;
    private long mSpeakItemId;
    private List<SpeakItem> mSpeakItems;


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
        mSpeakItems = mModel.getSpeakItems(mCollectionId);
        for (int i = 0; i < mSpeakItems.size(); i++) {
            if (mSpeakItems.get(i).getId() == mSpeakItemId) {
                mPagerIndex = i;
                break;
            }
        }
        //setup viewpager
        mSpeakItemPager = (ViewPager) findViewById(R.id.speak_item_pager_id);
        mSpeakItemPagerAdapter = new SpeakItemsPagerAdapter(getSupportFragmentManager(), mModel.getSpeakItems(mCollectionId));
        mSpeakItemPager.setAdapter(mSpeakItemPagerAdapter);
        mSpeakItemPager.setCurrentItem(mPagerIndex);
    }

    @Override
    public void onResume() {
        super.onResume();
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem cameraItem = menu.findItem(R.id.action_camera);
        if (cameraItem != null) {
            cameraItem.setVisible(false);
        }
        MenuItem plusItem = menu.findItem(R.id.action_plus);
        if (plusItem != null) {
            plusItem.setVisible(false);
        }

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchItem.setVisible(false);
        }

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
