package com.poptech.poptalk.storyboard;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.StoryBoard;
import com.poptech.poptalk.utils.ActivityUtils;
import com.poptech.poptalk.utils.AndroidUtilities;

/**
 * Created by sontt on 09/05/2017.
 */

public class StoryboardSelecActivity extends AppCompatActivity implements StoryBoardSelectFragment.StoryBoardSelectFragmentCallBack {
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
        getSupportActionBar().setTitle("Story Board Select");

        showAllSpeakItems();
    }

    private void showAllSpeakItems(){
        StoryBoardSelectFragment storyBoardFragment = StoryBoardSelectFragment.newInstance();
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), storyBoardFragment, R.id.contentFrame);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_build, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }else if (item.getItemId() == R.id.action_build) {
            showInputStoryBoardName();

        }
        return super.onOptionsItemSelected(item);
    }

    private void buildStoryBoard() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if(fragment == null)
            return;
        if(fragment instanceof StoryBoardSelectFragment){
            ((StoryBoardSelectFragment)fragment).buildStoryBoard(mStoryBoardName);
        }

    }

    private void showInputStoryBoardName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.PopTalk_AlertDialog);
        builder.setTitle("Set storyboard name");
        builder.setIcon(R.drawable.ic_add_circle);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.item_edittext_dialog, null);
        final EditText editText = (EditText) convertView.findViewById(R.id.edit_text_id);
        builder.setView(convertView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mStoryBoardName = editText.getText().toString();
                buildStoryBoard();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(false);

        builder.show();
    }

    private String mStoryBoardName = "";
    @Override
    public void onStoryBoardBuilt(StoryBoard storyBoard) {
        Intent intent = new Intent(this, StoryboardActivity.class);
        intent.putExtra("storyboard", storyBoard);
        startActivity(intent);
    }
}
