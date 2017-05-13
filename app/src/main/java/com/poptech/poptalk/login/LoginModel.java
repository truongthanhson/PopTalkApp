package com.poptech.poptalk.login;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.poptech.poptalk.BaseModel;
import com.poptech.poptalk.bean.Credentials;
import com.poptech.poptalk.provider.PopTalkContract;
import com.poptech.poptalk.provider.PopTalkDatabase;

import javax.inject.Inject;

/**
 * Created by sontt on 26/04/2017.
 */

public class LoginModel implements BaseModel {
    private PopTalkDatabase mDatabase;

    @Inject
    public LoginModel(PopTalkDatabase mDatabase) {
        this.mDatabase = mDatabase;
    }

    private interface CredentialsQuery {
        String[] projections = new String[]{
                PopTalkContract.Credentials._ID,
                PopTalkContract.Credentials.CREDENTIALS_NAME,
                PopTalkContract.Credentials.CREDENTIALS_EMAIL,
                PopTalkContract.Credentials.CREDENTIALS_PASSWORD,
                PopTalkContract.Credentials.CREDENTIALS_PHONE,
                PopTalkContract.Credentials.CREDENTIALS_PROFILE_PICTURE
        };

        int CREDENTIALS_NAME = 1;
        int CREDENTIALS_EMAIL = 2;
        int CREDENTIALS_PASSWORD = 3;
        int CREDENTIALS_PHONE = 4;
        int CREDENTIALS_PROFILE = 5;
    }

    public void addNewCredentials(Credentials credentials) {
        SQLiteDatabase database = mDatabase.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(PopTalkContract.Credentials.CREDENTIALS_NAME, credentials.getName());
        contentValues.put(PopTalkContract.Credentials.CREDENTIALS_EMAIL, credentials.getEmail());
        contentValues.put(PopTalkContract.Credentials.CREDENTIALS_PASSWORD, credentials.getPassword());
        contentValues.put(PopTalkContract.Credentials.CREDENTIALS_PHONE, credentials.getPhone());
        contentValues.put(PopTalkContract.Credentials.CREDENTIALS_PROFILE_PICTURE, credentials.getProfilePicture());
        database.insert(PopTalkContract.Tables.CREDENTIALS, null, contentValues);
    }

    public Credentials getCredentials() {
        synchronized (this) {
            Credentials credentials = new Credentials();
            Cursor cursor = null;

            try {
                SQLiteDatabase database = mDatabase.getWritableDatabase();
                cursor = database.query(PopTalkContract.Tables.CREDENTIALS,
                        LoginModel.CredentialsQuery.projections, null, null, null, null, null);

                if (cursor.moveToFirst()) {
                    do {
                        credentials.setName(cursor.getString(CredentialsQuery.CREDENTIALS_NAME));
                        credentials.setEmail(cursor.getString(CredentialsQuery.CREDENTIALS_EMAIL));
                        credentials.setPassword(cursor.getString(CredentialsQuery.CREDENTIALS_PASSWORD));
                        credentials.setPhone(cursor.getString(CredentialsQuery.CREDENTIALS_PHONE));
                        credentials.setProfilePicture(cursor.getString(CredentialsQuery.CREDENTIALS_PROFILE));
                    } while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return credentials;
        }
    }

    public boolean isCredentialsExisted() {
        boolean ret = false;
        synchronized (this) {
            Cursor cursor = null;
            try {
                SQLiteDatabase database = mDatabase.getWritableDatabase();
                cursor = database.query(PopTalkContract.Tables.CREDENTIALS,
                        LoginModel.CredentialsQuery.projections,
                        null,
                        null,
                        null,
                        null,
                        null);
                if (cursor != null && cursor.getCount() > 0)
                    ret = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return ret;
    }

    public long updateCredentials(Credentials credentials) {
        long ret = 0;
        ContentValues contentValues = new ContentValues();
        synchronized (this) {
            try {
                SQLiteDatabase database = mDatabase.getWritableDatabase();
                contentValues.put(PopTalkContract.Credentials.CREDENTIALS_NAME, credentials.getName());
                contentValues.put(PopTalkContract.Credentials.CREDENTIALS_EMAIL, credentials.getEmail());
                contentValues.put(PopTalkContract.Credentials.CREDENTIALS_PASSWORD, credentials.getPassword());
                contentValues.put(PopTalkContract.Credentials.CREDENTIALS_PHONE, credentials.getPhone());
                contentValues.put(PopTalkContract.Credentials.CREDENTIALS_PROFILE_PICTURE, credentials.getProfilePicture());

                ret = database.update(PopTalkContract.Tables.CREDENTIALS,
                        contentValues,
                        null,
                        null);
                return ret;
            } catch (Exception exception) {
                exception.printStackTrace();
                return -1;
            } finally {
            }
        }
    }

}
