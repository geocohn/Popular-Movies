/*
 *
 *  * Copyright (C) 2015 George Cohn III
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.creationgroundmedia.popularmovies.moviedb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by geo on 12/18/15.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;


    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesContract.MovieEntry.TABLE_NAME + " (" +
                MoviesContract.MovieEntry.COLUMN_ADULT + " INTEGER NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH + " STRING NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_ID_KEY + " INTEGER PRIMARY KEY," +
                MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " STRING NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_OVERVIEW + " STRING NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_RELEASE_DATE + " STRING NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_POSTER_PATH + " STRING NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_POPULARITY + " STRING NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_TITLE + " STRING NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_VIDEO + " INTEGER NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " STRING NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_VOTE_COUNT + " INTEGER NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_SORTTITLE + " STRING NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_KEEPER + " INTEGER NOT NULL" +
                " );";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on a real version change, do a real upgrade instead of hosing the user's data
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MovieEntry.TABLE_NAME);
        onCreate(db);

    }
}
