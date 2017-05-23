package com.poptech.poptalk.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.poptech.poptalk.bean.StoryBoard;

import static com.poptech.poptalk.utils.LogUtils.makeLogTag;
import static com.poptech.poptalk.provider.PopTalkContract.*;

/**
 * Created by sontt on 25/04/2017.
 */

public class PopTalkDatabase extends SQLiteOpenHelper {
    private static final String TAG = makeLogTag(PopTalkDatabase.class);
    private static final String DATABASE_NAME = "poptalk.db";
    private static final int VER_1_2 = 1;
    private static final int CURRENT_DATABASE_VERSION = VER_1_2;

    private final Context mContext;

    public static final String SQL_CREATE_CREDENTIALS = "CREATE TABLE " + Tables.CREDENTIALS + " ("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + CredentialsColumns.CREDENTIALS_NAME + " TEXT,"
            + CredentialsColumns.CREDENTIALS_EMAIL + " TEXT,"
            + CredentialsColumns.CREDENTIALS_PASSWORD + " TEXT,"
            + CredentialsColumns.CREDENTIALS_PHONE + " TEXT,"
            + CredentialsColumns.CREDENTIALS_PROFILE_PICTURE + " TEXT)";

    public static final String SQL_CREATE_COLLECTIONS = "CREATE TABLE " + Tables.COLLECTIONS + " ("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + CollectionsColumns.COLLECTION_ID + " INTEGER,"
            + CollectionsColumns.COLLECTION_THUMB_PATH + " TEXT,"
            + CollectionsColumns.COLLECTION_DESCRIPTION + " TEXT,"
            + CollectionsColumns.COLLECTION_LANGUAGE + " TEXT,"
            + CollectionsColumns.COLLECTION_NUM_SPEAK_ITEM + " INTEGER,"
            + CollectionsColumns.COLLECTION_ADDED_TIME + " INTEGER,"
            + CollectionsColumns.COLLECTION_NUM_ACCESS + " INTEGER)";

    public static final String SQL_CREATE_PHOTOS = "CREATE TABLE " + Tables.PHOTOS + " ("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + PhotoColumns.PHOTO_PATH + " TEXT)";

    public static final String SQL_CREATE_SPEAK_ITEMS = "CREATE TABLE " + Tables.SPEAK_ITEMS + " ("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + SpeakItemColumns.SPEAK_ITEM_ID + " INTEGER,"
            + SpeakItemColumns.SPEAK_ITEM_AUDIO_PATH + " TEXT,"
            + SpeakItemColumns.SPEAK_ITEM_AUDIO_MARK + " TEXT,"
            + SpeakItemColumns.SPEAK_ITEM_AUDIO_DURATION + " INTEGER,"
            + SpeakItemColumns.SPEAK_ITEM_PHOTO_PATH + " TEXT,"
            + SpeakItemColumns.SPEAK_ITEM_PHOTO_DESCRIPTION + " TEXT, "
            + SpeakItemColumns.SPEAK_ITEM_PHOTO_LOCATION + " TEXT, "
            + SpeakItemColumns.SPEAK_ITEM_PHOTO_LATITUDE + " TEXT, "
            + SpeakItemColumns.SPEAK_ITEM_PHOTO_LONGITUDE + " TEXT, "
            + SpeakItemColumns.SPEAK_ITEM_PHOTO_DATETIME + " TEXT, "
            + SpeakItemColumns.SPEAK_ITEM_PHOTO_LANGUAGE + " TEXT, "
            + SpeakItemColumns.SPEAK_ITEM_ADDED_TIME + " INTEGER, "
            + SpeakItemColumns.SPEAK_ITEM_NUM_ACCESS + " INTEGER, "
            + SpeakItemColumns.COLLECTION_ID + " INTEGER)";

    public static final String SQL_CREATE_LANGUAGES = "CREATE TABLE " + Tables.LANGUAGES + " ("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + LanguagesColumns.LANGUAGE_ACTIVE + " TEXT)";

    public static final String SQL_CREATE_LANGUAGE_ITEMS = "CREATE TABLE " + Tables.LANGUAGE_ITEMS + " ("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + LanguageItemsColumns.LANGUAGE_ITEM_NAME + " TEXT,"
            + LanguageItemsColumns.LANGUAGE_ITEM_COMMENT + " TEXT)";

    public static final String SQL_CREATE_STORY_BOARD = "CREATE TABLE " + Tables.STORY_BOARDS + " ("
            + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + StoryBoardColumns.SPEAK_ITEM_IDS + " TEXT, "
            + StoryBoardColumns.CREATED_TIME + " INTEGER)";


    public PopTalkDatabase(Context context) {
        super(context, DATABASE_NAME, null, CURRENT_DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CREDENTIALS);
        db.execSQL(SQL_CREATE_PHOTOS);
        db.execSQL(SQL_CREATE_COLLECTIONS);
        db.execSQL(SQL_CREATE_SPEAK_ITEMS);
        db.execSQL(SQL_CREATE_LANGUAGES);
        db.execSQL(SQL_CREATE_LANGUAGE_ITEMS);
        db.execSQL(SQL_CREATE_STORY_BOARD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
