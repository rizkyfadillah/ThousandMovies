package com.glkrenx.android.thousandmovies.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by GLkrenx on 05/04/2016.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesDb.Movies.TABLE_NAME + " (" +
                MoviesDb.Movies._ID + " INTEGER PRIMARY KEY," +
                MoviesDb.Movies.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesDb.Movies.COLUMN_TAHUN + " TEXT NOT NULL, " +
                MoviesDb.Movies.COLUMN_DESKRIPSI + " TEXT NOT NULL, " +
                MoviesDb.Movies.COLUMN_GENRE + " TEXT NOT NULL, " +
                MoviesDb.Movies.COLUMN_RATING + " TEXT NOT NULL, " +
                MoviesDb.Movies.COLUMN_PATH + " BLOB NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MoviesDb.Movies.TABLE_NAME);
        onCreate(db);

    }
}
