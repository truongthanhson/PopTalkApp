package com.poptech.poptalk.gallery;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.provider.CollectionsModel;
import com.poptech.poptalk.provider.PopTalkDatabase;
import com.poptech.poptalk.provider.SpeakItemModel;
import com.poptech.poptalk.speakitem.SpeakItemDetailActivity;
import com.poptech.poptalk.utils.ActivityUtils;
import com.poptech.poptalk.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.Random;

/**
 * Created by sontt on 29/04/2017.
 */

public class GalleryActivity extends AppCompatActivity {
    public enum GalleryType {
        PICK_PROFILE_PICTURE,
        PICK_ADDED_SPEAK_ITEM,
        PICK_EDITED_SPEAK_ITEM
    }

    private static final String TAG = "GalleryActivity";

    public static final int SELECT_PHOTO_REQUEST_CODE = 1111;
    private Toolbar mToolbar;
    private String mPhotoString;
    SpeakItemModel mSpeakItemModel;
    private CollectionsModel mCollectionModel;
    private GalleryType mGalleryType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGalleryType = (GalleryType) getIntent().getSerializableExtra(Constants.KEY_PHOTO_GALLERY);

        setContentView(R.layout.activity_photo);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.photo_gallery));

        PhotoLGalleryFragment photoLGalleryFragment =
                (PhotoLGalleryFragment) getSupportFragmentManager().findFragmentById(R.id.container_body);
        if (photoLGalleryFragment == null) {
            // Create the fragment
            photoLGalleryFragment = PhotoLGalleryFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), photoLGalleryFragment, R.id.container_body);
        }
        PopTalkDatabase database = new PopTalkDatabase(PopTalkApplication.applicationContext);
        mSpeakItemModel = new SpeakItemModel(database);
        mCollectionModel = new CollectionsModel(database);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackCount == 0) {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.action_camera) {
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.CAMERA).withListener(new BasePermissionListener() {
                @Override
                public void onPermissionGranted(PermissionGrantedResponse response) {
                    super.onPermissionGranted(response);
                    runCamera();
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem cameraItem = menu.findItem(R.id.action_camera);
        if (cameraItem != null) {
            cameraItem.setVisible(true);
        }
        MenuItem plusItem = menu.findItem(R.id.action_plus);
        if (plusItem != null) {
            plusItem.setVisible(false);
        }

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchItem.setVisible(false);
        }

        MenuItem moreItem = menu.findItem(R.id.action_more);
        if (moreItem != null) {
            moreItem.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_AVATAR_CAPTURE && resultCode == Activity.RESULT_OK) {
            startActivityCrop(mPhotoString);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                handleActivityResult(result.getUri().getPath());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Intent errorIntent = new Intent();
                errorIntent.putExtra("error", error.getMessage());
                setResult(Activity.RESULT_CANCELED, errorIntent);
            }
            finish();
        }
    }

    public void startActivityCrop(String imagePath) {
        CropImage.activity(Uri.fromFile(new File(imagePath)))
                .start(this);
    }

    private void handleActivityResult(String photoPath) {
        switch (mGalleryType) {
            case PICK_PROFILE_PICTURE:
                onProfilePicture(photoPath);
                break;
            case PICK_ADDED_SPEAK_ITEM:
                onSpeakItemDetailScreen(photoPath);
                break;
        }
    }

    public void onProfilePicture(String photoPath) {
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY_PHOTO_GALLERY_RESULT, photoPath);
        setResult(Activity.RESULT_OK, intent);
    }

    public void onSpeakItemDetailScreen(String photoPath) {
        long COLLECTION_ID = -1;
        long SPEAK_ITEM_ID = new Random().nextInt(Integer.MAX_VALUE);
        Collection collection = new Collection();
        collection.setThumbPath(photoPath);
        collection.setDescription("Unknown");
        collection.setId(COLLECTION_ID);
        if (!mCollectionModel.isCollectionExisted(collection.getId())) {
            mCollectionModel.addNewCollection(collection);
        }

        SpeakItem speakItem = new SpeakItem();
        speakItem.setId(SPEAK_ITEM_ID);
        speakItem.setPhotoPath(photoPath);
        speakItem.setCollectionId(collection.getId());
        mSpeakItemModel.addNewSpeakItem(speakItem);

        Intent intent = new Intent(this, SpeakItemDetailActivity.class);
        intent.putExtra(Constants.KEY_SPEAK_ITEM_ID, speakItem.getId());
        intent.putExtra(Constants.KEY_COLLECTION_ID, speakItem.getCollectionId());
        startActivity(intent);
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
}
