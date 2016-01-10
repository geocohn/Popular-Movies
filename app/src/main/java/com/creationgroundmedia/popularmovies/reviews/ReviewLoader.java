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

package com.creationgroundmedia.popularmovies.reviews;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
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
* This manages the asynchronous loading of reviews information about a movie from the themoviedb
*/
public class ReviewLoader extends AsyncTaskLoader<List<ReviewItem>> {
    final static private String LOG_TAG = ReviewLoader.class.getSimpleName();

    private final long mMovieId;
    private final Context mContext;
    private List<ReviewItem> mReviews;

    public ReviewLoader(Context context, long movieId) {
        super(context);
        mContext = context;
        mMovieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        if (mReviews == null) {
//            Log.d(LOG_TAG, "Forcing a load");
            forceLoad();
        }
    }

    @Override
    public List<ReviewItem> loadInBackground() {
        try {
            return getReviewsFromJSON(getTmdbReviewJSON());
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to get trailer information from Internet", e);
        }
        return null;
    }

    private String getTmdbReviewJSON() {
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
            builder.appendPath(mContext.getString(R.string.tmdbreviews));
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

    private List<ReviewItem> getReviewsFromJSON(String reviewsJsonStr) throws JSONException {

        if (reviewsJsonStr == null) {
            return null;
        }

        JSONObject movieJSON = new JSONObject(reviewsJsonStr);
        JSONArray trailersList = movieJSON.getJSONArray(mContext.getString(R.string.jsonresults));
        mReviews = new ArrayList<>();

        for (int i = 0; i < trailersList.length(); i++) {
            JSONObject titleJSON = trailersList.getJSONObject(i);
            ReviewItem t = new ReviewItem(
                    titleJSON.getString(mContext.getString(R.string.jsonreviewid)),
                    titleJSON.getString(mContext.getString(R.string.jsonauthor)),
                    titleJSON.getString(mContext.getString(R.string.jsoncontent)),
                    titleJSON.getString(mContext.getString(R.string.jsonurl))
            );
            mReviews.add(t);
        }
        return mReviews;
    }
}

