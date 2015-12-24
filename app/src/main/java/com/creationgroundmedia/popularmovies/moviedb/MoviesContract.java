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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by geo on 12/18/15.
 */
public class MoviesContract {
    public static final String CONTENT_AUTHORITY = "com.creationgroundmedia.popularmovies.moviedb";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    /* Inner class that defines the table contents of the weather table */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        // R rated or not, stored as boolean (integer 0 or 1)
        public static final String COLUMN_ADULT = "adult";
        // URL to backdrop artwork image, stored as a string
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        // Unique key from TMDB, stored as long
        public static final String COLUMN_ID_KEY = "_id";
        // Original language of the movie, stored as a string
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        // Brief synopsis of the movie, stored as a string
        public static final String COLUMN_OVERVIEW = "overview";
        // The release date of the movie, stored as a string YYYY-MM-DD
        public static final String COLUMN_RELEASE_DATE = "release_date";
        // URL to poster artwork image, stored as a string
        public static final String COLUMN_POSTER_PATH = "poster_path";
        // Popularity of the movie, real stored as a string, with range 0.0 - 100.0
        public static final String COLUMN_POPULARITY = "popularity";
        // Movie title, stored as a string
        public static final String COLUMN_TITLE = "title";
        // Whether or not trailers or other related videos exist, stored as a boolean (integer 0 or 1)
        public static final String COLUMN_VIDEO = "video";
        // Average vote, real stored as a string, with range 0.0 - 10.0
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        // Total number of votes, stored as a long
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        // Same as title, stored as a string, but with "The" trimmed off the beginning if it was there
        public static final String COLUMN_SORTTITLE = "sortTitle";
        // Whether or not the movie should persist between refreshes, stored as a boolean (integer 0 or 1)
        public static final String COLUMN_KEEPER = "keeper";


        public static Uri buildMoviesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
