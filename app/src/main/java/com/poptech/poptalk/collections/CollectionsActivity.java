package com.poptech.poptalk.collections;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.drawer.DrawerMenuAdapter;
import com.poptech.poptalk.drawer.DrawerMenuDataFactory;
import com.poptech.poptalk.gallery.GalleryActivity;
import com.poptech.poptalk.provider.CollectionsModel;
import com.poptech.poptalk.provider.SpeakItemModel;
import com.poptech.poptalk.utils.ActivityUtils;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 26/04/2017.
 */

public class CollectionsActivity extends AppCompatActivity implements View.OnClickListener, AppMenuOpen {
    private DrawerLayout mDrawerLayout;

    private RecyclerView mDrawerMenu;

    private FloatingActionButton mFloatingButton;

    private DrawerMenuAdapter mDrawerMenuAdapter;

    @Inject
    CollectionsPresenter mPresenter;

    @Inject
    CollectionsModel mCollectionModel;

    @Inject
    SpeakItemModel mSpeakItemModel;

//    @Inject
//    SpeakItemPresenter speakItemPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        // Set up the navigation drawer.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupNavigationBar();
        }

        //setup floating button
        mFloatingButton = (FloatingActionButton)findViewById(R.id.fab_add_speak_item);
        mFloatingButton.setOnClickListener(this);

        CollectionsFragment collectionsFragment =
                (CollectionsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (collectionsFragment == null) {
            // Create the fragment
            collectionsFragment = CollectionsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), collectionsFragment, R.id.contentFrame);
        }

        // Create the presenter
        DaggerCollectionsComponent.builder()
                .appComponent(((PopTalkApplication) PopTalkApplication.applicationContext).getAppComponent())
                .collectionsPresenterModule(new CollectionsPresenterModule(collectionsFragment)).build().inject(this);

        //addTestData
