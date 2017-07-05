package com.poptech.poptalk.storyboard;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.ShareItem;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.bean.StoryBoard;
import com.poptech.poptalk.share.ShareActivity;
import com.poptech.poptalk.utils.ActivityUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sontt on 09/05/2017.
 */

public class StoryboardActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolBar;
    private StoryBoard mStoryBoard;

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

        if (getIntent() != null) {
            mStoryBoard = getIntent().getParcelableExtra("storyboard");
        }
        showStoryBoard((ArrayList<SpeakItem>) mStoryBoard.getSpeakItems());
    }

    private void showStoryBoard(ArrayList<SpeakItem> speakItems) {
        StoryBoardFragment storyBoardFragment = StoryBoardFragment.newInstance();
        Bundle args = new Bundle();
        args.putParcelableArrayList("selected_speak_items", speakItems);
        storyBoardFragment.setArguments(args);
        ActivityUtils.addFragmentToActivity(
                getSupportFragmentManager(), storyBoardFragment, R.id.contentFrame);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.action_share) {
            showChooseShareMethodDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChooseShareMethodDialog() {
        ShareItem shareItem = new ShareItem();
        shareItem.setType(Constants.ShareType.STORY_BOARD);
        shareItem.setStoryBoard(mStoryBoard);
        CharSequence choices[] = new CharSequence[]{"Share via Email", "Share via WiFi Direct"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.PopTalk_AlertDialog);
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
                    Intent intent = new Intent(StoryboardActivity.this, ShareActivity.class);
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

}
