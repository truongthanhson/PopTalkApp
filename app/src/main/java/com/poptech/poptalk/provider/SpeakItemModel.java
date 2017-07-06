package com.poptech.poptalk.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.poptech.poptalk.BaseModel;
import com.poptech.poptalk.bean.SpeakItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 27/04/2017.
 */

public class SpeakItemModel implements BaseModel {

    PopTalkDatabase mDatabase;

    @Inject
    public SpeakItemModel(PopTalkDatabase database) {
        this.mDatabase = database;
    }

    public interface SpeakItemQuery {
        String[] projections = new String[]{
                PopTalkContract.SpeakItems._ID,
                PopTalkContract.SpeakItems.SPEAK_ITEM_ID,
                PopTalkContract.SpeakItems.SPEAK_ITEM_AUDIO_PATH,
                PopTalkContract.SpeakItems.SPEAK_ITEM_AUDIO_MARK,
                PopTalkContract.SpeakItems.SPEAK_ITEM_AUDIO_DURATION,
                PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_PATH,
                PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_DESCRIPTION1,
                PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_DESCRIPTION2,
                PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_LOCATION,
                PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_LATITUDE,
                PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_LONGITUDE,
                PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_DATETIME,
                PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_LANGUAGE,
                PopTalkContract.SpeakItems.SPEAK_ITEM_ADDED_TIME,
                PopTalkContract.SpeakItems.SPEAK_ITEM_NUM_ACCESS,
                PopTalkContract.SpeakItems.COLLECTION_ID
        };
        int SPEAK_ITEM_ID = 1;
        int SPEAK_ITEM_AUDIO_PATH = 2;
        int SPEAK_ITEM_AUDIO_MARK = 3;
        int SPEAK_ITEM_AUDIO_DURATION = 4;
        int SPEAK_ITEM_PHOTO_PATH = 5;
        int SPEAK_ITEM_PHOTO_DESCRIPTION1 = 6;
        int SPEAK_ITEM_PHOTO_DESCRIPTION2 = 7;
        int SPEAK_ITEM_PHOTO_LOCATION = 8;
        int SPEAK_ITEM_PHOTO_LATITUDE = 9;
        int SPEAK_ITEM_PHOTO_LONGITUDE = 10;
        int SPEAK_ITEM_PHOTO_DATETIME = 11;
        int SPEAK_ITEM_PHOTO_LANGUAGE = 12;
        int SPEAK_ITEM_ADDED_TIME = 13;
        int SPEAK_ITEM_NUM_ACCESS = 14;
        int SPEAK_ITEM_COLLECTION_ID = 15;
    }

