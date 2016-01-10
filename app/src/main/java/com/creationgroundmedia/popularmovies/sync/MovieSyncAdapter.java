/*
 *
 *  * Copyright (C) 2015 George Cohn III
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use mContext file except in compliance with the License.
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

package com.creationgroundmedia.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.creationgroundmedia.popularmovies.R;
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
import java.util.Vector;

/**
 * This does the actual work of loading a list of popular movies from themoviedb and getting
 * them into the local database managed by the content provider.
 *
 * The idea is to load a user specified number of the top most popular movies,
 * adding any that are new, and finally, getting rid of any that aren't on the list
 * and aren't marked as favorites.
 */
public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    final static private String LOG_TAG = MovieSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the tmdb
    public static final int SYNC_INTERVAL = 6 * 60 * 60; // 6 hours
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    ContentResolver mContentResolver;
    Context mContext;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        mContext = context;
    }

    public MovieSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        mContext = context;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        Log.d(LOG_TAG, "onAccountCreated called");
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }




    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
//        Log.d(LOG_TAG, "onPerformSync called");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        int maxPages = Integer.valueOf(sharedPref.getString(mContext.getString(R.string.movie_list_size_name), "1"));
        for (int i = 0; i < maxPages; i++) {
            try {
                getMovieDataFromJson(getTmdbPage(i + 1));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error parsing JSON", e);
            }
        }
        int deleted = deleteOldEntriesFromDb();
    }

    private int deleteOldEntriesFromDb() {
        final String NOT_FAVE_SELECTION = MoviesContract.MovieEntry.COLUMN_FAVORITE + " != 1";
        final String NOT_FRESH_AND_NOT_FAVE_SELECTION = MoviesContract.MovieEntry.COLUMN_FRESH +
                " != 1 AND " +
                NOT_FAVE_SELECTION;
        int rows;
        // Get rid of anything not FRESH and not FAVORITE
        rows = mContext.getContentResolver().delete(MoviesContract.MovieEntry.CONTENT_URI, NOT_FRESH_AND_NOT_FAVE_SELECTION, null);
//        Log.d(LOG_TAG, "deleted " + rows + " \'" + NOT_FRESH_AND_NOT_FAVE_SELECTION + "\'");
        ContentValues values = new ContentValues();

        // Set everything to no longer FRESH
        values.put(MoviesContract.MovieEntry.COLUMN_FRESH, "0");
        rows = mContext.getContentResolver().update(MoviesContract.MovieEntry.CONTENT_URI, values, NOT_FAVE_SELECTION, null);
//        Log.d(LOG_TAG, "updated \'" + NOT_FAVE_SELECTION + "\' " + rows + " to NOT_FRESH");
        return rows;
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
                stringBuffer.append(line).append("\n");
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
            if (reader != null) {
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
            movieValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITE, 0);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_FRESH, 1);
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

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }
}
