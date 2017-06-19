package com.poptech.poptalk.share;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.utils.Utils;
import com.poptech.poptalk.utils.ZipManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by sontt on 17/05/2017.
 */

public class ShareActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener, DeviceListFragment.DeviceActionListener, WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener {

    public static final String TAG = "ShareActivity";
    private Toolbar mToolbar;
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private Dialog mWifiDialog = null;
    private SpeakItem mSpeakItem;
    private WifiP2pInfo mLastInfo;

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
        if (!isWifiP2pEnabled) {
            showWifiSettings();
        } else {
            if (mWifiDialog != null) {
                mWifiDialog.dismiss();
            }
            discoveryPeers();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Key = getIntent().getIntExtra(Constants.KEY_PHOTO_GALLERY, 1);
        mSpeakItem = getIntent().getParcelableExtra(Constants.KEY_SPEAK_ITEM);
        setContentView(R.layout.activity_share_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.sender_title));

        // add necessary intent values to be matched.

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        deletePersistentGroups();
    }

    private void deletePersistentGroups() {
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(manager, channel, netid, null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * register the BroadcastReceiver with the intent values to be matched
     */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
//                Toast.makeText(ShareActivity.this, "Discovery initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(ShareActivity.this, "Discovery failed: " + reasonCode + ".\n Please check wifi connection!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        unregisterReceiver(receiver);
//        disconnect();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_discovery, menu);
        MenuItem discoveryItem = menu.findItem(R.id.action_discovery);
        if (discoveryItem != null) {
            discoveryItem.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_discovery:
                if (!isWifiP2pEnabled) {
                    showWifiSettings();
                } else {
                    discoveryPeers();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showWifiSettings() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Wifi Setting");
        alertDialog.setMessage("Wifi is not enabled. Do you want to enable wifi?");
        alertDialog.setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        mWifiDialog = alertDialog.create();
        mWifiDialog.show();
    }

    public void discoveryPeers() {
        DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
        fragment.onInitiateDiscovery();
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
//                Toast.makeText(ShareActivity.this, "Discovery initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(ShareActivity.this, "Discovery failed: " + reasonCode + ".\nPlease check wifi connection!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        if (device.status == WifiP2pDevice.AVAILABLE) {
            builder.setTitle("Connect")
                    .setMessage("Are you sure you want to connect with this device?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = device.deviceAddress;
                            config.groupOwnerIntent = 0;
                            config.wps.setup = WpsInfo.PBC;
                            connect(config);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        } else if (device.status == WifiP2pDevice.CONNECTED) {
            builder.setTitle("Share")
                    .setMessage("Are you sure you want to share speak item with this device?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startTransferFile(mLastInfo);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(R.drawable.ic_share_white)
                    .show();
        } else {
            Toast.makeText(ShareActivity.this, "Unable to share right now", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void connect(WifiP2pConfig config) {
//        config.groupOwnerIntent = 15; // I want this device to become the owner
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                Toast.makeText(ShareActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(ShareActivity.this, "Connect failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {

        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
//                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
            }

        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
            deletePersistentGroups();
        } else {
            Toast.makeText(this, "Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(ShareActivity.this, "Aborting connection", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(ShareActivity.this, "Connect abort request failed. Reason Code: " + reasonCode, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        mLastInfo = info;
        if (info.groupFormed && !info.isGroupOwner) {
//            startTransferFile(info);
            Toast.makeText(ShareActivity.this, "Ready to send speak item!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTransferFile(WifiP2pInfo info) {
        if (info != null) {
            String speakItemZip = zipSpeakItem(mSpeakItem);
            Intent serviceIntent = new Intent(this, FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, speakItemZip);
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
            this.startService(serviceIntent);
        }
    }

    public static String zipSpeakItem(SpeakItem speakItem) {
        String speakItemDir = Environment.getExternalStorageDirectory() +
                Constants.PATH_APP + "/" +
                Constants.PATH_SHARE + "/" +
                Constants.PATH_SEND + "/" +
                speakItem.getId();
        File iSpeakItemDir = new File(speakItemDir);
        try {
            if (!iSpeakItemDir.exists()) {
                Utils.forceMkdir(iSpeakItemDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String speakItemJson = speakItemDir + "/" + speakItem.getId() + ".json";
        Gson gson = new Gson();
        try {
            String jsonString = gson.toJson(speakItem);
            FileWriter fileWriter = new FileWriter(speakItemJson);
            fileWriter.write(jsonString);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> zipFiles = new ArrayList<>();
        if (new File(speakItemJson).exists()) {
            zipFiles.add(speakItemJson);
        }
        if (new File(speakItem.getAudioPath()).exists()) {
            zipFiles.add(speakItem.getAudioPath());
        }
        if (new File(speakItem.getPhotoPath()).exists()) {
            zipFiles.add(speakItem.getPhotoPath());
        }

        String speakItemZip = speakItemDir + "/" + speakItem.getId() + ".ptf";
        ZipManager zipManager = new ZipManager();
        zipManager.zip(zipFiles.toArray(new String[zipFiles.size()]), speakItemZip);
        return speakItemZip;
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        Log.e("sontt", peers.toString());
    }

//
//    private void startReceiveFileServer() {
//        mFileServerTask = new FileServerAsyncTask(this);
//        mFileServerTask.setListener(new FileServerAsyncTask.FileServerTaskListener() {
//            @Override
//            public void onStart() {
//                Toast.makeText(PopTalkApplication.applicationContext,
//                        "Start receiving speak item",
//                        Toast.LENGTH_SHORT).show();
////                findViewById(R.id.progress_bar_id).setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onSuccess(SpeakItem speakItem) {
//                Toast.makeText(PopTalkApplication.applicationContext,
//                        "Receive speak item successfully",
//                        Toast.LENGTH_SHORT).show();
////                findViewById(R.id.progress_bar_id).setVisibility(View.GONE);
//                startReceiveFileServer();
//            }
//        });
//        mFileServerTask.execute();
//    }
}
