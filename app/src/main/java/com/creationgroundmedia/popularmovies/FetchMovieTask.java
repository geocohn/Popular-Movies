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
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.creationgroundmedia.popularmovies.moviedb.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

/**
 * uses the tmdb API to load the most popular current movies
 */
class FetchMovieTask extends AsyncTask {

    final private String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private Context mContext;

    public FetchMovieTask(Context context) {
        mContext = context;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        int deleted = deleteNonKeepersFromDb();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        int maxPages = Integer.valueOf(sharedPref.getString("movie_list_size", "1"));
        for (int i = 0; i < maxPages; i++) {
            try {
                getMovieDataFromJson(getTmdbPage(i + 1));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error parsing JSON", e);
            }
        }
        return null;
    }

    private int deleteNonKeepersFromDb() {
        final String SELECTION = MoviesContract.MovieEntry.COLUMN_KEEPER + " = 0";
        int deleted = mContext.getContentResolver().delete(MoviesContract.MovieEntry.CONTENT_URI, SELECTION, null);
        return deleted;
    }

    /**
 * do a TMDB get for a single page sorted by popularity and return the JSON string
 */
    private String getTmdbPage(int page) {
        // These need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        URL url;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(mContext.getString(R.string.tmdbscheme));
            builder.authority(mContext.getString(R.string.tmdbbaseurl));
            builder.appendPath(mContext.getString(R.string.tmdbapiversion));
            builder.appendPath(mContext.getString(R.string.tmdbdiscover));
            builder.appendPath(mContext.getString(R.string.tmdbmovie));
            builder.appendQueryParameter(mContext.getString(R.string.tmdbsortby), mContext.getString(R.string.tmdbpopularity));
            builder.appendQueryParameter(mContext.getString(R.string.tmdbpage), String.valueOf(page));
            builder.appendQueryParameter(mContext.getString(R.string.tmdbapikey), mContext.getString(R.string.tmdbapikeyvalue));

            url = new URL(builder.build().toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(mContext.getString(R.string.httpget));
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer stringBuffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }
            if (stringBuffer.length() == 0) {
                return null;
            }

            movieJsonStr = stringBuffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader == null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return movieJsonStr;
    }

    private void getMovieDataFromJson(String movieJsonStr) throws JSONException {

        if (movieJsonStr == null) {
            return;
        }

        JSONObject movieJSON = new JSONObject(movieJsonStr);
        JSONArray movieList = movieJSON.getJSONArray(mContext.getString(R.string.jsonresults));

        Vector<ContentValues> cvVector = new Vector<ContentValues>(movieList.length());

        for (int i = 0; i < movieList.length(); i++) {
            JSONObject titleJSON = movieList.getJSONObject(i);

            ContentValues movieValues = new ContentValues();

            String title = titleJSON.getString(mContext.getString(R.string.jsontitle));

            movieValues.put(MoviesContract.MovieEntry.COLUMN_ADULT, titleJSON.getBoolean(mContext.getString(R.string.jsonadult))? 1 : 0);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH, titleJSON.getString(mContext.getString(R.string.jsonbackdrop)));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_KEEPER, 0);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_ID_KEY, titleJSON.getLong(mContext.getString(R.string.jsonid)));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, titleJSON.getString(mContext.getString(R.string.jsonoriginallanguage)));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, titleJSON.getString(mContext.getString(R.string.jsonoverview)));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, titleJSON.getString(mContext.getString(R.string.jsonpopularity)));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, titleJSON.getString(mContext.getString(R.string.jsonposter)));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, titleJSON.getString(mContext.getString(R.string.jsondate)));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_SORTTITLE, trimLeadingThe(title));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, title);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_VIDEO, titleJSON.getBoolean(mContext.getString(R.string.jsonvideo))? 1 : 0);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, titleJSON.getString(mContext.getString(R.string.jsonvoteaverage)));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, titleJSON.getInt(mContext.getString(R.string.jsonvotecount)));

            cvVector.add(movieValues);
        }

        ContentValues[] cvArray = new ContentValues[cvVector.size()];
        cvVector.toArray(cvArray);
        int inserted = mContext.getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, cvArray);
    }

    private String trimLeadingThe(String title) {
        if (title.startsWith("The ")) {
            return title.substring(4);
        } else {
            return title;
        }
    }
}
