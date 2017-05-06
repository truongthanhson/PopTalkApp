package com.poptech.poptalk.speakitem;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.poptech.poptalk.R;

/**
 * Created by sontt on 30/04/2017.
 */

public class SpeakItemsDetailActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ViewPager mSpeakItemPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speak_items);

        //setup Toolbar
        mToolBar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Speak Items");

        //setup viewpager
        mSpeakItemPager = (ViewPager)findViewById(R.id.speak_item_pager_id);
        mSpeakItemPager.setAdapter(new SpeakItemsPagerAdapter(getSupportFragmentManager()));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SpeakItemsPagerAdapter extends FragmentStatePagerAdapter {
        private static int NUM_ITEMS = 3;

        public SpeakItemsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SpeakItemDetailFragment.newInstance(0);
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return SpeakItemDetailFragment.newInstance(1);
                case 2: // Fragment # 1 - This will show SecondFragment
                    return SpeakItemDetailFragment.newInstance(2);
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }
}
