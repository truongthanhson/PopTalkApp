package com.poptech.poptalk.collections;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
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
import com.poptech.poptalk.location.LocationListener;
import com.poptech.poptalk.location.LocationTracker;
import com.poptech.poptalk.login.LoginActivity;
import com.poptech.poptalk.login.LoginModel;
import com.poptech.poptalk.provider.CollectionsModel;
import com.poptech.poptalk.provider.PopTalkDatabase;
import com.poptech.poptalk.provider.SpeakItemModel;
import com.poptech.poptalk.share.ReceiveActivity;
import com.poptech.poptalk.speakitem.SpeakItemDetailActivity;
import com.poptech.poptalk.storyboard.StoryBoardListActivity;
import com.poptech.poptalk.utils.ActivityUtils;
import com.poptech.poptalk.utils.SaveData;
import com.poptech.poptalk.utils.StringUtils;
import com.poptech.poptalk.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sontt on 26/04/2017.
 */

public class CollectionsActivity extends AppCompatActivity implements View.OnClickListener, AppMenuOpen, CollectionsFragment.CollectionsFragmentCallback, SpeakItemsFragment.SpeakItemsFragmentCallback, LocationListener {
    private DrawerLayout mDrawerLayout;

    private RecyclerView mDrawerMenu;

    private FloatingActionButton mFloatingButton;

    private DrawerMenuAdapter mDrawerMenuAdapter;

    private CircleImageView mProfilePicture;

    private TextView mUserName;

    private ImageButton mLogoutButton;

    private LocationTracker mTracker;
    private Location mLocation = new Location("");

