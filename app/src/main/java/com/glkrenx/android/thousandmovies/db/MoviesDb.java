package com.glkrenx.android.thousandmovies.db;

import android.provider.BaseColumns;

/**
 * Created by GLkrenx on 05/04/2016.
 */
public class MoviesDb {
    public static final class Movies implements BaseColumns{
        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_TAHUN = "tahun";
        public static final String COLUMN_DESKRIPSI = "deskripsi";
        public static final String COLUMN_GENRE = "genre";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_PATH = "path";
    }
}
