package com.poptech.poptalk.share;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.location.LocationTask;
import com.poptech.poptalk.provider.CollectionsModel;
import com.poptech.poptalk.provider.PopTalkDatabase;
import com.poptech.poptalk.provider.SpeakItemModel;
import com.poptech.poptalk.utils.IOUtils;
import com.poptech.poptalk.utils.StringUtils;
import com.poptech.poptalk.utils.Utils;
import com.poptech.poptalk.utils.ZipManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Created by sontt on 18/05/2017.
 */

public class ReceiveActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener, WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    public static final String TAG = "ReceiveActivity";
    private Toolbar mToolbar;
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private SpeakItemModel mSpeakItemModel;
    private CollectionsModel mCollectionModel;

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.receiver_title));

        PopTalkDatabase database = new PopTalkDatabase(PopTalkApplication.applicationContext);
        mSpeakItemModel = new SpeakItemModel(database);
        mCollectionModel = new CollectionsModel(database);

        // add necessary intent values to be matched.

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);

    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(ReceiveActivity.this, "Discovery Initiated",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(ReceiveActivity.this, "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onChannelDisconnected() {
        manager.initialize(this, getMainLooper(), this);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        Log.e("sontt", peers.toString());
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info.groupFormed && info.isGroupOwner) {
            FileServerAsyncTask task = new FileServerAsyncTask(this);
            task.setListener(new FileServerAsyncTask.FileServerTaskListener() {
                @Override
                public void onStart() {
                    Toast.makeText(PopTalkApplication.applicationContext,
                            "Start Receiving Speak Item",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(SpeakItem speakItem) {
                    if (speakItem != null) {
                        Toast.makeText(PopTalkApplication.applicationContext,
                                "Receive Speak Item Successfully",
                                Toast.LENGTH_SHORT).show();
                        // Update Collection
                        if (mCollectionModel.isCollectionExisted(speakItem.getCollectionId())) {
                            Collection collection = mCollectionModel.getCollection(speakItem.getCollectionId());
                            collection.setNumSpeakItem(collection.getNumSpeakItem() + 1);
                            collection.setThumbPath(speakItem.getPhotoPath());
                            mCollectionModel.updateCollection(collection);
                        } else {
                            Collection collection = new Collection();
                            collection.setId(speakItem.getCollectionId());
                            collection.setNumSpeakItem(1);
                            collection.setThumbPath(speakItem.getPhotoPath());
                            collection.setAddedTime(System.currentTimeMillis());
                            mCollectionModel.addNewCollection(collection);
                        }

                        // Update Speak Item
                        speakItem.setAddedTime(System.currentTimeMillis());
                        mSpeakItemModel.addNewSpeakItem(speakItem);
                    }
                    disconnect();
                }
            });
            task.execute();
        }
    }

    public void disconnect() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                Toast.makeText(ReceiveActivity.this, "disconnect", Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
     */
    private class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private ReceiveActivity mActivity;

        public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                           ReceiveActivity activity) {
            super();
            this.mManager = manager;
            this.mChannel = channel;
            this.mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                } else {
                    // Wi-Fi P2P is not enabled
                }
                Log.e("sontt", "state = " + state);
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                Log.e("sontt", "WIFI_P2P_PEERS_CHANGED_ACTION");
                if (manager != null) {
                    manager.requestPeers(channel, ReceiveActivity.this);
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
                Log.e("sontt", "WIFI_P2P_CONNECTION_CHANGED_ACTION");
                if (manager == null) {
                    return;
                }
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    // we are connected with the other device, request connection
                    // info to find group owner IP
                    manager.requestConnectionInfo(channel, ReceiveActivity.this);
                } else {
                    // It's a disconnect
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
                Log.e("sontt", "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
            }
        }

    }
}
