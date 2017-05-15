package com.poptech.poptalk.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.poptech.poptalk.BaseModel;
import com.poptech.poptalk.bean.SpeakItem;

import java.util.ArrayList;
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

    private interface SpeakItemQuery {
        String[] projections = new String[]{
                PopTalkContract.SpeakItems._ID,
                PopTalkContract.SpeakItems.SPEAK_ITEM_ID,
                PopTalkContract.SpeakItems.SPEAK_ITEM_AUDIO_PATH,
                PopTalkContract.SpeakItems.SPEAK_ITEM_AUDIO_MARK,
                PopTalkContract.SpeakItems.SPEAK_ITEM_AUDIO_DURATION,
                PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_PATH,
                PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_DESCRIPTION,
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
        int SPEAK_ITEM_PHOTO_DESCRIPTION = 6;
        int SPEAK_ITEM_PHOTO_LOCATION = 7;
        int SPEAK_ITEM_PHOTO_LATITUDE = 8;
        int SPEAK_ITEM_PHOTO_LONGITUDE = 9;
        int SPEAK_ITEM_PHOTO_DATETIME = 10;
        int SPEAK_ITEM_PHOTO_LANGUAGE = 11;
        int SPEAK_ITEM_ADDED_TIME = 12;
        int SPEAK_ITEM_NUM_ACCESS = 13;
        int SPEAK_ITEM_COLLECTION_ID = 14;
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
            contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_DESCRIPTION, speakItem.getDescription());
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
                        speakItem.setAudioDuration(cursor.getInt(SpeakItemQuery.SPEAK_ITEM_AUDIO_DURATION));
                        speakItem.setPhotoPath(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_PATH));
                        speakItem.setDescription(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_DESCRIPTION));
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
                            speakItem.setAudioDuration(cursor.getInt(SpeakItemQuery.SPEAK_ITEM_AUDIO_DURATION));
                            speakItem.setPhotoPath(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_PATH));
                            speakItem.setDescription(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_DESCRIPTION));
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
                            speakItem.setAudioDuration(cursor.getInt(SpeakItemQuery.SPEAK_ITEM_AUDIO_DURATION));
                            speakItem.setPhotoPath(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_PATH));
                            speakItem.setDescription(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PHOTO_DESCRIPTION));
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
                contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PHOTO_DESCRIPTION, speakItem.getDescription());
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

    //Testing purpose only
    public void generateTestData() {
        SpeakItem speakItem = new SpeakItem(1, "Ha Noi, Vietnam", "May 08, 2017", "Ha Noi", "", "https://www.vietnamgrouptour.com/images/detailed/7/hanoi-full-day-city-tour.jpg", 1);
        SpeakItem speakItem1 = new SpeakItem(2, "Sai Gon, Vietnam", "May 08, 2017", "Sai Gon", "", "https://beetours.vn/images/news/ve-may-bay-di-sai-gon-49751.jpg", 1);
        SpeakItem speakItem2 = new SpeakItem(3, "Hoi An, Vietnam", "May 08, 2017", "Hoi An", "", "https://cdn3.ivivu.com/2014/10/dia-diem-chup-hinh-dep-hoi-an-ivivu.com-4.jpg", 1);
        SpeakItem speakItem3 = new SpeakItem(4, "Da Nang, Vietnam", "May 08, 2017", "Da Nang", "", "https://www.vietravel.com/images/news/2_14.jpg", 1);
        SpeakItem speakItem4 = new SpeakItem(5, "Phu Yen, Vietnam", "May 08, 2017", "Phu Yen", "", "http://phuyentourism.gov.vn/uploads/article/1470360052.jpg", 1);
        SpeakItem speakItem5 = new SpeakItem(6, "London, England", "May 08, 2017", "Big Ben tower", "", "https://cdn.getyourguide.com/niwziy2l9cvz/4Ai0yNx1okqIysAwCO2My4/8e6697ca63fb89ea1ff3a8de691029d5/london-bigben-1500x850.jpg", 2);
        SpeakItem speakItem6 = new SpeakItem(7, "Wiltshire, England", "May 08, 2017", "stonehenge", "", "http://www.english-heritage.org.uk/remote/www.english-heritage.org.uk/content/properties/stonehenge/hero-carousel/stonehenge-circle-pink-sky?w=1440&h=612&mode=crop&scale=both&cache=always&quality=60&anchor=bottomcenter", 2);
        SpeakItem speakItem7 = new SpeakItem(8, "Osaka, Japan", "May 08, 2017", "Osaka", "", "http://congtyduhocnhatban.com/wp-content/uploads/2016/10/osaka-city.jpg", 3);
        SpeakItem speakItem8 = new SpeakItem(9, "Tokyo, Japan", "May 08, 2017", "Tokyo", "", "http://congtyduhocnhatban.com/wp-content/uploads/2016/09/Tokyo-city.jpg", 3);
        SpeakItem speakItem9 = new SpeakItem(10, "Kobe, Japan", "May 08, 2017", "Kobe", "", "https://www.insidejapantours.com/csp/jap/insidejapan/images/destpages/123/slider/1.jpg", 3);
        SpeakItem speakItem10 = new SpeakItem(11, "Kyoto, Japan", "May 08, 2017", "Kyoto", "", "http://www.fourseasons.com/content/dam/fourseasons/images/web/KYO/KYO_041_1280x486.jpg/jcr:content/renditions/cq5dam.web.1280.1280.jpeg", 3);
        SpeakItem speakItem11 = new SpeakItem(12, "Berlin, Germany", "May 08, 2017", "Berlin", "", "http://www.zapphyre.com/wp-content/uploads/2016/03/Berlin2.jpg", 5);
        SpeakItem speakItem12 = new SpeakItem(13, "New York, USA", "May 08, 2017", "New York", "", "http://www.newyorker.com/wp-content/uploads/2015/12/Veix-Goodbye-New-York-Color-1200.jpg", 4);
        SpeakItem speakItem13 = new SpeakItem(14, "San Francisco, USA", "May 08, 2017", "San Francisco", "", "https://travel.com.vn/images/destination/Large/dc_150821_SanFrancisco.jpg", 4);
        SpeakItem speakItem14 = new SpeakItem(15, "Washington, USA", "May 08, 2017", "Washington", "", "https://hieuminh.files.wordpress.com/2015/04/washington-dc-labeled-for-reuse.jpg", 4);
        SpeakItem speakItem15 = new SpeakItem(16, "Chicago, USA", "May 08, 2017", "Chicago", "", "http://i.huffpost.com/gen/4328520/images/o-CHICAGO-facebook.jpg", 4);
        SpeakItem speakItem16 = new SpeakItem(17, "Texas, USA", "May 08, 2017", "Texas", "", "https://www.choicehotels.com/cms/images/choice-hotels/demand-articles/know-before-you-go-about-dallas/know-before-you-go-about-dallas.jpg", 4);
        SpeakItem speakItem17 = new SpeakItem(18, "Boston, USA", "May 08, 2017", "Boston", "", "https://upload.wikimedia.org/wikipedia/commons/thumb/f/ff/Panoramic_Boston.jpg/800px-Panoramic_Boston.jpg", 4);
        SpeakItem speakItem18 = new SpeakItem(19, "San Diego, USA", "May 08, 2017", "San Diego", "", "http://www.visitcalifornia.com/sites/default/files/styles/welcome_image/public/SanDiego_Skyline_JohnBahu_1280x642_downsized.jpg", 4);
        SpeakItem speakItem19 = new SpeakItem(20, "Florida, USA", "May 08, 2017", "Florida", "", "http://baotreonline.com/wp-content/uploads/2016/11/FLORIDA.jpg", 4);

        speakItem.setCollectionId(1);
        speakItem1.setCollectionId(1);
        speakItem2.setCollectionId(1);
        speakItem3.setCollectionId(2);
        speakItem4.setCollectionId(2);
        speakItem5.setCollectionId(2);
        speakItem6.setCollectionId(3);
        speakItem7.setCollectionId(3);
        speakItem8.setCollectionId(4);
        speakItem9.setCollectionId(4);
        speakItem10.setCollectionId(5);
        speakItem11.setCollectionId(5);
        speakItem12.setCollectionId(6);
        speakItem13.setCollectionId(7);
        speakItem14.setCollectionId(7);
        speakItem15.setCollectionId(7);
        speakItem16.setCollectionId(8);
        speakItem17.setCollectionId(8);
        speakItem18.setCollectionId(9);
        speakItem19.setCollectionId(10);

        addNewSpeakItem(speakItem);
        addNewSpeakItem(speakItem1);
        addNewSpeakItem(speakItem2);
        addNewSpeakItem(speakItem3);
        addNewSpeakItem(speakItem4);
        addNewSpeakItem(speakItem5);
        addNewSpeakItem(speakItem6);
        addNewSpeakItem(speakItem7);
        addNewSpeakItem(speakItem8);
        addNewSpeakItem(speakItem9);
        addNewSpeakItem(speakItem10);
        addNewSpeakItem(speakItem11);
        addNewSpeakItem(speakItem12);
        addNewSpeakItem(speakItem13);
        addNewSpeakItem(speakItem14);
        addNewSpeakItem(speakItem15);
        addNewSpeakItem(speakItem16);
        addNewSpeakItem(speakItem17);
        addNewSpeakItem(speakItem18);
        addNewSpeakItem(speakItem19);


    }

}
