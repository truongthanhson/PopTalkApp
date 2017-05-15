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
        contentValues.put(PopTalkContract.Collections.COLLECTION_THUMB_PATH, collection.getThumbPath());
        contentValues.put(PopTalkContract.Collections.COLLECTION_DESCRIPTION, collection.getDescription());
        contentValues.put(PopTalkContract.Collections.COLLECTION_LANGUAGE, collection.getLanguage());
        contentValues.put(PopTalkContract.Collections.COLLECTION_NUM_SPEAK_ITEM, collection.getNumSpeakItem());
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
                        collection.setThumbPath(cursor.getString(CollectionQuery.COLLECTION_THUMB_PATH));
                        collection.setDescription(cursor.getString(CollectionQuery.COLLECTION_DESCRIPTION));
                        collection.setLanguage(cursor.getString(CollectionQuery.COLLECTION_LANGUAGE));
                        collection.setNumSpeakItem(cursor.getInt(CollectionQuery.COLLECTION_NUM_SPEAK_ITEM));
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
                            collection.setThumbPath(cursor.getString(CollectionQuery.COLLECTION_THUMB_PATH));
                            collection.setDescription(cursor.getString(CollectionQuery.COLLECTION_DESCRIPTION));
                            collection.setLanguage(cursor.getString(CollectionQuery.COLLECTION_LANGUAGE));
                            collection.setNumSpeakItem(cursor.getInt(CollectionQuery.COLLECTION_NUM_SPEAK_ITEM));
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
                contentValues.put(PopTalkContract.Collections.COLLECTION_THUMB_PATH, collection.getThumbPath());
                contentValues.put(PopTalkContract.Collections.COLLECTION_DESCRIPTION, collection.getDescription());
                contentValues.put(PopTalkContract.Collections.COLLECTION_LANGUAGE, collection.getLanguage());
                contentValues.put(PopTalkContract.Collections.COLLECTION_NUM_SPEAK_ITEM, collection.getNumSpeakItem());
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


    //Testing purpose only
    public void generateTestData() {
        ArrayList<Collection> collections = new ArrayList<>();
        Collection collection1 = new Collection(1, "collection1", "Vietnam", "http://kids.nationalgeographic.com/content/dam/kids/photos/Countries/Q-Z/vietnam-ha-long-bay.ngsversion.1412614607489.jpg");
        Collection collection2 = new Collection(2, "collection2", "England", "https://images.goaheadtours.com/tour-tile/5176_80_2500_0/a-view-of-big-ben-london-eye-and-the-parliament-in-london-england.jpg");
        Collection collection3 = new Collection(3, "collection3", "Japan", "http://japan-magazine.jnto.go.jp/jnto2wm/wp-content/uploads/1608_special_TOTO_main.jpg");
        Collection collection4 = new Collection(4, "collection4", "USA", "http://wikitravel.org/upload/shared//thumb/3/37/Space_Needle_Mount_Ranier_Seattle_Washington_USA.jpg/390px-Space_Needle_Mount_Ranier_Seattle_Washington_USA.jpg");
        Collection collection5 = new Collection(5, "collection5", "Germany", "https://www.studyabroad.com/sites/default/files/images/Summer-Study-Abroad-Germany-Programs.jpg");
        Collection collection6 = new Collection(6, "collection6", "Australia", "http://www.princess.com/images/learn/cruise-destinations/australia-new-zealand-cruises/overview/AU-cruises-640x420.jpg");
        Collection collection7 = new Collection(7, "collection7", "Malaysia", "http://apttravel.com/documents/10194/0/port-klang-malaysia.jpg");
        Collection collection8 = new Collection(8, "collection8", "Switzerland", "http://www.fodors.com/ee/files/slideshows/11675/switzerland-hero.jpg");
        Collection collection9 = new Collection(9, "collection9", "Sweden", "http://s3.amazonaws.com/iexplore_web/images/assets/000/006/463/original/sweden.jpg?1443530953");
        Collection collection10 = new Collection(10, "collection10", "Norway", "https://res.cloudinary.com/simpleview/image/upload/c_fill,f_auto,h_280,q_64,w_560/v1/clients/norway/unesco-geirangerfjord-skagefla-waterfall-2-1_6cc6a64a-a204-432e-8753-01ef2080f24e.jpg");
        collections.add(collection1);
        collections.add(collection2);
        collections.add(collection3);
        collections.add(collection4);
        collections.add(collection5);
        collections.add(collection6);
        collections.add(collection7);
        collections.add(collection8);
        collections.add(collection9);
        collections.add(collection10);

        for (Collection collection : collections) {
            addNewCollection(collection);
        }
    }

}
