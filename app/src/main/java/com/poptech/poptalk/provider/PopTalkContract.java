package com.poptech.poptalk.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by sontt on 25/04/2017.
 */

public class PopTalkContract {

    public interface SyncColumn {
        /**
         * Last time this entry was updated or synchronized.
         */
        String UPDATED = "updated";
    }

    public interface CredentialsColumns {
        String CREDENTIALS_NAME = "name";
        String CREDENTIALS_EMAIL = "email";
        String CREDENTIALS_PHONE = "phone";
        String CREDENTIALS_PASSWORD = "password";
        String CREDENTIALS_PROFILE_PICTURE = "profile_picture";
    }

    public interface Tables {
        String CREDENTIALS = "credentials";
        String PHOTOS = "photos";
        String COLLECTIONS = "collections";
        String SPEAK_ITEMS = "speak_items";
        String LANGUAGES = "languages";
        String LANGUAGE_ITEMS = "language_items";
        String STORY_BOARDS = "story_boards";
    }

    public interface PhotoColumns {
        String PHOTO_PATH = "photo_path";
    }

    public interface CollectionsColumns {
        String COLLECTION_ID = "collection_id";
        String COLLECTION_THUMB_PATH = "collection_thumb_path";
        String COLLECTION_DESCRIPTION = "description";
        String COLLECTION_LANGUAGE = "language_1";
        String COLLECTION_NUM_SPEAK_ITEM = "num_speak_item";
        String COLLECTION_ADDED_TIME = "added_time";
        String COLLECTION_NUM_ACCESS = "num_access";
    }

    public interface SpeakItemColumns {
        String SPEAK_ITEM_ID = "speak_item_id";
        String SPEAK_ITEM_AUDIO_PATH = "audio_path";
        String SPEAK_ITEM_AUDIO_MARK = "audio_mark";
        String SPEAK_ITEM_AUDIO_DURATION = "audio_duration";
        String SPEAK_ITEM_PHOTO_PATH = "photo_path";
        String SPEAK_ITEM_PHOTO_DESCRIPTION1 = "photo_description1";
        String SPEAK_ITEM_PHOTO_DESCRIPTION2 = "photo_description2";
        String SPEAK_ITEM_PHOTO_LOCATION = "photo_location";
        String SPEAK_ITEM_PHOTO_LATITUDE = "photo_latitude";
        String SPEAK_ITEM_PHOTO_LONGITUDE = "photo_longitude";
        String SPEAK_ITEM_PHOTO_DATETIME = "photo_datetime";
        String SPEAK_ITEM_PHOTO_LANGUAGE = "photo_language";
        String SPEAK_ITEM_ADDED_TIME = "added_time";
        String SPEAK_ITEM_NUM_ACCESS = "num_access";
        String COLLECTION_ID = "collection_id";
    }

    public interface LanguagesColumns {
        String LANGUAGE_ACTIVE = "language_active";
    }

    public interface LanguageItemsColumns {
        String LANGUAGE_ITEM_NAME = "language_item_name";
        String LANGUAGE_ITEM_COMMENT = "language_item_comment";
    }

    public interface StoryBoardColumns {
        String SPEAK_ITEM_IDS = "speak_items";
        String CREATED_TIME = "created_time";
    }

    public static final String CONTENT_AUTHORITY = "com.poptech.poptalk";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_CREDENTIALS = "credentials";

    private static final String PATH_PHOTOS = "photos";

    private static final String PATH_SPEAK_ITEMS = "speak_items";

    private static final String PATH_COLLECTIONS = "collections";

    private static final String PATH_LANGUAGES = "languages";

    private static final String PATH_LANGUAGE_ITEMS = "language_items";

    private static final String PATH_STORY_BOARD = "storyboard";

    public static final String[] TOP_LEVEL_PATHS = {
            PATH_PHOTOS,
            PATH_SPEAK_ITEMS,
            PATH_COLLECTIONS,
            PATH_LANGUAGES,
            PATH_LANGUAGE_ITEMS,
            PATH_STORY_BOARD
    };

    public static class Credentials implements CredentialsColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CREDENTIALS).build();

        public static final String CONTENT_TYPE_ID = "credentials";

        /**
         * Build {@link Uri} that references all photos.
         */
        public static Uri buildCollectionsUri() {
            return CONTENT_URI;
        }
    }

    public static class Photos implements PhotoColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PHOTOS).build();

        public static final String CONTENT_TYPE_ID = "photos";

        /**
         * Build {@link Uri} that references all photos.
         */
        public static Uri buildCollectionsUri() {
            return CONTENT_URI;
        }
    }

    public static class Collections implements CollectionsColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COLLECTIONS).build();

        public static final String CONTENT_TYPE_ID = "collections";

        /**
         * Build {@link Uri} that references all collections.
         */
        public static Uri buildCollectionsUri() {
            return CONTENT_URI;
        }
    }

    public static class SpeakItems implements SpeakItemColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SPEAK_ITEMS).build();

        public static final String CONTENT_TYPE_ID = "speakItems";

        /**
         * Build {@link Uri} that references all speak items.
         */
        public static Uri buildCollectionsUri() {
            return CONTENT_URI;
        }
    }

    public static class Languages implements LanguagesColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LANGUAGES).build();

        public static final String CONTENT_TYPE_ID = "languages";

        /**
         * Build {@link Uri} that references all languages.
         */
        public static Uri buildCollectionsUri() {
            return CONTENT_URI;
        }
    }

    public static class LanguageItems implements LanguagesColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LANGUAGE_ITEMS).build();

        public static final String CONTENT_TYPE_ID = "language_items";

        /**
         * Build {@link Uri} that references all language items.
         */
        public static Uri buildCollectionsUri() {
            return CONTENT_URI;
        }
    }

    public static class StoryBoards implements StoryBoardColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LANGUAGE_ITEMS).build();

        public static final String CONTENT_TYPE_ID = "storyboard";

        /**
         * Build {@link Uri} that references all storyboard items.
         */
        public static Uri buildCollectionsUri() {
            return CONTENT_URI;
        }
    }

    private PopTalkContract() {

    }
}
