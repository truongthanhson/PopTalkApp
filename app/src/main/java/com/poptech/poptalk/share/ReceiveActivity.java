package com.poptech.poptalk.share;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.poptech.poptalk.Constants;
import com.poptech.poptalk.R;
import com.poptech.poptalk.utils.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by sontt on 18/05/2017.
 */

public class ReceiveActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener, WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener{

    public static final String TAG = "ReceiveActivity";
    private Toolbar mToolbar;
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;

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
            new FileServerAsyncTask(this)
                    .execute();
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

    public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;

        /**
         * @param context
         */
        public FileServerAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(context, "start receiving file", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(ShareActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(ShareActivity.TAG, "Server: connection done");
                final File f = new File(Environment.getExternalStorageDirectory() + Constants.PATH_APP + "/" + Constants.PATH_TEMP + "/" + System.currentTimeMillis() + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(ShareActivity.TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                IOUtils.copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e(ShareActivity.TAG, e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(context,"receive file successfully", Toast.LENGTH_SHORT).show();
            }
            disconnect();
        }

    }
}
