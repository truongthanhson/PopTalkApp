package com.poptech.poptalk.share;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.PopTalkApplication;
import com.poptech.poptalk.R;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.ShareItem;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.bean.StoryBoard;
import com.poptech.poptalk.provider.CollectionsModel;
import com.poptech.poptalk.provider.PopTalkDatabase;
import com.poptech.poptalk.provider.SpeakItemModel;
import com.poptech.poptalk.provider.StoryBoardModel;
import com.poptech.poptalk.speakitem.SpeakItemDetailActivity;
import com.poptech.poptalk.storyboard.StoryBoardListFragment;
import com.poptech.poptalk.storyboard.StoryboardActivity;
import com.poptech.poptalk.utils.IOUtils;
import com.poptech.poptalk.utils.StringUtils;
import com.poptech.poptalk.utils.Utils;
import com.poptech.poptalk.utils.ZipManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ReceiveActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener, WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {
    public static final String TAG = "ReceiveActivity";
    private Toolbar mToolbar;
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private static final Executor SERVER_EXECUTOR = Executors.newSingleThreadExecutor();

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private SpeakItemModel mSpeakItemModel;
    private CollectionsModel mCollectionModel;
    private StoryBoardModel mStoryBoardModel;
    private RecyclerView mSpeakItemsView;
    private SpeakItemsAdapter mSpeakItemsAdapter;
    private StoryBoardListAdapter mStoryBoardsAdapter;
    private List<SpeakItem> mSpeakItems;
    private List<StoryBoard> mStoryBoard;
    private FileServerAsyncTask mFileServerTask;
    CompositeDisposable mDisposable = new CompositeDisposable();

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
        mStoryBoardModel = new StoryBoardModel(database);

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
        mStoryBoard = new ArrayList<>();
        mSpeakItemsAdapter = new SpeakItemsAdapter(mSpeakItems, this);
        mStoryBoardsAdapter = new StoryBoardListAdapter(mStoryBoard, this);

        //action open file from email
        Intent intent = getIntent();
        String action = intent.getAction();

        if (action != null && action.compareTo(Intent.ACTION_VIEW) == 0) {
            Dexter.withActivity(this)
                    .withPermissions(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ).withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport report) {
                    if (report.areAllPermissionsGranted()) {
                        try {
                            ContentResolver contentResolver = getContentResolver();
                            Uri uri = intent.getData();
                            InputStream inputStream = contentResolver.openInputStream(uri);
                            String filePath = Environment.getExternalStorageDirectory() + Constants.PATH_APP + "/" + Constants.PATH_SHARE + "/" + Constants.PATH_RECEIVE + "/" + System.currentTimeMillis() + ".ptf";
                            File file = new File(filePath);
                            try {
                                if (!file.getParentFile().exists()) {
                                    Utils.forceMkdir(file.getParentFile());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            file.createNewFile();
                            IOUtils.copyFile(inputStream, new FileOutputStream(file));
                            getReceivedSpeakItem(filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                }
            }).check();
        }
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
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(ReceiveActivity.this, "Discovery failed: " + reasonCode + ".\nPlease check wifi connection!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        if (mFileServerTask != null) {
            mFileServerTask.cancel(true);
        }
        disconnect();
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onChannelDisconnected() {
        manager.initialize(this, getMainLooper(), this);
        deletePersistentGroups();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (info.groupFormed && info.isGroupOwner) {
            Toast.makeText(this,
                    "Ready to receive speak item!",
                    Toast.LENGTH_SHORT).show();
            startReceiveFileServer();
        }
    }

    private void startReceiveFileServer() {
        mFileServerTask = new FileServerAsyncTask(this);
        mFileServerTask.setListener(new FileServerAsyncTask.FileServerTaskListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(String file) {
                Toast.makeText(ReceiveActivity.this,
                        "Receive speak item successfully!",
                        Toast.LENGTH_SHORT).show();
                getReceivedSpeakItem(file);
                startReceiveFileServer();
            }
        });
        mFileServerTask.executeOnExecutor(SERVER_EXECUTOR);
    }

    private void getReceivedSpeakItem(String file) {
        Disposable subscription = Observable.create(new ObservableOnSubscribe<ShareItem>() {
            @Override
            public void subscribe(ObservableEmitter<ShareItem> e) throws Exception {
                e.onNext(unZipSpeakItem(file));
                e.onComplete();
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<ShareItem>() {
                    @Override
                    public void accept(ShareItem shareItem) throws Exception {
                        if (shareItem != null) {
                            // Update Collection
                            List<SpeakItem> speakItems = new ArrayList<>();
                            if (shareItem.getShareType() == Constants.ShareType.SPEAK_ITEM) {
                                // Add Collections
                                speakItems.add(shareItem.getSpeakItem());
                                // Set Adapter
                                mSpeakItems.add(shareItem.getSpeakItem());
                                mSpeakItemsView.setAdapter(mSpeakItemsAdapter);
                                mSpeakItemsAdapter.notifyDataSetChanged();
                            } else if (shareItem.getShareType() == Constants.ShareType.STORY_BOARD) {
                                speakItems.addAll(shareItem.getStoryboard().getSpeakItems());
                                mStoryBoard.add(shareItem.getStoryboard());
                                mSpeakItemsView.setAdapter(mStoryBoardsAdapter);
                                mStoryBoardsAdapter.notifyDataSetChanged();
                                mStoryBoardModel.addNewStoryBoard(shareItem.getStoryboard());
                            }

                            // Update Collection
                            for (SpeakItem speakItem : speakItems) {
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
                        }
                    }
                });
        mDisposable.add(subscription);
    }

    private ShareItem unZipSpeakItem(String zipFile) {
        ShareItem shareItem = new ShareItem();
        if (StringUtils.isNullOrEmpty(zipFile) || !new File(zipFile).exists()) {
            return null;
        }
        String speakItemDir = Environment.getExternalStorageDirectory() +
                Constants.PATH_APP + "/" +
                Constants.PATH_SHARE + "/" +
                Constants.PATH_RECEIVE + "/" +
                System.currentTimeMillis() + "/";
        File iSpeakItemDir = new File(speakItemDir);
        try {
            if (!iSpeakItemDir.exists()) {
                Utils.forceMkdir(iSpeakItemDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        ZipManager zipManager = new ZipManager();
        zipManager.unzip(zipFile, speakItemDir);

        File[] jsonFiles = getFileWithExtension(new String[]{".json", ".JSON"}, speakItemDir);
        if (jsonFiles != null) {
            for (File json : jsonFiles) {
                if (json != null && json.exists()) {
                    Gson gson = new Gson();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(json));
                        ShareItem jsonShareItem = gson.fromJson(br, ShareItem.class);
                        if (jsonShareItem != null) {
                            shareItem = jsonShareItem;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (shareItem.getShareType() == Constants.ShareType.SPEAK_ITEM) {
            String photoPath = shareItem.getSpeakItem().getPhotoPath();
            if (!StringUtils.isNullOrEmpty(photoPath)) {
                int lastIndex = photoPath.lastIndexOf("/");
                if (lastIndex >= 0) {
                    shareItem.getSpeakItem().setPhotoPath(speakItemDir + "/" + photoPath.substring(lastIndex + 1));
                }
            }
            String audioPath = shareItem.getSpeakItem().getAudioPath();
            if (!StringUtils.isNullOrEmpty(audioPath)) {
                int lastIndex = audioPath.lastIndexOf("/");
                if (lastIndex >= 0) {
                    shareItem.getSpeakItem().setAudioPath(speakItemDir + "/" + audioPath.substring(lastIndex + 1));
                }
            }
            shareItem.getSpeakItem().setId(new Random().nextInt(Integer.MAX_VALUE));
            shareItem.getSpeakItem().setCollectionId(-1);
        } else if (shareItem.getShareType() == Constants.ShareType.STORY_BOARD) {
            for (int i = 0; i < shareItem.getStoryboard().getSpeakItems().size(); i++) {
                String photoPath = shareItem.getStoryboard().getSpeakItems().get(i).getPhotoPath();
                if (!StringUtils.isNullOrEmpty(photoPath)) {
                    int lastIndex = photoPath.lastIndexOf("/");
                    if (lastIndex >= 0) {
                        shareItem.getStoryboard().getSpeakItems().get(i).setPhotoPath(speakItemDir + "/" + photoPath.substring(lastIndex + 1));
                    }
                }
                String audioPath = shareItem.getStoryboard().getSpeakItems().get(i).getAudioPath();
                if (!StringUtils.isNullOrEmpty(audioPath)) {
                    int lastIndex = audioPath.lastIndexOf("/");
                    if (lastIndex >= 0) {
                        shareItem.getStoryboard().getSpeakItems().get(i).setAudioPath(speakItemDir + "/" + audioPath.substring(lastIndex + 1));
                    }
                }
                shareItem.getStoryboard().getSpeakItems().get(i).setId(new Random().nextInt(Integer.MAX_VALUE));
                shareItem.getStoryboard().getSpeakItems().get(i).setCollectionId(-1);
                shareItem.getStoryboard().setId(new Random().nextInt(Integer.MAX_VALUE));
            }
        }
        return shareItem;
    }

    private File[] getFileWithExtension(String[] extensions, String dir) {
        File[] files = new File(dir).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                boolean ret = false;
                for (int i = 0; i < extensions.length; i++) {
                    ret = (name.endsWith(extensions[i]));
                    if (ret)
                        break;
                }
                return ret;
            }
        });
        return files;
    }

    public void disconnect() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
            }

            @Override
            public void onSuccess() {
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
            holder.mDescriptionTv.setText(mSpeakItems.get(position).getDescription1());
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


    private class StoryBoardListAdapter extends RecyclerView.Adapter<StoryBoardItemViewHolder> {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        private List<StoryBoard> mStoryBoards;
        private Context mContext;
        Calendar calendar = Calendar.getInstance();

        public StoryBoardListAdapter(List<StoryBoard> storyBoards, Context context) {
            this.mStoryBoards = storyBoards;
            this.mContext = context;
        }

        @Override
        public StoryBoardItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_story_board_list_item_layout, parent, false);
            return new StoryBoardItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(final StoryBoardItemViewHolder holder, final int position) {
            Glide.with(mContext)
                    .load(mStoryBoards.get(position).getSpeakItems().get(0).getPhotoPath())
                    .centerCrop()
                    .thumbnail(0.5f)
                    .placeholder(R.color.colorAccent)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(holder.mThumbnailIv);

            calendar.setTimeInMillis(mStoryBoards.get(position).getCreatedTime());
            holder.mCreatedTimeTv.setText("Built " + dateFormat.format(calendar.getTime()));

            holder.mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, StoryboardActivity.class);
                    intent.putExtra("storyboard", mStoryBoard.get(position));
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mStoryBoards.size();
        }


    }

    public class StoryBoardItemViewHolder extends RecyclerView.ViewHolder {

        public View mRootView;
        public ImageView mThumbnailIv;
        public TextView mCreatedTimeTv;

        public StoryBoardItemViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
            mThumbnailIv = (ImageView) mRootView.findViewById(R.id.iv_thumb_id);
            mCreatedTimeTv = (TextView) mRootView.findViewById(R.id.tv_created_time);
        }
    }
}
