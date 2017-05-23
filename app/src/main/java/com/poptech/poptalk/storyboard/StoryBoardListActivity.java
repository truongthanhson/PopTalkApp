package com.poptech.poptalk.storyboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.StoryBoard;
import com.poptech.poptalk.utils.ActivityUtils;

/**
 * Created by sontt on 23/05/2017.
 */

public class StoryBoardListActivity extends AppCompatActivity implements View.OnClickListener, StoryBoardListFragment.StoryBoardListFragmentCallback {
    private Toolbar mToolbar;
    private FloatingActionButton mFloatingButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storyboard_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle("StoryBoards");

        //setup floating button
        mFloatingButton = (FloatingActionButton) findViewById(R.id.fab_add_new_story_board);
        mFloatingButton.setOnClickListener(this);

        showAllStoryBoard();
    }

    private void showAllStoryBoard() {
        StoryBoardListFragment storyBoardListFragment = StoryBoardListFragment.newInstance();

        ActivityUtils.replaceFragmentToActivity(
                getSupportFragmentManager(), storyBoardListFragment, R.id.container_body);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add_new_story_board:
                goToCreateStoryBoardScreen();
                break;
            default:
                break;
        }
    }

    private void goToCreateStoryBoardScreen() {
        Intent intent = new Intent(this, StoryboardSelecActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClickStoryBoard(StoryBoard storyBoard) {
        Intent intent = new Intent(this, StoryboardActivity.class);
        intent.putExtra("storyboard", storyBoard);
        startActivity(intent);
    }
}
