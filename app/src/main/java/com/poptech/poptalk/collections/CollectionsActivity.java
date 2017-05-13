package com.poptech.poptalk.collections;

import android.Manifest;
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
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.Credentials;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.drawer.DrawerMenuAdapter;
import com.poptech.poptalk.drawer.DrawerMenuDataFactory;
import com.poptech.poptalk.gallery.GalleryActivity;
import com.poptech.poptalk.login.LoginActivity;
import com.poptech.poptalk.login.LoginModel;
import com.poptech.poptalk.provider.CollectionsModel;
import com.poptech.poptalk.provider.PopTalkDatabase;
import com.poptech.poptalk.provider.SpeakItemModel;
import com.poptech.poptalk.speakitem.SpeakItemDetailActivity;
import com.poptech.poptalk.storyboard.StoryBoardSelectFragment;
import com.poptech.poptalk.storyboard.StoryboardActivity;
import com.poptech.poptalk.storyboard.StoryboardSelecActivity;
import com.poptech.poptalk.utils.ActivityUtils;
import com.poptech.poptalk.utils.SaveData;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sontt on 26/04/2017.
 */

public class CollectionsActivity extends AppCompatActivity implements View.OnClickListener, AppMenuOpen, CollectionsFragment.CollectionsFragmentCallback, SpeakItemsFragment.SpeakItemsFragmentCallback {
    private DrawerLayout mDrawerLayout;

    private RecyclerView mDrawerMenu;

    private FloatingActionButton mFloatingButton;

    private DrawerMenuAdapter mDrawerMenuAdapter;

    private CircleImageView mProfilePicture;

    private TextView mUserName;

    private ImageButton mLogoutButton;

    private LoginModel mLoginModel;
    private SpeakItemModel mSpeakItemModel;
    private CollectionsModel mCollectionModel;


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

        mProfilePicture = (CircleImageView) findViewById(R.id.user_avatar_id);
        mProfilePicture.setOnClickListener(this);

        mUserName = (TextView) findViewById(R.id.user_name_id);

        mLogoutButton = (ImageButton) findViewById(R.id.logout_button_id);
        mLogoutButton.setOnClickListener(this);

        PopTalkDatabase database = new PopTalkDatabase(PopTalkApplication.applicationContext);
        mSpeakItemModel = new SpeakItemModel(database);
        mCollectionModel = new CollectionsModel(database);
        mLoginModel = new LoginModel(database);

        onNavigateToViewCollection();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.INTERNET).withListener(new BasePermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                super.onPermissionGranted(response);
                onLoadUserProfile();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                super.onPermissionDenied(response);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                super.onPermissionRationaleShouldBeShown(permission, token);
            }
        }).check();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
            case R.id.logout_button_id:
                logoutUser();
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
                            GalleryActivity.GALLERY_RESULT_SPEAK_ITEM);
                    startActivityForResult(intent, GalleryActivity.GALLERY_REQUEST_CODE);
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
                    intent.putExtra(Constants.KEY_PHOTO_GALLERY, GalleryActivity.GALLERY_RESULT_PICK_PHOTO);
                    startActivityForResult(intent, GalleryActivity.GALLERY_REQUEST_CODE);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                super.onPermissionRationaleShouldBeShown(permissions, token);
            }
        }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == GalleryActivity.GALLERY_REQUEST_CODE) {
            if (resultCode == GalleryActivity.GALLERY_RESULT_PICK_PHOTO) {
                String path = intent.getExtras().getString(Constants.KEY_GALLERY_PATH);
                updateUserProfile(path);
                setUserProfilePicture(path);
            } else if (resultCode == GalleryActivity.GALLERY_RESULT_SPEAK_ITEM) {
                String path = intent.getExtras().getString(Constants.KEY_GALLERY_PATH);
                String date = intent.getExtras().getString(Constants.KEY_GALLERY_DATETIME);
                float lat = intent.getExtras().getFloat(Constants.KEY_GALLERY_LATITUDE);
                float lng = intent.getExtras().getFloat(Constants.KEY_GALLERY_LONGITUDE);
                onAddSpeakItem(path, date, lat, lng);
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
        Intent intent = new Intent(this, StoryboardSelecActivity.class);
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


    public void onAddSpeakItem(String path, String date, float lat, float lng) {
        long COLLECTION_ID = -1;
        long SPEAK_ITEM_ID = new Random().nextInt(Integer.MAX_VALUE);
        Collection collection = new Collection();
        collection.setThumbPath(path);
        collection.setDescription("None");
        collection.setId(COLLECTION_ID);
        if (!mCollectionModel.isCollectionExisted(collection.getId())) {
            mCollectionModel.addNewCollection(collection);
        }

        SpeakItem speakItem = new SpeakItem();
        speakItem.setId(SPEAK_ITEM_ID);
        speakItem.setPhotoPath(path);
        speakItem.setLatitude(lat);
        speakItem.setLongitude(lng);
        speakItem.setDateTime(date);
        speakItem.setCollectionId(collection.getId());
        mSpeakItemModel.addNewSpeakItem(speakItem);

        Intent intent = new Intent(this, SpeakItemDetailActivity.class);
        intent.putExtra(Constants.KEY_SPEAK_ITEM_ID, speakItem.getId());
        intent.putExtra(Constants.KEY_COLLECTION_ID, speakItem.getCollectionId());
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
                .placeholder(R.drawable.ic_profile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mProfilePicture);
    }

    private void setUserName(String name) {
        mUserName.setText(name);
    }

    private void logoutUser() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            LoginManager.getInstance().logOut();
        }
        SaveData.getInstance(this).setLoggedIn(false);
        Intent intent = new Intent(CollectionsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
