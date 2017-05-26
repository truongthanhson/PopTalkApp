package com.poptech.poptalk.share;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.provider.CollectionsModel;
import com.poptech.poptalk.provider.PopTalkDatabase;
import com.poptech.poptalk.provider.SpeakItemModel;
import com.poptech.poptalk.speakitem.SpeakItemDetailActivity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    private RecyclerView mSpeakItemsView;
    private List<SpeakItem> mSpeakItems;
    private FileServerAsyncTask mFileServerTask;

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
        deletePersistentGroups();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        mSpeakItemsView = (RecyclerView) findViewById(R.id.speak_item_list);
        mSpeakItemsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mSpeakItems = new ArrayList<>();
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

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(ReceiveActivity.this, "Discovery initiated",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(ReceiveActivity.this, "Discovery failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mFileServerTask != null) {
            mFileServerTask.cancel(true);
        }
        disconnect();
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onChannelDisconnected() {
        manager.initialize(this, getMainLooper(), this);
        deletePersistentGroups();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        Log.e("sontt", peers.toString());
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info.groupFormed && info.isGroupOwner) {
            startReceiveFileServer();
        }
    }

    private void startReceiveFileServer() {
        mFileServerTask = new FileServerAsyncTask(this);
        mFileServerTask.setListener(new FileServerAsyncTask.FileServerTaskListener() {
            @Override
            public void onStart() {
//                Toast.makeText(PopTalkApplication.applicationContext,
//                        "Start receiving speak item",
//                        Toast.LENGTH_SHORT).show();
//                findViewById(R.id.progress_bar_id).setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(SpeakItem speakItem) {
                Toast.makeText(PopTalkApplication.applicationContext,
                        "Receive speak item successfully",
                        Toast.LENGTH_SHORT).show();
//                findViewById(R.id.progress_bar_id).setVisibility(View.GONE);
                if (speakItem != null) {
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
                    mSpeakItems.add(speakItem);
                    mSpeakItemsView.setAdapter(new SpeakItemsAdapter(mSpeakItems, ReceiveActivity.this));
                }
                startReceiveFileServer();
            }
        });
        mFileServerTask.execute();
    }

    public void disconnect() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
//                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
//                Toast.makeText(ReceiveActivity.this, "disconnect", Toast.LENGTH_SHORT).show();
            }

        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

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
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                if (manager != null) {
                    manager.requestPeers(channel, ReceiveActivity.this);
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
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
            }
        }
    }

    private class SpeakItemsAdapter extends RecyclerView.Adapter<SpeakItemViewHolder> {
        private List<SpeakItem> mSpeakItems;
        private Context mContext;

        public SpeakItemsAdapter(List<SpeakItem> speakItems, Context context) {
            this.mSpeakItems = speakItems;
            this.mContext = context;
        }

        @Override
        public SpeakItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_speak_item_layout, parent, false);
            return new SpeakItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(SpeakItemViewHolder holder, final int position) {
            holder.mDescriptionTv.setText(mSpeakItems.get(position).getDescription());
            holder.mLanguageTv.setText(mSpeakItems.get(position).getLanguage());
            Glide.with(mContext)
                    .load(mSpeakItems.get(position).getPhotoPath())
                    .centerCrop()
                    .thumbnail(0.5f)
                    .placeholder(R.color.colorAccent)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mThumbnailIv);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, SpeakItemDetailActivity.class);
                intent.putExtra(Constants.KEY_SPEAK_ITEM_ID, mSpeakItems.get(position).getId());
                intent.putExtra(Constants.KEY_COLLECTION_ID, mSpeakItems.get(position).getCollectionId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return mSpeakItems.size();
        }
    }

    private class SpeakItemViewHolder extends RecyclerView.ViewHolder {

        private View mRootView;
        private TextView mLanguageTv;
        private ImageView mThumbnailIv;
        private TextView mDescriptionTv;

        public SpeakItemViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
            mDescriptionTv = (TextView) mRootView.findViewById(R.id.tv_description_id);
            mLanguageTv = (TextView) mRootView.findViewById(R.id.tv_lang_id);
            mThumbnailIv = (ImageView) mRootView.findViewById(R.id.iv_thumb_id);
        }
    }
}