//        mCollectionModel.generateTestData();
//        mSpeakItemModel.generateTestData();
    }

    private void setupNavigationBar() {
        mDrawerMenu = (RecyclerView) findViewById(R.id.drawer_menu);

        // RecyclerView has some built in animations to it, using the DefaultItemAnimator.
        // Specifically when you call notifyItemChanged() it does a fade animation for the changing
        // of the data in the ViewHolder. If you would like to disable this you can use the following:
        RecyclerView.ItemAnimator animator = mDrawerMenu.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        mDrawerMenu.setLayoutManager(new LinearLayoutManager(this));
        mDrawerMenuAdapter = new DrawerMenuAdapter(DrawerMenuDataFactory.makeDrawerMenu(), this);
        mDrawerMenu.setAdapter(mDrawerMenuAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add_speak_item:
                navigateToAddSpeakItem();
                break;
            default:
                break;
        }
    }

    private void navigateToAddSpeakItem() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new BaseMultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                super.onPermissionsChecked(report);
                if (report.areAllPermissionsGranted()) {
                    Intent intent = new Intent(CollectionsActivity.this, GalleryActivity.class);
                    startActivityForResult(intent, GalleryActivity.SELECT_PHOTO_REQUEST_CODE);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                super.onPermissionRationaleShouldBeShown(permissions, token);
            }
        }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GalleryActivity.SELECT_PHOTO_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                String path = data.getExtras().getString("croppedPath");
                Snackbar.make(mDrawerLayout,path,Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDrawerMenuAdapter.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDrawerMenuAdapter.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onNavigateToViewCollection() {
        Toast.makeText(this, "onNavigateToViewCollection", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNavigateToViewList() {
        mDrawerLayout.closeDrawers();

        // Create the fragment
        SpeakItemsFragment speakItemsFragment = SpeakItemsFragment.newInstance();

        Bundle bundle = new Bundle();
        bundle.putSerializable(SpeakItemsFragment.KEY_SPEAK_ITEM_VIEW_TYPE, SpeakItemsFragment.GroupSpeakItemViewType.LIST);
        bundle.putSerializable(SpeakItemsFragment.KEY_SPEAK_ITEM_SORT_TYPE, SpeakItemsFragment.GroupSpeakItemSortType.RECENT);
        speakItemsFragment.setArguments(bundle);

        ActivityUtils.replaceFragmentToActivity(
                getSupportFragmentManager(), speakItemsFragment, R.id.contentFrame);
    }

    @Override
    public void onNavigateToViewLocation() {
        mDrawerLayout.closeDrawers();

        // Create the fragment
        SpeakItemsFragment speakItemsFragment = SpeakItemsFragment.newInstance();

        Bundle bundle = new Bundle();
        bundle.putSerializable(SpeakItemsFragment.KEY_SPEAK_ITEM_VIEW_TYPE, SpeakItemsFragment.GroupSpeakItemViewType.GRID);
        bundle.putSerializable(SpeakItemsFragment.KEY_SPEAK_ITEM_SORT_TYPE, SpeakItemsFragment.GroupSpeakItemSortType.RECENT);
        speakItemsFragment.setArguments(bundle);

        ActivityUtils.replaceFragmentToActivity(
                getSupportFragmentManager(), speakItemsFragment, R.id.contentFrame);
    }

    @Override
    public void onNavigateToSortByDescription() {
        mDrawerLayout.closeDrawers();

        // Create the fragment
        SpeakItemsFragment speakItemsFragment = SpeakItemsFragment.newInstance();

        Bundle bundle = new Bundle();
        bundle.putSerializable(SpeakItemsFragment.KEY_SPEAK_ITEM_VIEW_TYPE, SpeakItemsFragment.GroupSpeakItemViewType.LIST);
        bundle.putSerializable(SpeakItemsFragment.KEY_SPEAK_ITEM_SORT_TYPE, SpeakItemsFragment.GroupSpeakItemSortType.DESCRIPTION);
        speakItemsFragment.setArguments(bundle);

        ActivityUtils.replaceFragmentToActivity(
                getSupportFragmentManager(), speakItemsFragment, R.id.contentFrame);
    }

    @Override
    public void onNavigateToSortByLanguage() {
        mDrawerLayout.closeDrawers();

        // Create the fragment
        SpeakItemsFragment speakItemsFragment = SpeakItemsFragment.newInstance();

        Bundle bundle = new Bundle();
        bundle.putSerializable(SpeakItemsFragment.KEY_SPEAK_ITEM_VIEW_TYPE, SpeakItemsFragment.GroupSpeakItemViewType.LIST);
        bundle.putSerializable(SpeakItemsFragment.KEY_SPEAK_ITEM_SORT_TYPE, SpeakItemsFragment.GroupSpeakItemSortType.LANGUAGE);
        speakItemsFragment.setArguments(bundle);

        ActivityUtils.replaceFragmentToActivity(
                getSupportFragmentManager(), speakItemsFragment, R.id.contentFrame);
    }

    @Override
    public void onNavigateToSortByRecent() {
        mDrawerLayout.closeDrawers();

        // Create the fragment
        SpeakItemsFragment speakItemsFragment = SpeakItemsFragment.newInstance();

        Bundle bundle = new Bundle();
        bundle.putSerializable(SpeakItemsFragment.KEY_SPEAK_ITEM_VIEW_TYPE, SpeakItemsFragment.GroupSpeakItemViewType.LIST);
        bundle.putSerializable(SpeakItemsFragment.KEY_SPEAK_ITEM_SORT_TYPE, SpeakItemsFragment.GroupSpeakItemSortType.RECENT);
        speakItemsFragment.setArguments(bundle);

        ActivityUtils.replaceFragmentToActivity(
                getSupportFragmentManager(), speakItemsFragment, R.id.contentFrame);
    }

    @Override
    public void onNavigateStoryboardMap() {

    }

    @Override
    public void onNavigateStoryboardFrequency() {

    }
}