    private String mPhotoString;
    private String mDateTime = "";

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
        if (mTracker != null) {
            mTracker.stopLocation();
        }
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
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add_speak_item:
                CharSequence sources[] = new CharSequence[]{"from Camera", "from Gallery"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Create new speak item");
                builder.setItems(sources, (dialog, which) -> {
                    if (which == 0) {
                        Dexter.withActivity(this)
                                .withPermissions(
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                ).withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    mTracker = new LocationTracker(CollectionsActivity.this, CollectionsActivity.this);
                                    if (mTracker.canGetLocation()) {
                                        runCamera();
                                    } else {
                                        // Run camera on activity result
                                        showSettingsAlert();
                                    }
                                } else {
                                    for (PermissionGrantedResponse permission : report.getGrantedPermissionResponses()) {
                                        if (permission.getPermissionName().equals(Manifest.permission.CAMERA)) {
                                            runCamera();
                                            break;
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            }
                        }).check();
                    } else if (which == 1) {
                        navigateToAddSpeakItem();
                    }
                });
                builder.show();

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

    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
        alertDialog.setTitle("Location Setting");
        alertDialog.setMessage("Location is not enabled. Do you want to enable location?");
        alertDialog.setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, Constants.REQUEST_LOCATION_ENABLE);
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                runCamera();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void runCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Uri mImageCaptureUri;
            mPhotoString = Environment.getExternalStorageDirectory() + Constants.PATH_APP + "/" + Constants.PATH_PHOTO + "/" + System.currentTimeMillis() + ".jpg";
            File iFilePath = new File(mPhotoString);
            try {
                if (!iFilePath.getParentFile().exists()) {
                    Utils.forceMkdir(iFilePath.getParentFile());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mImageCaptureUri = Uri.fromFile(iFilePath);
            takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            takePictureIntent.putExtra("return-data", true);
            startActivityForResult(takePictureIntent, Constants.REQUEST_AVATAR_CAPTURE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
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
                    intent.putExtra(Constants.KEY_PHOTO_GALLERY, Constants.GALLERY_RESULT_SPEAK_ITEM);
                    startActivityForResult(intent, Constants.REQUEST_GALLERY_CAPTURE);
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
                    intent.putExtra(Constants.KEY_PHOTO_GALLERY, Constants.GALLERY_RESULT_PICK_PHOTO);
                    startActivityForResult(intent, Constants.REQUEST_GALLERY_CAPTURE);
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
        if (requestCode == Constants.REQUEST_GALLERY_CAPTURE) {
            if (resultCode == Constants.GALLERY_RESULT_PICK_PHOTO) {
                String path = intent.getExtras().getString(Constants.KEY_GALLERY_PATH);
                updateUserProfile(path);
                setUserProfilePicture(path);
            } else if (resultCode == Constants.GALLERY_RESULT_SPEAK_ITEM) {
                String path = intent.getExtras().getString(Constants.KEY_GALLERY_PATH);
                String date = intent.getExtras().getString(Constants.KEY_GALLERY_DATETIME);
                Location location = intent.getExtras().getParcelable(Constants.KEY_GALLERY_LOCATION);
                onAddSpeakItem(path, date, location);
            }
        } else if (requestCode == Constants.REQUEST_AVATAR_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                if (mTracker != null) {
                    mLocation = mTracker.getLocation();
                    mTracker.stopLocation();
                }
                mDateTime = getCurrentDate();
                startActivityCrop(mPhotoString);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(intent);
            if (resultCode == RESULT_OK) {
                Intent cropIntent = new Intent();
//                cropIntent.putExtra(Constants.KEY_GALLERY_PATH, result.getUri().getPath());
//                cropIntent.putExtra(Constants.KEY_GALLERY_LOCATION, mLocation);
//                cropIntent.putExtra(Constants.KEY_GALLERY_DATETIME, mDateTime);
                onAddSpeakItem(result.getUri().getPath(), mDateTime, mLocation);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Intent errorIntent = new Intent();
                errorIntent.putExtra("error", error.getMessage());
                setResult(Activity.RESULT_CANCELED, errorIntent);
            }
        } else if (requestCode == Constants.REQUEST_LOCATION_ENABLE) {
            runCamera();
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
        bundle.putSerializable(Constants.KEY_SPEAK_ITEM_SORT_TYPE, SpeakItemsFragment.GroupSpeakItemSortType.LOCATION);
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
        Intent intent = new Intent(this, StoryBoardListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onNavigateStoryboardFrequency() {
        Intent intent = new Intent(this, StoryBoardListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onNavigateToReceiveScreen() {
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
                    Intent intent = new Intent(CollectionsActivity.this, ReceiveActivity.class);
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

    @Override
    public void onClickShareItem(Collection collection) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        if (mTracker != null) {
            mTracker.stopLocation();
        }
    }

    public void openSpeakItemDetailScreen(long speakItemId, long collectionId) {
        Intent intent = new Intent(this, SpeakItemDetailActivity.class);
        intent.putExtra(Constants.KEY_SPEAK_ITEM_ID, speakItemId);
        intent.putExtra(Constants.KEY_COLLECTION_ID, collectionId);
        startActivity(intent);
    }


    public void onAddSpeakItem(String path, String date, Location location) {
        long COLLECTION_ID = -1;
        long SPEAK_ITEM_ID = new Random().nextInt(Integer.MAX_VALUE);

        // Update Collection
        if (mCollectionModel.isCollectionExisted(COLLECTION_ID)) {
            Collection collection = mCollectionModel.getCollection(COLLECTION_ID);
            collection.setNumSpeakItem(collection.getNumSpeakItem() + 1);
            collection.setThumbPath(path);
            mCollectionModel.updateCollection(collection);
        } else {
            Collection collection = new Collection();
            collection.setId(COLLECTION_ID);
            collection.setNumSpeakItem(1);
            collection.setThumbPath(path);
            collection.setAddedTime(System.currentTimeMillis());
            mCollectionModel.addNewCollection(collection);
        }

        // Update Speak Item
        SpeakItem speakItem = new SpeakItem();
        speakItem.setId(SPEAK_ITEM_ID);
        speakItem.setPhotoPath(path);
        if (location != null) {
            speakItem.setLatitude(location.getLatitude());
            speakItem.setLongitude(location.getLongitude());
        }
        speakItem.setDateTime(date);
        speakItem.setAddedTime(System.currentTimeMillis());
        speakItem.setCollectionId(COLLECTION_ID);
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


    private void getExifPhoto(String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            mLocation = new Location("");

            // Parse location
            String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String lat_ref = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String lng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String lng_ref = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            if ((!StringUtils.isNullOrEmpty(lat) && !StringUtils.isNullOrEmpty(lat_ref)) &&
                    (!StringUtils.isNullOrEmpty(lng) && !StringUtils.isNullOrEmpty(lng_ref))) {
                if (lat_ref.equals("N")) {
                    mLocation.setLatitude(convertToDegree(lat));
                } else {
                    mLocation.setLatitude(0 - convertToDegree(lat));
                }
                if (lng_ref.equals("E")) {
                    mLocation.setLongitude(convertToDegree(lng));
                } else {
                    mLocation.setLongitude(0 - convertToDegree(lng));
                }
            }

            // Parse date
            mDateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            SimpleDateFormat outFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a");
            mDateTime = outFormat.format(inFormat.parse(mDateTime));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Double convertToDegree(String stringDMS) {
        Double result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0 / S1;

        result = new Double(FloatD + (FloatM / 60) + (FloatS / 3600));

        return result;
    }

    private String getCurrentDate() {
        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a");
        String dateString = sdf.format(date);
        return dateString;
    }

    public void startActivityCrop(String path) {
        CropImage.activity(Uri.fromFile(new File(path)))
                .start(this);
    }
}
