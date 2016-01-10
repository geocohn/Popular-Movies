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

package com.creationgroundmedia.popularmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.creationgroundmedia.popularmovies.moviedb.MoviesContract;
import com.creationgroundmedia.popularmovies.moviedb.MoviesDbHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * sqlite tests
 */
public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteTheDb() { mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME); }

    @Override
    public void setUp() throws Exception {
        deleteTheDb();
    }

    public void testCreateDb() throws Exception {
        final String tableName = MoviesContract.MovieEntry.TABLE_NAME;

        mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MoviesDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        boolean hasName = false;
        do {
            hasName = c.getString(0).equals(tableName);
        } while (!hasName & c.moveToNext());

        assertTrue("Error: the database was created without the movies table",
                hasName);

        c = db.rawQuery("PRAGMA table_info(" + MoviesContract.MovieEntry.TABLE_NAME + ")",
                null);
        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_ADULT);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_ID_KEY);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_FAVORITE);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_OVERVIEW);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_POPULARITY);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_POSTER_PATH);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_SORTTITLE);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_TITLE);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_VIDEO);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        locationColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means the database doesn't contain all of the required columns
        assertTrue("Error: The database doesn't contain all of the required movie entry columns",
                locationColumnHashSet.isEmpty());

        db.close();
    }

    public void testMoviesTable() {

        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues movieValues = createMovieValues(87101);

        long rowId = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, movieValues);
        assertTrue("Error: unable to insert row into the database", rowId != -1);

        Cursor c = db.query(MoviesContract.MovieEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        assertTrue("Error: no rows returned from database query", c.moveToFirst());

        validateRecord("Error: read db entry failed to validate", c, movieValues);

        assertFalse("Error: more than one row returned from database query", c.moveToNext());

        c.close();
        dbHelper.close();
    }

    private void validateRecord(String s, Cursor c, ContentValues movieValues) {
        Set<Map.Entry<String, Object>> valueSet = movieValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = c.getColumnIndex(columnName);
            assertFalse(s + ", column '" + columnName + "' not found. ", idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(s + ", value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. ", expectedValue, c.getString(idx));
        }

    }

    static ContentValues createMovieValues(long rowId) {

        ContentValues movieValues = new ContentValues();

        movieValues.put(MoviesContract.MovieEntry.COLUMN_ADULT, 1);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH, "http://image.tmdb.org/t/p/w500/D6e8RJf2qUstnfkTslTXNTUAlT.jpg");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_ID_KEY, rowId);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITE, 1);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_FRESH, 0);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, "English");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, "An apocalyptic story set in the furthest reaches of our planet, in a stark desert landscape where humanity is broken, and most everyone is crazed fighting for the necessities of life. Within this world exist two rebels on the run who just might be able to restore order. There's Max, a man of action and a man of few words, who seeks peace of mind following the loss of his wife and child in the aftermath of the chaos. And Furiosa, a woman of action and a woman who believes her path to survival may be achieved if she can make it across the desert back to her childhood homeland.");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, "n20.600143");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, "http://image.tmdb.org/t/p/w500/D6e8RJf2qUstnfkTslTXNTUAlT.jpg");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-07-10");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_SORTTITLE, "Terminator");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, "The Terminator");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_VIDEO, 1);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, "6.18");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, 1871);

        return movieValues;
    }
}
