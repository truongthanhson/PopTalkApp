package com.poptech.poptalk.share;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.poptech.poptalk.Constants;
import com.poptech.poptalk.bean.SpeakItem;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Created by cuonghl on 5/23/2017.
 */

public class FileServerAsyncTask extends AsyncTask<Void, Void, SpeakItem> {
    public interface FileServerTaskListener {
        public void onStart();

        public void onSuccess(SpeakItem speakItem);
    }

    private FileServerTaskListener mListener;

    private static final String TAG = "FileServerAsyncTask";

    public void setListener(FileServerTaskListener mListener) {
        this.mListener = mListener;
    }

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
        mListener.onStart();
    }

    @Override
    protected SpeakItem doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(8988);
            Log.d(ShareActivity.TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();
            Log.d(ShareActivity.TAG, "Server: connection done");
            String zipFile = Environment.getExternalStorageDirectory() +
                    Constants.PATH_APP + "/" +
                    Constants.PATH_SHARE + "/" +
                    Constants.PATH_RECEIVE + "/" +
                    System.currentTimeMillis() + ".zip";
            final File f = new File(zipFile);

            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();

            Log.d(ShareActivity.TAG, "server: copying files " + f.toString());
            InputStream inputstream = client.getInputStream();
            IOUtils.copyFile(inputstream, new FileOutputStream(f));
            serverSocket.close();
            return unZipSpeakItem(f.getAbsolutePath());
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
    protected void onPostExecute(SpeakItem result) {
        mListener.onSuccess(result);
    }

    private SpeakItem unZipSpeakItem(String zipFile) {
        SpeakItem speakItem = new SpeakItem();
        if (StringUtils.isNullOrEmpty(zipFile) || !new File(zipFile).exists()) {
            return null;
        }
        long speakItemId = new Random().nextInt(Integer.MAX_VALUE);
        String speakItemDir = Environment.getExternalStorageDirectory() +
                Constants.PATH_APP + "/" +
                Constants.PATH_SHARE + "/" +
                Constants.PATH_RECEIVE + "/" +
                speakItemId + "/";
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
                        SpeakItem jsonSpeakItem = gson.fromJson(br, SpeakItem.class);
                        if(jsonSpeakItem != null) {
                            speakItem = jsonSpeakItem;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        File[] photoFiles = getFileWithExtension(new String[]{".jpg", ".jpeg", ".png"}, speakItemDir);
        if (photoFiles != null) {
            for (File photo : photoFiles) {
                if (photo != null && photo.exists()) {
                    speakItem.setPhotoPath(photo.getAbsolutePath());
                }
            }
        }

        File[] audioFiles = getFileWithExtension(new String[]{".ogg", ".mp3", ".3gp"}, speakItemDir);
        if (audioFiles != null) {
            for (File audio : audioFiles) {
                if (audio != null && audio.exists()) {
                    speakItem.setAudioPath(audio.getAbsolutePath());
                }
            }
        }

        speakItem.setId(speakItemId);
        speakItem.setCollectionId(-1);

        return speakItem;
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

}