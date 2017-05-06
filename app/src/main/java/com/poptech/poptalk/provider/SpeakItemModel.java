package com.poptech.poptalk.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    public List<SpeakItem> getSpeakItems() {
        return querySpeakItems();
    }

    private interface SpeakItemQuery{
        String[] projections = new String[]{
                PopTalkContract.SpeakItems._ID,
                PopTalkContract.SpeakItems.SPEAK_ITEM_MARK,
                PopTalkContract.SpeakItems.SPEAK_ITEM_PATH,
                PopTalkContract.SpeakItems.SPEAK_ITEM_DESCRIPTION,
                PopTalkContract.SpeakItems.COLLECTION_ID
        };

        int SPEAK_ITEM_ID = 0;
        int SPEAK_ITEM_MARK = 1;
        int SPEAK_ITEM_PATH = 2;
        int SPEAK_ITEM_DESCRIPTION = 3;
        int SPEAK_ITEM_COLLECTION_ID = 4;
    }

    public void addNewSpeakItem(SpeakItem speakItem){
        SQLiteDatabase database = mDatabase.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_MARK, speakItem.getMark());
        contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_PATH, speakItem.getPhotoPath());
        contentValues.put(PopTalkContract.SpeakItems.SPEAK_ITEM_DESCRIPTION, speakItem.getDescription());
        contentValues.put(PopTalkContract.SpeakItems.COLLECTION_ID, speakItem.getCollectionId());

        database.insert(PopTalkContract.Tables.SPEAK_ITEMS, null, contentValues);
    }

    public List<SpeakItem> querySpeakItems(){
        List<SpeakItem> speakItems = new ArrayList<>();
        Cursor cursor = null;

        try {
            SQLiteDatabase database = mDatabase.getWritableDatabase();

            cursor = database.query(PopTalkContract.Tables.SPEAK_ITEMS,
                                                            SpeakItemQuery.projections,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null);

            if(cursor.moveToFirst()){
                do{
                    SpeakItem speakItem = new SpeakItem();
                    speakItem.setId(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_ID));
                    speakItem.setMark(cursor.getString(SpeakItemQuery.SPEAK_ITEM_MARK));
                    speakItem.setPhotoPath(cursor.getString(SpeakItemQuery.SPEAK_ITEM_PATH));
                    speakItem.setDescription(cursor.getString(SpeakItemQuery.SPEAK_ITEM_DESCRIPTION));
                    speakItem.setCollectionId(cursor.getLong(SpeakItemQuery.SPEAK_ITEM_COLLECTION_ID));

                    speakItems.add(speakItem);
                }while (cursor.moveToNext());
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return speakItems;
    }

    //Testing purpose only
    public void generateTestData(){
        SpeakItem speakItem = new SpeakItem(1,"Ha Noi","","https://www.vietnamgrouptour.com/images/detailed/7/hanoi-full-day-city-tour.jpg",1);
        SpeakItem speakItem1 = new SpeakItem(2,"Sai Gon","","https://beetours.vn/images/news/ve-may-bay-di-sai-gon-49751.jpg",1);
        SpeakItem speakItem2 = new SpeakItem(3,"Hoi An","","https://cdn3.ivivu.com/2014/10/dia-diem-chup-hinh-dep-hoi-an-ivivu.com-4.jpg",1);
        SpeakItem speakItem3 = new SpeakItem(4,"Da Nang","","https://www.vietravel.com/images/news/2_14.jpg",1);
        SpeakItem speakItem4 = new SpeakItem(5,"Phu Yen","","http://phuyentourism.gov.vn/uploads/article/1470360052.jpg",1);
        SpeakItem speakItem5 = new SpeakItem(6,"Big Ben tower","","https://cdn.getyourguide.com/niwziy2l9cvz/4Ai0yNx1okqIysAwCO2My4/8e6697ca63fb89ea1ff3a8de691029d5/london-bigben-1500x850.jpg",2);
        SpeakItem speakItem6 = new SpeakItem(7,"stonehenge","","http://www.english-heritage.org.uk/remote/www.english-heritage.org.uk/content/properties/stonehenge/hero-carousel/stonehenge-circle-pink-sky?w=1440&h=612&mode=crop&scale=both&cache=always&quality=60&anchor=bottomcenter",2);
        SpeakItem speakItem7 = new SpeakItem(8,"Osaka","","http://congtyduhocnhatban.com/wp-content/uploads/2016/10/osaka-city.jpg",3);
        SpeakItem speakItem8 = new SpeakItem(9,"Tokyo","","http://congtyduhocnhatban.com/wp-content/uploads/2016/09/Tokyo-city.jpg",3);
        SpeakItem speakItem9 = new SpeakItem(10,"Kobe","","https://www.insidejapantours.com/csp/jap/insidejapan/images/destpages/123/slider/1.jpg",3);
        SpeakItem speakItem10 = new SpeakItem(11,"Kyoto","","http://www.fourseasons.com/content/dam/fourseasons/images/web/KYO/KYO_041_1280x486.jpg/jcr:content/renditions/cq5dam.web.1280.1280.jpeg",3);
        SpeakItem speakItem11 = new SpeakItem(12,"Berlin","","http://www.zapphyre.com/wp-content/uploads/2016/03/Berlin2.jpg",5);
        SpeakItem speakItem12 = new SpeakItem(13,"New York","","http://www.newyorker.com/wp-content/uploads/2015/12/Veix-Goodbye-New-York-Color-1200.jpg",4);
        SpeakItem speakItem13 = new SpeakItem(14,"San Francisco","","https://travel.com.vn/images/destination/Large/dc_150821_SanFrancisco.jpg",4);
        SpeakItem speakItem14 = new SpeakItem(15,"Washington","","https://hieuminh.files.wordpress.com/2015/04/washington-dc-labeled-for-reuse.jpg",4);
        SpeakItem speakItem15 = new SpeakItem(16,"Chicago","","http://i.huffpost.com/gen/4328520/images/o-CHICAGO-facebook.jpg",4);
        SpeakItem speakItem16 = new SpeakItem(17,"Texas","","https://www.choicehotels.com/cms/images/choice-hotels/demand-articles/know-before-you-go-about-dallas/know-before-you-go-about-dallas.jpg",4);
        SpeakItem speakItem17 = new SpeakItem(18,"Boston","","https://upload.wikimedia.org/wikipedia/commons/thumb/f/ff/Panoramic_Boston.jpg/800px-Panoramic_Boston.jpg",4);
        SpeakItem speakItem18 = new SpeakItem(19,"San Diego","","http://www.visitcalifornia.com/sites/default/files/styles/welcome_image/public/SanDiego_Skyline_JohnBahu_1280x642_downsized.jpg",4);
        SpeakItem speakItem19 = new SpeakItem(20,"Florida","","http://baotreonline.com/wp-content/uploads/2016/11/FLORIDA.jpg",4);

        speakItem.setCollectionId(1);
        speakItem1.setCollectionId(1);
        speakItem2.setCollectionId(1);
        speakItem3.setCollectionId(1);
        speakItem4.setCollectionId(1);
        speakItem5.setCollectionId(2);
        speakItem6.setCollectionId(2);
        speakItem7.setCollectionId(3);
        speakItem8.setCollectionId(3);
        speakItem9.setCollectionId(3);
        speakItem10.setCollectionId(3);
        speakItem11.setCollectionId(5);
        speakItem12.setCollectionId(4);
        speakItem13.setCollectionId(4);
        speakItem14.setCollectionId(4);
        speakItem15.setCollectionId(4);
        speakItem16.setCollectionId(4);
        speakItem17.setCollectionId(4);
        speakItem18.setCollectionId(4);
        speakItem19.setCollectionId(4);

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
