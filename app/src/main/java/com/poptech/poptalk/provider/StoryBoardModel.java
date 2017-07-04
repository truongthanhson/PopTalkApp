package com.poptech.poptalk.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.poptech.poptalk.BaseModel;
import com.poptech.poptalk.bean.SpeakItem;
import com.poptech.poptalk.bean.StoryBoard;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sontt on 27/04/2017.
 */

public class StoryBoardModel implements BaseModel {

    PopTalkDatabase mDatabase;

    @Inject
    public StoryBoardModel(PopTalkDatabase database) {
        this.mDatabase = database;
    }

    private interface StoryBoardQuery {
        String[] projections = new String[]{
                PopTalkContract.StoryBoards._ID,
                PopTalkContract.StoryBoards.STORYBOARD_ID,
                PopTalkContract.StoryBoards.STORYBOARD_NAME,
                PopTalkContract.StoryBoards.SPEAK_ITEM_IDS,
                PopTalkContract.StoryBoards.CREATED_TIME
        };
        int STORY_BOARD_ID = 1;
        int STORY_BOARD_NAME = 2;
        int SPEAK_ITEMS = 3;
        int CREATED_TIME = 4;
    }

    public long addNewStoryBoard(StoryBoard storyBoard) {
        synchronized (this) {
            long ret = 0;
            SQLiteDatabase database = mDatabase.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(PopTalkContract.StoryBoards.STORYBOARD_ID, storyBoard.getId());
            contentValues.put(PopTalkContract.StoryBoards.STORYBOARD_NAME, storyBoard.getName());
            contentValues.put(PopTalkContract.StoryBoards.SPEAK_ITEM_IDS, generateStringSpeakItems(storyBoard.getSpeakItems()));
            contentValues.put(PopTalkContract.StoryBoards.CREATED_TIME, storyBoard.getCreatedTime());
            ret = database.insert(PopTalkContract.Tables.STORY_BOARDS, null, contentValues);
            return ret;
        }
    }

    public List<StoryBoard> getStoryBoards() {
        synchronized (this) {
            List<StoryBoard> storyBoards = new ArrayList<>();
            Cursor cursor = null;

            try {
                SQLiteDatabase database = mDatabase.getWritableDatabase();
                cursor = database.query(PopTalkContract.Tables.STORY_BOARDS,
                        StoryBoardQuery.projections, null, null, null, null, null);

                if (cursor.moveToFirst()) {
                    do {
                        StoryBoard storyBoard = new StoryBoard();
                        storyBoard.setId(cursor.getLong(StoryBoardQuery.STORY_BOARD_ID));
                        storyBoard.setName(cursor.getString(StoryBoardQuery.STORY_BOARD_NAME));
                        storyBoard.setSpeakItems(parseSpeakItemsFromString(cursor.getString(StoryBoardQuery.SPEAK_ITEMS)));
                        storyBoard.setCreatedTime(cursor.getLong(StoryBoardQuery.CREATED_TIME));
                        storyBoards.add(storyBoard);
                    } while (cursor.moveToNext());
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return storyBoards;
        }
    }

    //TODO: refactor this ugly @@
    public List<SpeakItem> querySpeakItems(String[] fromIds){
        List<SpeakItem> speakItems = new ArrayList<>();

        // Build the string with all the IDs, e.g. "(1,2,3,4)"
        StringBuilder ids = new StringBuilder();
        ids.append("(");
        for(int i = 0; i < fromIds.length; i++) {
            ids.append(String.valueOf(fromIds[i]));
            if (i < fromIds.length - 1) {
                ids.append(",");
            }
        }
        ids.append(")");

        String selectQueryStr = "SELECT * FROM " + PopTalkContract.Tables.SPEAK_ITEMS + " WHERE " + PopTalkContract.SpeakItemColumns.SPEAK_ITEM_ID + " IN " + ids.toString();
        Cursor cursor = null;

        try {
            cursor = mDatabase.getWritableDatabase().rawQuery(selectQueryStr, null);
            if (cursor.moveToFirst()) {
                do {
                    SpeakItem speakItem = new SpeakItem();
                    speakItem.setId(cursor.getLong(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_ID));
                    speakItem.setAudioPath(cursor.getString(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_AUDIO_PATH));
                    speakItem.setAudioMark(cursor.getString(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_AUDIO_MARK));
                    speakItem.setAudioDuration(cursor.getLong(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_AUDIO_DURATION));
                    speakItem.setPhotoPath(cursor.getString(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_PHOTO_PATH));
                    speakItem.setDescription1(cursor.getString(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_PHOTO_DESCRIPTION1));
                    speakItem.setDescription2(cursor.getString(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_PHOTO_DESCRIPTION2));
                    speakItem.setLocation(cursor.getString(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_PHOTO_LOCATION));
                    speakItem.setLatitude(cursor.getString(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_PHOTO_LATITUDE));
                    speakItem.setLongitude(cursor.getString(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_PHOTO_LONGITUDE));
                    speakItem.setDateTime(cursor.getString(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_PHOTO_DATETIME));
                    speakItem.setLanguage(cursor.getString(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_PHOTO_LANGUAGE));
                    speakItem.setAddedTime(cursor.getLong(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_ADDED_TIME));
                    speakItem.setNumAccess(cursor.getInt(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_NUM_ACCESS));
                    speakItem.setCollectionId(cursor.getLong(SpeakItemModel.SpeakItemQuery.SPEAK_ITEM_COLLECTION_ID));
                    speakItems.add(speakItem);
                } while (cursor.moveToNext());
            }
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        List<SpeakItem> sortedSpeakItems = new ArrayList<>();
        //re-sort
        for (String id: fromIds) {
            for (SpeakItem speakItem: speakItems) {
                if (id.equalsIgnoreCase(speakItem.getId()+"")) {
                    sortedSpeakItems.add(speakItem);
                    break;
                }
            }
        }
        return sortedSpeakItems;
    }


    public String generateStringSpeakItems(List<SpeakItem> speakItems){
        String[] speakItemIds = new String[speakItems.size()];
        for(int i = 0; i < speakItems.size(); i++){
            speakItemIds[i] = speakItems.get(i).getId() + "";
        }
        return convertArrayToString(speakItemIds);
    }

    public List<SpeakItem> parseSpeakItemsFromString(String speakItemsString){
        String[] speakItemIds = convertStringToArray(speakItemsString);
        return querySpeakItems(speakItemIds);
    }

    public static String strSeparator = ";";
    public static String convertArrayToString(String[] array){
        String str = "";
        for (int i = 0;i<array.length; i++) {
            str = str+array[i];
            // Do not append comma at the end of last element
            if(i<array.length-1){
                str = str+strSeparator;
            }
        }
        return str;
    }
    public static String[] convertStringToArray(String str){
        String[] arr = str.split(strSeparator);
        return arr;
    }
}