    public long addNewSpeakItem(SpeakItem speakItem) {
        synchronized (this) {
            long ret = 0;
            SQLiteDatabase database = mDatabase.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_ID, speakItem.getId());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_AUDIO_PATH, speakItem.getAudioPath());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_AUDIO_MARK, speakItem.getAudioMark());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_AUDIO_DURATION, speakItem.getAudioDuration());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_PATH, speakItem.getPhotoPath());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_DESCRIPTION1, speakItem.getDescription1());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_DESCRIPTION2, speakItem.getDescription2());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_LOCATION, speakItem.getLocation());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_LATITUDE, speakItem.getLatitude());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_LONGITUDE, speakItem.getLongitude());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_DATETIME, speakItem.getDateTime());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_LANGUAGE, speakItem.getLanguage());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_ADDED_TIME, speakItem.getAddedTime());
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_NUM_ACCESS, speakItem.getNumAccess());
            contentValues.put(PopTalkContract.SpeakItems.COLLECTION_ID, speakItem.getCollectionId());

            ret = database.insert(PopTalkContract.Tables.SPEAK_ITEMS, null, contentValues);
            return ret;
        }
    }

    public List<SpeakItem> getSpeakItems() {
        synchronized (this) {
            List<SpeakItem> speakItems = new ArrayList<>();
            Cursor cursor = null;

            try {
                SQLiteDatabase database = mDatabase.getWritableDatabase();
                cursor = database.query(PopTalkContract.Tables.SPEAK_ITEMS,
                        SpeakItemQuery.projections, null, null, null, null, null);

                if (cursor.moveToFirst()) {
                    do {
                        SpeakItem speakItem = new SpeakItem();
                        speakItem.setId(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_ID));
                        speakItem.setAudioPath(cursor.getString(SpeakItemQuery.SPEAK_ITEM_AUDIO_PATH));
                        speakItem.setAudioMark(cursor.getString(SpeakItemQuery.SPEAK_ITEM_AUDIO_MARK));
                        speakItem.setAudioDuration(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_AUDIO_DURATION));
                        speakItem.setPhotoPath(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_PATH));
                        speakItem.setDescription1(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_DESCRIPTION1));
                        speakItem.setDescription2(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_DESCRIPTION2));
                        speakItem.setLocation(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_LOCATION));
                        speakItem.setLatitude(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_LATITUDE));
                        speakItem.setLongitude(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_LONGITUDE));
                        speakItem.setDateTime(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_DATETIME));
                        speakItem.setLanguage(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_LANGUAGE));
                        speakItem.setAddedTime(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_ADDED_TIME));
                        speakItem.setNumAccess(cursor.getInt(SpeakItemQuery.SPEAK_ITEM_NUM_ACCESS));
                        speakItem.setCollectionId(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_COLLECTION_ID));
                        speakItems.add(speakItem);
                    } while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return speakItems;
        }
    }

    public SpeakItem getSpeakItem(long withSpeakItemId) {
        synchronized (this) {
            SpeakItem speakItem = new SpeakItem();
            Cursor cursor = null;
            try {
                SQLiteDatabase database = mDatabase.getWritableDatabase();

                cursor = database.query(PopTalkContract.Tables.SPEAK_ITEMS,
                        SpeakItemQuery.projections,
                        PopTalkContract.SpeakItems.SPEAK_ITEM_ID + " = ?",
                        new String[]{"" + withSpeakItemId},
                        null,
                        null,
                        null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            speakItem.setId(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_ID));
                            speakItem.setAudioPath(cursor.getString(SpeakItemQuery.SPEAK_ITEM_AUDIO_PATH));
                            speakItem.setAudioMark(cursor.getString(SpeakItemQuery.SPEAK_ITEM_AUDIO_MARK));
                            speakItem.setAudioDuration(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_AUDIO_DURATION));
                            speakItem.setPhotoPath(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_PATH));
                            speakItem.setDescription1(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_DESCRIPTION1));
                            speakItem.setDescription2(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_DESCRIPTION2));
                            speakItem.setLocation(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_LOCATION));
                            speakItem.setLatitude(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_LATITUDE));
                            speakItem.setLongitude(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_LONGITUDE));
                            speakItem.setDateTime(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_DATETIME));
                            speakItem.setLanguage(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_LANGUAGE));
                            speakItem.setAddedTime(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_ADDED_TIME));
                            speakItem.setNumAccess(cursor.getInt(SpeakItemQuery.SPEAK_ITEM_NUM_ACCESS));
                            speakItem.setCollectionId(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_COLLECTION_ID));
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return speakItem;
        }
    }

    public List<SpeakItem> getSpeakItems(long withCollectionId) {
        synchronized (this) {
            List<SpeakItem> speakItems = new ArrayList<>();
            Cursor cursor = null;
            try {
                SQLiteDatabase database = mDatabase.getWritableDatabase();
                cursor = database.query(PopTalkContract.Tables.SPEAK_ITEMS,
                        SpeakItemQuery.projections,
                        PopTalkContract.SpeakItems.COLLECTION_ID + " = ?",
                        new String[]{"" + withCollectionId},
                        null,
                        null,
                        null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            SpeakItem speakItem = new SpeakItem();
                            speakItem.setId(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_ID));
                            speakItem.setAudioPath(cursor.getString(SpeakItemQuery.SPEAK_ITEM_AUDIO_PATH));
                            speakItem.setAudioMark(cursor.getString(SpeakItemQuery.SPEAK_ITEM_AUDIO_MARK));
                            speakItem.setAudioDuration(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_AUDIO_DURATION));
                            speakItem.setPhotoPath(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_PATH));
                            speakItem.setDescription1(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_DESCRIPTION1));
                            speakItem.setDescription2(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_DESCRIPTION2));
                            speakItem.setLocation(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_LOCATION));
                            speakItem.setLatitude(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_LATITUDE));
                            speakItem.setLongitude(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_LONGITUDE));
                            speakItem.setDateTime(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_DATETIME));
                            speakItem.setLanguage(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_LANGUAGE));
                            speakItem.setAddedTime(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_ADDED_TIME));
                            speakItem.setNumAccess(cursor.getInt(SpeakItemQuery.SPEAK_ITEM_NUM_ACCESS));
                            speakItem.setCollectionId(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_COLLECTION_ID));
                            speakItems.add(speakItem);
                        } while (cursor.moveToNext());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            Collections.sort(speakItems, (o1, o2) -> (
                    o1.getAddedTime() < o2.getAddedTime()) ? 1 :
                    ((o1.getAddedTime() == o2.getAddedTime()) ? 0 : -1));
            return speakItems;
        }
    }


    public long updateSpeakItem(SpeakItem speakItem) {
        long ret = 0;
        ContentValues contentValues = new ContentValues();
        synchronized (this) {
            try {
                SQLiteDatabase database = mDatabase.getWritableDatabase();

                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_ID, speakItem.getId());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_AUDIO_PATH, speakItem.getAudioPath());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_AUDIO_MARK, speakItem.getAudioMark());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_AUDIO_DURATION, speakItem.getAudioDuration());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_PATH, speakItem.getPhotoPath());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_DESCRIPTION1, speakItem.getDescription1());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_DESCRIPTION2, speakItem.getDescription2());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_LOCATION, speakItem.getLocation());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_LATITUDE, speakItem.getLatitude());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_LONGITUDE, speakItem.getLongitude());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_DATETIME, speakItem.getDateTime());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_LANGUAGE, speakItem.getLanguage());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_ADDED_TIME, speakItem.getAddedTime());
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_NUM_ACCESS, speakItem.getNumAccess());
                contentValues.put(PopTalkContract.SpeakItems.COLLECTION_ID, speakItem.getCollectionId());

                ret = database.update(PopTalkContract.Tables.SPEAK_ITEMS,
                        contentValues,
                        PopTalkContract.SpeakItems.SPEAK_ITEM_ID + " = ?",
                        new String[]{"" + speakItem.getId()});
                if (ret < 0) {
                    ret = addNewSpeakItem(speakItem);
                }
                return ret;
            } catch (Exception exception) {
                exception.printStackTrace();
                return -1;
            } finally {
            }
        }
    }
}
