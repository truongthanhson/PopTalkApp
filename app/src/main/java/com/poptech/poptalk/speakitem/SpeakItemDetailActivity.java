package com.poptech.poptalk.speakitem;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.provider.PopTalkDatabase;
import com.poptech.poptalk.provider.SpeakItemModel;

import java.util.List;


public class SpeakItemDetailActivity extends AppCompatActivity {

    SpeakItemModel mModel;

    private Toolbar mToolBar;
    private ViewPager mSpeakItemPager;
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
        mSpeakItemPager.setAdapter(new SpeakItemsPagerAdapter(getSupportFragmentManager(), mModel.getSpeakItems(mCollectionId)));
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
        } else if (item.getItemId() == R.id.action_more) {

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

    public static class SpeakItemsPagerAdapter extends FragmentStatePagerAdapter {
        private List<SpeakItem> mSpeakItems;

        public SpeakItemsPagerAdapter(FragmentManager fragmentManager, List<SpeakItem> speakItems) {
            super(fragmentManager);
            this.mSpeakItems = speakItems;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return mSpeakItems.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return SpeakItemDetailFragment.newInstance(mSpeakItems.get(position).getId());
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }
}
