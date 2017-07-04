package com.poptech.poptalk.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.poptech.poptalk.BaseModel;
import com.poptech.poptalk.bean.Collection;
import com.poptech.poptalk.bean.SpeakItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 27/04/2017.
 */

public class CollectionsModel implements BaseModel {

    PopTalkDatabase mDatabase;

    @Inject
    public CollectionsModel(PopTalkDatabase database) {
        this.mDatabase = database;
    }

    private interface CollectionQuery {
        String[] projections = new String[]{
                PopTalkContract.Collections._ID,
                PopTalkContract.Collections.COLLECTION_ID,
                PopTalkContract.Collections.COLLECTION_THUMB_PATH,
                PopTalkContract.Collections.COLLECTION_DESCRIPTION,
                PopTalkContract.Collections.COLLECTION_LANGUAGE,
                PopTalkContract.Collections.COLLECTION_NUM_SPEAK_ITEM,
                PopTalkContract.Collections.COLLECTION_ADDED_TIME,
                PopTalkContract.Collections.COLLECTION_NUM_ACCESS
        };

        int COLLECTION_ID = 1;
        int COLLECTION_THUMB_PATH = 2;
        int COLLECTION_DESCRIPTION = 3;
        int COLLECTION_LANGUAGE = 4;
        int COLLECTION_NUM_SPEAK_ITEM = 5;
        int COLLECTION_ADDED_TIME = 6;
        int COLLECTION_NUM_ACCESS = 7;
    }

    public void addNewCollection(Collection collection) {
        SQLiteDatabase database = mDatabase.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(PopTalkContract.Collections.COLLECTION_ID, collection.getId());
        contentValues.put(PopTalkContract.Collections.COLLECTION_DESCRIPTION, collection.getDescription());
        contentValues.put(PopTalkContract.Collections.COLLECTION_ADDED_TIME, collection.getAddedTime());
        contentValues.put(PopTalkContract.Collections.COLLECTION_NUM_ACCESS, collection.getNumAccess());
        database.insert(PopTalkContract.Tables.COLLECTIONS, null, contentValues);
    }


    public List<Collection> getCollections() {
        List<Collection> collections = new ArrayList<>();
        Cursor cursor = null;
        synchronized (this) {
            try {
                SQLiteDatabase database = mDatabase.getWritableDatabase();
                cursor = database.query(PopTalkContract.Tables.COLLECTIONS,
                        CollectionQuery.projections,
                        null,
                        null,
                        null,
                        null,
                        null);

                if (cursor.moveToFirst()) {
                    do {
                        Collection collection = new Collection();
                        collection.setId(cursor.getLong(CollectionQuery.COLLECTION_ID));
                        collection.setDescription(cursor.getString(CollectionQuery.COLLECTION_DESCRIPTION));
                        collection.setAddedTime(cursor.getLong(CollectionQuery.COLLECTION_ADDED_TIME));
                        collection.setNumAccess(cursor.getInt(CollectionQuery.COLLECTION_NUM_ACCESS));
                        collections.add(collection);
                    } while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return collections;
    }

    public Collection getCollection(long withCollectionId) {
        synchronized (this) {
            Collection collection = new Collection();
            Cursor cursor = null;
            try {
                SQLiteDatabase database = mDatabase.getWritableDatabase();

                cursor = database.query(PopTalkContract.Tables.COLLECTIONS,
                        CollectionsModel.CollectionQuery.projections,
                        PopTalkContract.Collections.COLLECTION_ID + " = ?",
                        new String[]{"" + withCollectionId},
                        null,
                        null,
                        null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            collection.setId(cursor.getLong(CollectionQuery.COLLECTION_ID));
                            collection.setDescription(cursor.getString(CollectionQuery.COLLECTION_DESCRIPTION));
                            collection.setAddedTime(cursor.getLong(CollectionQuery.COLLECTION_ADDED_TIME));
                            collection.setNumAccess(cursor.getInt(CollectionQuery.COLLECTION_NUM_ACCESS));
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
            return collection;
        }
    }

    public long updateCollection(Collection collection) {
        long ret = 0;
        ContentValues contentValues = new ContentValues();
        synchronized (this) {
            try {
                SQLiteDatabase database = mDatabase.getWritableDatabase();

                contentValues.put(PopTalkContract.Collections.COLLECTION_ID, collection.getId());
                contentValues.put(PopTalkContract.Collections.COLLECTION_DESCRIPTION, collection.getDescription());
                contentValues.put(PopTalkContract.Collections.COLLECTION_ADDED_TIME, collection.getAddedTime());
                contentValues.put(PopTalkContract.Collections.COLLECTION_NUM_ACCESS, collection.getNumAccess());

                ret = database.update(PopTalkContract.Tables.COLLECTIONS,
                        contentValues,
                        PopTalkContract.Collections.COLLECTION_ID + " = ?",
                        new String[]{"" + collection.getId()});
                if (ret < 0) {
                    addNewCollection(collection);
                }
                return ret;
            } catch (Exception exception) {
                exception.printStackTrace();
                return -1;
            } finally {
            }
        }
    }

    public boolean isCollectionExisted(long withCollectionId) {
        boolean ret = false;
        synchronized (this) {
            Cursor cursor = null;
            try {
                SQLiteDatabase database = mDatabase.getWritableDatabase();
                cursor = database.query(PopTalkContract.Tables.COLLECTIONS,
                        CollectionsModel.CollectionQuery.projections,
                        PopTalkContract.Collections.COLLECTION_ID + " = ?",
                        new String[]{"" + withCollectionId},
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
}
