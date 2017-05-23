package com.poptech.poptalk.collections;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.poptech.poptalk.Constants;
import com.poptech.poptalk.R;
import com.poptech.poptalk.speakitem.SpeakItemDetailActivity;
import com.poptech.poptalk.utils.ActivityUtils;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickSpeakItem(long speakItemId, long collectionId) {
        openSpeakItemDetailScreen(speakItemId, collectionId);
    }

    public void openSpeakItemDetailScreen(long speakItemId, long collectionId) {
        Intent intent = new Intent(this, SpeakItemDetailActivity.class);
        intent.putExtra(Constants.KEY_SPEAK_ITEM_ID, speakItemId);
        intent.putExtra(Constants.KEY_COLLECTION_ID, collectionId);
        startActivity(intent);
    }
}
