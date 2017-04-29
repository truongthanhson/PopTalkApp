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
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.drawer.data.BaseItem;
import com.poptech.poptalk.drawer.data.CustomDataProvider;
import com.poptech.poptalk.drawer.multilevellistview.ItemInfo;
import com.poptech.poptalk.drawer.multilevellistview.MultiLevelListAdapter;
import com.poptech.poptalk.drawer.multilevellistview.MultiLevelListView;
import com.poptech.poptalk.drawer.multilevellistview.OnItemClickListener;
import com.poptech.poptalk.drawer.views.LevelBeamView;
import com.poptech.poptalk.gallery.GalleryActivity;
import com.poptech.poptalk.gallery.PhotoLGalleryFragment;
import com.poptech.poptalk.utils.ActivityUtils;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 26/04/2017.
 */

public class CollectionsActivity extends AppCompatActivity implements View.OnClickListener {
    private DrawerLayout mDrawerLayout;

    private MultiLevelListView multiLevelListView;

    private FloatingActionButton mFloatingButton;

    @Inject
    CollectionsPresenter mPresenter;
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

        //setup floafing button
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
    }

    private void setupNavigationBar() {
        multiLevelListView = (MultiLevelListView) findViewById(R.id.multiLevelMenu);

        // custom ListAdapter
        ListAdapter listAdapter = new ListAdapter();

        multiLevelListView.setAdapter(listAdapter);
        multiLevelListView.setOnItemClickListener(mOnItemClickListener);

        listAdapter.setDataItems(CustomDataProvider.getInitialItems());
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        private void showItemDescription(Object object, ItemInfo itemInfo) {
        }

        @Override
        public void onItemClicked(MultiLevelListView parent, View view, Object item, ItemInfo itemInfo) {
            showItemDescription(item, itemInfo);
        }

        @Override
        public void onGroupItemClicked(MultiLevelListView parent, View view, Object item, ItemInfo itemInfo) {
            showItemDescription(item, itemInfo);
        }
    };

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

    private class ListAdapter extends MultiLevelListAdapter {

        private class ViewHolder {
            TextView nameView;
            TextView infoView;
            ImageView arrowView;
            LevelBeamView levelBeamView;
        }

        @Override
        public List<?> getSubObjects(Object object) {
            return CustomDataProvider.getSubItems((BaseItem) object);
        }

        @Override
        public boolean isExpandable(Object object) {
            return CustomDataProvider.isExpandable((BaseItem) object);
        }

        @Override
        public View getViewForObject(Object object, View convertView, ItemInfo itemInfo) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(CollectionsActivity.this).inflate(R.layout.data_item, null);
                //viewHolder.infoView = (TextView) convertView.findViewById(R.id.dataItemInfo);
                viewHolder.nameView = (TextView) convertView.findViewById(R.id.dataItemName);
                viewHolder.arrowView = (ImageView) convertView.findViewById(R.id.dataItemArrow);
                viewHolder.levelBeamView = (LevelBeamView) convertView.findViewById(R.id.dataItemLevelBeam);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.nameView.setText(((BaseItem) object).getName());
            //viewHolder.infoView.setText(getItemInfoDsc(itemInfo));

            if (itemInfo.isExpandable()) {
                viewHolder.arrowView.setVisibility(View.VISIBLE);
                viewHolder.arrowView.setImageResource(itemInfo.isExpanded() ?
                        R.drawable.ic_expand_less : R.drawable.ic_expand_more);
            } else {
                viewHolder.arrowView.setVisibility(View.GONE);
            }

            viewHolder.levelBeamView.setLevel(itemInfo.getLevel());

            return convertView;
        }
    }

}
