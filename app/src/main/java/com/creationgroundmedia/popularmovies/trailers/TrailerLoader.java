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

package com.creationgroundmedia.popularmovies.trailers;

import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.creationgroundmedia.popularmovies.R;

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
import java.util.List;

/**
* This manages the asynchronous loading of trailer information about a movie from the themoviedb
*/
public class TrailerLoader extends AsyncTaskLoader<List<TrailerItem>> {
    final static private String LOG_TAG = TrailerLoader.class.getSimpleName();

    private final long mMovieId;
    private final Context mContext;
    private List<TrailerItem> mTrailers;

    public TrailerLoader(Context context, long movieId) {
        super(context);
        mContext = context;
        mMovieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        if (mTrailers == null) {
//            Log.d(LOG_TAG, "Forcing a load");
            forceLoad();
        }
    }

    @Override
    public List<TrailerItem> loadInBackground() {
        try {
            return getTrailersFromJSON(getTmdbTrailerJSON());
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to get trailer information from Internet", e);
        }
        return null;
    }

    private String getTmdbTrailerJSON() {
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
            builder.appendPath(mContext.getString(R.string.tmdbmovie));
            builder.appendPath(Long.toString(mMovieId));
            builder.appendPath(mContext.getString(R.string.tmdbVideos));
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
//        Log.d(LOG_TAG, "Got trailer info");
        return movieJsonStr;
    }

    private List<TrailerItem> getTrailersFromJSON(String trailersJsonStr) throws JSONException {

        if (trailersJsonStr == null) {
            return null;
        }

        JSONObject movieJSON = new JSONObject(trailersJsonStr);
        JSONArray trailersList = movieJSON.getJSONArray(mContext.getString(R.string.jsonresults));
        mTrailers = new ArrayList<>();

        for (int i = 0; i < trailersList.length(); i++) {
            JSONObject titleJSON = trailersList.getJSONObject(i);
            if (titleJSON.getString(mContext.getString(R.string.jsonsite)).equalsIgnoreCase("YouTube")) {
                TrailerItem t = new TrailerItem(
                        titleJSON.getString(mContext.getString(R.string.jsonname)),
                        titleJSON.getString(mContext.getString(R.string.jsonkey))
                );
                mTrailers.add(t);
            }
        }
        return mTrailers;
    }

}

