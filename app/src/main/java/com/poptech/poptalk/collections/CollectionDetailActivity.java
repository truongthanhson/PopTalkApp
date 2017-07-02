package com.poptech.poptalk.collections;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.ShareItem;
import com.poptech.poptalk.share.ShareActivity;
import com.poptech.poptalk.speakitem.SpeakItemDetailActivity;
import com.poptech.poptalk.storyboard.StoryboardActivity;
import com.poptech.poptalk.utils.ActivityUtils;

import java.io.File;
import java.util.List;

/**
 * Created by sontt on 06/05/2017.
 */

public class CollectionDetailActivity extends AppCompatActivity implements SpeakItemsFragment.SpeakItemsFragmentCallback {
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle("Collection Detail");

        showCollectionDetail();
    }

    private void showCollectionDetail() {
        SpeakItemsFragment speakItemsFragment = SpeakItemsFragment.newInstance();

        Bundle bundle = new Bundle();
        bundle.putLong(Constants.KEY_COLLECTION_ID, getIntent().getLongExtra(Constants.KEY_COLLECTION_ID, -1));
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_VIEW_TYPE, SpeakItemsFragment.GroupSpeakItemViewType.LIST);
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_SORT_TYPE, SpeakItemsFragment.GroupSpeakItemSortType.NONE);
        speakItemsFragment.setArguments(bundle);

        ActivityUtils.replaceFragmentToActivity(
                getSupportFragmentManager(), speakItemsFragment, R.id.container_body);
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChooseShareMethodDialog(Collection collection) {
        ShareItem shareItem = new ShareItem();
        shareItem.setType(Constants.ShareType.COLLECTION);
        shareItem.setCollection(collection);
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
                    Intent intent = new Intent(CollectionDetailActivity.this, ShareActivity.class);
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

    @Override
    public void onClickSpeakItem(long speakItemId, long collectionId) {
        openSpeakItemDetailScreen(speakItemId, collectionId);
    }

    @Override
    public void onClickShareItem(Collection collection) {
        if (collection != null) {
            showChooseShareMethodDialog(collection);
        }
    }

    public void openSpeakItemDetailScreen(long speakItemId, long collectionId) {
        Intent intent = new Intent(this, SpeakItemDetailActivity.class);
        intent.putExtra(Constants.KEY_SPEAK_ITEM_ID, speakItemId);
        intent.putExtra(Constants.KEY_COLLECTION_ID, collectionId);
        startActivity(intent);
    }
}
