package com.poptech.poptalk.share;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
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

public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {
    public interface FileServerTaskListener {
        public void onStart();

        public void onSuccess(String file);
    }

    private FileServerTaskListener mListener;
    private ServerSocket mServerSocket;

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
    protected void onCancelled() {
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
                Log.d(ShareActivity.TAG, "Server: Socket close");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onCancelled();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            mServerSocket = new ServerSocket(8988);
            Log.d(ShareActivity.TAG, "Server: Socket opened");
            Socket client = mServerSocket.accept();
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
            mServerSocket.close();
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
        mListener.onSuccess(result);
    }
}