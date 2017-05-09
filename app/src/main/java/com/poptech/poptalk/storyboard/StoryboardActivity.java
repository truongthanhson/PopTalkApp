package com.poptech.poptalk.storyboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.poptech.poptalk.R;
import com.poptech.poptalk.utils.ActivityUtils;

/**
 * Created by sontt on 09/05/2017.
 */

public class StoryboardActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_board);


        //setup Toolbar
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Story Board");

        showStoryBoard();
    }

    private void showStoryBoard(){
        StoryBoardFragment storyBoardFragment = StoryBoardFragment.newInstance();
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), storyBoardFragment, R.id.contentFrame);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}
