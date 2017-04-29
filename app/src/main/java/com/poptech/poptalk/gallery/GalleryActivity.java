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
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.R;
import com.poptech.poptalk.collections.CollectionsFragment;
import com.poptech.poptalk.utils.ActivityUtils;
import com.poptech.poptalk.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;

/**
 * Created by sontt on 29/04/2017.
 */

public class GalleryActivity extends AppCompatActivity {
    public static final int SELECT_PHOTO_REQUEST_CODE = 1111;
    private static final String TAG = "GalleryActivity";
    private Toolbar mToolbar;
    private String mFilePathString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            String pathPhoto = Environment.getExternalStorageDirectory() + Constants.PATH_APP + Constants.PATH_PHOTO;
            String pathSound = Environment.getExternalStorageDirectory() + Constants.PATH_APP + Constants.PATH_SOUND;
            File filePhoto = new File(pathPhoto);
            File fileSound = new File(pathSound);
            if (!filePhoto.exists()) {
                Utils.forceMkdir(filePhoto);
            }
            if (!fileSound.exists()) {
                Utils.forceMkdir(fileSound);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        }else if (item.getItemId() == R.id.action_camera) {
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
        if(cameraItem != null) {
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_AVATAR_CAPTURE && resultCode == Activity.RESULT_OK) {
            startActivityCrop(mFilePathString);
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("croppedPath", resultUri.getPath());
                setResult(Activity.RESULT_OK,resultIntent);
                Toast.makeText(this,resultUri.getPath()+ "", Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Intent errorIntent = new Intent();
                errorIntent.putExtra("error", error.getMessage());
                setResult(Activity.RESULT_CANCELED,errorIntent);
            }
            finish();
        }
    }

    public void startActivityCrop(String imagePath) {
        CropImage.activity(Uri.fromFile(new File(imagePath)))
                .start(this);
    }

    private void runCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Uri mImageCaptureUri;
            mFilePathString = Environment.getExternalStorageDirectory() + Constants.PATH_APP + "/" + Constants.PATH_PHOTO + "/" + System.currentTimeMillis() + ".jpg";
            File iFilePath = new File(mFilePathString);
            mImageCaptureUri = Uri.fromFile(iFilePath);
            takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            takePictureIntent.putExtra("return-data", true);
            startActivityForResult(takePictureIntent, Constants.REQUEST_AVATAR_CAPTURE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
