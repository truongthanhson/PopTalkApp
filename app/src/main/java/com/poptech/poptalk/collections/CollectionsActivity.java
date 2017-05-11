package com.poptech.poptalk.collections;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Credentials;
import com.poptech.poptalk.drawer.DrawerMenuAdapter;
import com.poptech.poptalk.drawer.DrawerMenuDataFactory;
import com.poptech.poptalk.gallery.GalleryActivity;
import com.poptech.poptalk.login.LoginModel;
import com.poptech.poptalk.provider.PopTalkDatabase;
import com.poptech.poptalk.speakitem.SpeakItemDetailActivity;
import com.poptech.poptalk.storyboard.StoryboardActivity;
import com.poptech.poptalk.utils.ActivityUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sontt on 26/04/2017.
 */

public class CollectionsActivity extends AppCompatActivity implements View.OnClickListener, AppMenuOpen, CollectionsFragment.CollectionsFragmentCallback, SpeakItemsFragment.SpeakItemsFragmentCallback {
    private DrawerLayout mDrawerLayout;

    private RecyclerView mDrawerMenu;

    private FloatingActionButton mFloatingButton;

    private DrawerMenuAdapter mDrawerMenuAdapter;

    private CircleImageView mUserProfilePicture;

    private TextView mUserName;

    private LoginModel mLoginModel;

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
        mFloatingButton = (FloatingActionButton) findViewById(R.id.fab_add_speak_item);
        mFloatingButton.setOnClickListener(this);

        mUserProfilePicture = (CircleImageView) findViewById(R.id.user_avatar_id);
        mUserProfilePicture.setOnClickListener(this);

        mUserName = (TextView) findViewById(R.id.user_name_id);

        mLoginModel = new LoginModel(new PopTalkDatabase(PopTalkApplication.applicationContext));

        onLoadUserProfile();

        onNavigateToViewCollection();
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
            case R.id.user_avatar_id:
                navigateToChangeAvatar();
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
                    intent.putExtra(
                            Constants.KEY_PHOTO_GALLERY,
                            GalleryActivity.GalleryType.PICK_ADDED_SPEAK_ITEM);
                    startActivity(intent);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                super.onPermissionRationaleShouldBeShown(permissions, token);
            }
        }).check();
    }


    private void navigateToChangeAvatar() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new BaseMultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                super.onPermissionsChecked(report);
                if (report.areAllPermissionsGranted()) {
                    Intent intent = new Intent(CollectionsActivity.this, GalleryActivity.class);
                    intent.putExtra(
                            Constants.KEY_PHOTO_GALLERY,
                            GalleryActivity.GalleryType.PICK_PROFILE_PICTURE);
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
        if (requestCode == GalleryActivity.SELECT_PHOTO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String path = data.getExtras().getString(Constants.KEY_PHOTO_GALLERY_RESULT);
                updateUserProfile(path);
                setUserProfilePicture(path);
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
        mDrawerLayout.closeDrawers();

        CollectionsFragment collectionsFragment = CollectionsFragment.newInstance();
        ActivityUtils.replaceFragmentToActivity(
                getSupportFragmentManager(), collectionsFragment, R.id.contentFrame);
    }

    @Override
    public void onNavigateToViewList() {
        mDrawerLayout.closeDrawers();

        // Create the fragment
        SpeakItemsFragment speakItemsFragment = SpeakItemsFragment.newInstance();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_VIEW_TYPE, SpeakItemsFragment.GroupSpeakItemViewType.LIST);
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_SORT_TYPE, SpeakItemsFragment.GroupSpeakItemSortType.RECENT);
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
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_VIEW_TYPE, SpeakItemsFragment.GroupSpeakItemViewType.GRID);
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_SORT_TYPE, SpeakItemsFragment.GroupSpeakItemSortType.RECENT);
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
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_VIEW_TYPE, SpeakItemsFragment.GroupSpeakItemViewType.LIST);
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_SORT_TYPE, SpeakItemsFragment.GroupSpeakItemSortType.DESCRIPTION);
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
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_VIEW_TYPE, SpeakItemsFragment.GroupSpeakItemViewType.LIST);
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_SORT_TYPE, SpeakItemsFragment.GroupSpeakItemSortType.LANGUAGE);
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
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_VIEW_TYPE, SpeakItemsFragment.GroupSpeakItemViewType.LIST);
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_SORT_TYPE, SpeakItemsFragment.GroupSpeakItemSortType.RECENT);
        speakItemsFragment.setArguments(bundle);

        ActivityUtils.replaceFragmentToActivity(
                getSupportFragmentManager(), speakItemsFragment, R.id.contentFrame);
    }

    @Override
    public void onNavigateStoryboardMap() {
        Intent intent = new Intent(this, StoryboardActivity.class);
        startActivity(intent);
    }

    @Override
    public void onNavigateStoryboardFrequency() {

    }

    @Override
    public void onClickCollections(long collectionId) {
        navigateToCollectionDetailScreen(collectionId);
    }

    private void navigateToCollectionDetailScreen(long collectionId) {
        Intent intent = new Intent(this, CollectionDetailActivity.class);
        intent.putExtra(Constants.KEY_COLLECTION_ID, collectionId);
        startActivity(intent);
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

    private void onLoadUserProfile() {
        Credentials credentials = mLoginModel.getCredentials();
        setUserProfilePicture(credentials.getProfilePicture());
        setUserName(credentials.getName());
    }

    private void updateUserProfile(String path) {
        Credentials credentials = mLoginModel.getCredentials();
        credentials.setProfilePicture(path);
        mLoginModel.updateCredentials(credentials);
    }

    private void setUserProfilePicture(String path) {
        Glide.with(PopTalkApplication.applicationContext)
                .load(path)
                .centerCrop()
                .dontAnimate()
                .thumbnail(0.5f)
                .placeholder(R.color.colorAccent)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mUserProfilePicture);
    }

    private void setUserName(String name) {
        mUserName.setText(name);
    }
}
