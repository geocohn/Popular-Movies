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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * This content provider is a little unusual in the way insert() and bulkInsert() work.
 * The idea is to mark new data as FRESH, so that the client can identify FAVORITES and
 * newly loaded data. That allows purging of old data that isn't marked FAVORITE.
 * The client is expected to load new data, delete any rows that are neither FRESH nor FAVORITE,
 * and then zero the FRESH columns.
 */

public class MoviesProvider extends ContentProvider {
    final static private String LOG_TAG = MoviesProvider.class.getSimpleName();
    public static final String ID_SELECTION = MoviesContract.MovieEntry.COLUMN_ID_KEY + " = ?";

    public MoviesProvider() {
    }
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    public static final int MOVIES = 100;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);

        return matcher;
    }



    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(
                        MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
             default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case MOVIES:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        final ContentValues cvFresh = new ContentValues();
        cvFresh.put(MoviesContract.MovieEntry.COLUMN_FRESH, 1);

        switch (match) {
            case MOVIES: {
                long id = 0;
                // update an existing row as long as it's not a favorite
                // This insures the FRESH field gets reinitialized
                String[] selectionThisKey = {values.getAsString(MoviesContract.MovieEntry.COLUMN_ID_KEY)};
                // update the FRESH column of an existing row if its id matches
                int rows = db.update(MoviesContract.MovieEntry.TABLE_NAME,
                        cvFresh,
                        ID_SELECTION,
                        selectionThisKey);
                if (rows <= 0) {
                    // if it doesn't exist, go ahead and insert it
                    id = db.insert(MoviesContract.MovieEntry.TABLE_NAME,
                            null,
                            values);
                }
                if ( id >= 0 )
                    returnUri = MoviesContract.MovieEntry.buildMoviesUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        final ContentValues cvFresh = new ContentValues();
        cvFresh.put(MoviesContract.MovieEntry.COLUMN_FRESH, 1);
        switch (match) {
            case MOVIES:
                int updates = 0;
                long id = 0;
                int inserts = 0;
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        String[] selectionThisKey = {value.getAsString(MoviesContract.MovieEntry.COLUMN_ID_KEY)};
                        // update the FRESH column of an existing row if its id matches
                        int rows = db.update(MoviesContract.MovieEntry.TABLE_NAME,
                                cvFresh,
                                ID_SELECTION,
                                selectionThisKey);
                        if (rows > 0) {
                            updates++;
                        } else {
                            // if it doesn't exist, go ahead and insert it
                            id = db.insert(MoviesContract.MovieEntry.TABLE_NAME,
                                    null,
                                    value);
                            if (id != -1) {
                                inserts++;
                            }                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                int nRows = updates + inserts;
                Log.d(LOG_TAG, "bulk inserted "
                        + nRows
                        + " rows ("
                        + updates
                        +" updates and "
                        + inserts
                        + " inserts)");
                return nRows;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
             case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);
            int rowsUpdated;

            switch (match) {
                case MOVIES:
                    rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
    }
}
