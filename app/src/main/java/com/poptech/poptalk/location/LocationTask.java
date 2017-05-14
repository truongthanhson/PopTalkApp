package com.poptech.poptalk.location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.poptech.poptalk.utils.StringUtils;

/**
 * An AsyncTask that calls getFromLocation() in the background. The class uses
 * the following generic types: Location - A {@link Location}
 * object containing the current location, passed as the input parameter to
 * doInBackground() Void - indicates that progress units are not used by this
 * subclass String - An address passed to onPostExecute()
 */
public class LocationTask extends AsyncTask<Location, Void, String> {

    public interface onGetAddressTaskListener {
        public void onStart();

        public void onSuccess(String address);
    }

    private onGetAddressTaskListener mListener;

    private static final String TAG = "LocationTask";

    public void setListener(onGetAddressTaskListener mListener) {
        this.mListener = mListener;
    }

    // Store the context passed to the AsyncTask when the system instantiates
    // it.
    private Context mContext;

    // Constructor called by the system to instantiate the task
    public LocationTask(Context context) {

        // Required by the semantics of AsyncTask
        super();

        // Set a Context for the background task
        mContext = context;
    }

    /**
     * Get a geocoding service instance, pass latitude and longitude to it,
     * format the returned address, and return the address to the UI thread.
     */
    @Override
    protected String doInBackground(Location... params) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        Location location = params[0];
        List<Address> addresses = null;

        if (location != null) {
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException exception1) {
                Log.e(TAG, exception1.getMessage());
                exception1.printStackTrace();
                return "";
            } catch (IllegalArgumentException exception2) {
                Log.e(TAG, exception2.getMessage());
                exception2.printStackTrace();
                return "";
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
                ex.printStackTrace();
            }
        }


        if (addresses != null && addresses.size() > 0) {
            // Get the first address
            Address address = addresses.get(0);
            StringBuilder stringBuilder = new StringBuilder();
            List<String> stringList = new ArrayList<>();

            if (!StringUtils.isNullOrEmpty(address.getLocality())) {
                stringList.add(address.getLocality());
            } else if (!StringUtils.isNullOrEmpty(address.getAdminArea())) {
                stringList.add(address.getAdminArea());
            }
            if (!StringUtils.isNullOrEmpty(address.getCountryName())) {
                stringList.add(address.getCountryName());
            }
            for (int i = 0; i < stringList.size(); i++) {
                stringBuilder.append(stringList.get(i));
                if (i < stringList.size() - 1) {
                    stringBuilder.append(", ");
                }
            }
            // Return the text
            return stringBuilder.toString();

            // If there aren't any addresses, post a message
        } else {
            return "";
        }
    }

    /**
     * A method that's called once doInBackground() completes. Set the text of
     * the UI element that displays the address. This method runs on the UI
     * thread.
     */
    @Override
    protected void onPostExecute(String address) {
        mListener.onSuccess(address);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onStart();
    }
}
