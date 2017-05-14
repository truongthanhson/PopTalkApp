package com.poptech.poptalk.gallery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.R;
import com.poptech.poptalk.location.LocationListener;
import com.poptech.poptalk.location.LocationTracker;
import com.poptech.poptalk.utils.ActivityUtils;
import com.poptech.poptalk.utils.StringUtils;
import com.poptech.poptalk.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by sontt on 29/04/2017.
 */

public class GalleryActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "GalleryActivity";

    private Toolbar mToolbar;

    private int mResultCode;
    private LocationTracker mTracker;
    private String mPhotoString = "";
    private String mDateTime = "";
    private Location mLocation = new Location("");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResultCode = getIntent().getIntExtra(Constants.KEY_PHOTO_GALLERY, Constants.GALLERY_RESULT_FAILED);

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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mTracker != null) {
            mTracker.stopLocation();
        }
        super.onDestroy();
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
                    .withPermissions(
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ).withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {
                    if (report.areAllPermissionsGranted()) {
                        mTracker = new LocationTracker(GalleryActivity.this, GalleryActivity.this);
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
        if (requestCode == Constants.REQUEST_AVATAR_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                if (mTracker != null) {
                    mLocation = mTracker.getLocation();
                    mTracker.stopLocation();
                }
                mDateTime = getCurrentDate();
                startActivityCrop(mPhotoString);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent();
                intent.putExtra(Constants.KEY_GALLERY_PATH, result.getUri().getPath());
                intent.putExtra(Constants.KEY_GALLERY_LOCATION, mLocation);
                intent.putExtra(Constants.KEY_GALLERY_DATETIME, mDateTime);
                setResult(mResultCode, intent);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Intent errorIntent = new Intent();
                errorIntent.putExtra("error", error.getMessage());
                setResult(Activity.RESULT_CANCELED, errorIntent);
            }
            finish();
        } else if (requestCode == Constants.REQUEST_LOCATION_ENABLE) {
            runCamera();
        }
    }

    public void parsePhoAttribute(final String path) {
        startActivityCrop(path);
        Completable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getExifPhoto(path);
                return null;
            }
        }).subscribeOn(Schedulers.computation())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "on Parse complete");
                    }

                    @Override
                    public void onError(Throwable error) {
                    }
                });

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

    public void startActivityCrop(String path) {
        CropImage.activity(Uri.fromFile(new File(path)))
                .start(this);
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


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
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


    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        if (mTracker != null) {
            mTracker.stopLocation();
        }
    }

    private String getCurrentDate() {
        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a");
        String dateString = sdf.format(date);
        return dateString;
    }
}
