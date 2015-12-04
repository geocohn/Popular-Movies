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

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

/**
 * Created by geo on 11/15/15.
 *
 * in-memory tmdb movie database
 */
public class MovieData {
    public MovieData(Context context) {
        updateMovies(context, this);
    }

    private ArrayList<MovieFields> movieList;

    public void SortMovieList(int position) {
        if (!movieList.isEmpty()) {
            switch (position) {
                case 0:
                    Collections.sort(movieList, new PopularityDescComparator());
                    break;
                case 1:
                    Collections.sort(movieList, new VoteAverageDescComparator());
                    break;
                case 2:
                    Collections.sort(movieList, new TitleComparator());
                    break;
                case 3:
                    Collections.sort(movieList, new DateDescComparator());
                    break;
                default:
                    break;
            }
        }
    }

    public ArrayList<MovieFields> getMovieList() {
        return movieList;
    }

    public int getCount() {
        return movieList.size();
    }

    public void setMovieList(ArrayList<MovieFields> movieList) {
        this.movieList = movieList;
    }

    private void updateMovies(Context context, MovieData movieData) {
        FetchMovieTask movieTask = new FetchMovieTask(context);
        movieTask.execute();
        try {
            movieData.setMovieList((ArrayList<MovieFields>) movieTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}

class TitleComparator implements Comparator<MovieFields> {
    @Override
    public int compare(MovieFields lhs, MovieFields rhs) {
        return lhs.getSortTitle().compareTo(rhs.getSortTitle());
    }
}

class DateDescComparator implements Comparator<MovieFields> {
    @Override
    public int compare(MovieFields lhs, MovieFields rhs) {
        return rhs.getRelease_date().compareTo(lhs.getRelease_date());
    }
}

class PopularityDescComparator implements Comparator<MovieFields> {
    @Override
    public int compare(MovieFields lhs, MovieFields rhs) {
        return (int) (rhs.getPopularity() - lhs.getPopularity());
    }
}

class VoteAverageDescComparator implements Comparator<MovieFields> {
    @Override
    public int compare(MovieFields lhs, MovieFields rhs) {
        return (int) (rhs.getVote_average() - lhs.getVote_average());
    }
}

/**
 * record for a single tmdb movie
 */
class MovieFields implements Parcelable {
    private boolean adult;
    private String backdrop_path;
    private long id;
    private String original_language;
    private String overview;
    private String release_date;
    private String poster_path;
    private double popularity;
    private String title;
    private boolean video;
    private double vote_average;
    private long vote_count;
    private String sortTitle;

    public MovieFields() {
    }

    public MovieFields(boolean adult,
                       String backdrop_path,
                       long id,
                       String original_language,
                       String overview,
                       String release_date,
                       String poster_path,
                       double popularity,
                       String title,
                       boolean video,
                       double vote_average,
                       long vote_count,
                       String sortTitle) {
        setAdult(adult);
        setBackdrop_path(backdrop_path);
        setId(id);
        setOriginal_language(original_language);
        setOverview(overview);
        setRelease_date(release_date);
        setPoster_path(poster_path);
        setPopularity(popularity);
        setTitle(title);
        setVideo(video);
        setVote_average(vote_average);
        setVote_count(vote_count);
        setSortTitle(sortTitle);
    }

    public MovieFields(Parcel source) {
        setAdult(source.readInt() != 0);
        setBackdrop_path(source.readString());
        setId(source.readLong());
        setOriginal_language(source.readString());
        setOverview(source.readString());
        setRelease_date(source.readString());
        setPoster_path(source.readString());
        setPopularity(source.readDouble());
        setTitle(source.readString());
        setVideo(source.readInt() != 0);
        setVote_average(source.readDouble());
        setVote_count(source.readLong());
        setSortTitle(source.readString());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public static final Parcelable.Creator<MovieFields> CREATOR
            = new Parcelable.Creator<MovieFields>() {
        public MovieFields createFromParcel(Parcel in) {
            return new MovieFields(in);
        }

        public MovieFields[] newArray(int size) {
            return new MovieFields[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(isAdult() ? 1 : 0);
        dest.writeString(getBackdrop_path());
        dest.writeLong(getId());
        dest.writeString(getOriginal_language());
        dest.writeString(getOverview());
        dest.writeString(getRelease_date());
        dest.writeString(getPoster_path());
        dest.writeDouble(getPopularity());
        dest.writeString(getTitle());
        dest.writeInt(isVideo() ? 1 : 0);
        dest.writeDouble(getVote_average());
        dest.writeLong(getVote_count());
        dest.writeString(getSortTitle());
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public String getSortTitle() {
        return sortTitle;
    }

    public void setSortTitle(String sortTitle) {
        this.sortTitle = sortTitle;
    }

    public String getOriginal_language() {
        return original_language;
    }

    public void setOriginal_language(String original_language) {
        this.original_language = original_language;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public double getVote_average() {
        return vote_average;
    }

    public void setVote_average(double vote_average) {
        this.vote_average = vote_average;
    }

    public long getVote_count() {
        return vote_count;
    }

    public void setVote_count(long vote_count) {
        this.vote_count = vote_count;
    }
}

/**
 * uses the tmdb API to load the most popular current movies
 */
class FetchMovieTask extends AsyncTask {

    final private String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private Context mContext;
    private ArrayList<MovieFields> list = new ArrayList<MovieFields>();

    public FetchMovieTask(Context context) {
        mContext = context;
    }

    @Override
    protected ArrayList<MovieFields> doInBackground(Object[] params) {
        /**
         * todo: give user progress feedback when loading is slow
         */
        final int MAX_PAGES = 1;
        for (int i = 0; i < MAX_PAGES; i++) {
            try {
                getMovieDataFromJson(getTmdbPage(i + 1));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error parsing JSON", e);
            }
        }
        return list;
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
        for (int i = 0; i < movieList.length(); i++) {
            JSONObject titleJSON = movieList.getJSONObject(i);
            String title = titleJSON.getString(mContext.getString(R.string.jsontitle));
            MovieFields movie = new MovieFields(
                    titleJSON.getBoolean(mContext.getString(R.string.jsonadult)),
                    mContext.getString(R.string.tmdbimagepath) + titleJSON.getString(mContext.getString(R.string.jsonbackdrop)),
                    titleJSON.getLong(mContext.getString(R.string.jsonid)),
                    titleJSON.getString(mContext.getString(R.string.jsonoriginallanguage)),
                    titleJSON.getString(mContext.getString(R.string.jsonoverview)),
                    titleJSON.getString(mContext.getString(R.string.jsondate)),
                    mContext.getString(R.string.tmdbimagepath) + titleJSON.getString(mContext.getString(R.string.jsonposter)),
                    titleJSON.getDouble(mContext.getString(R.string.jsonpopularity)),
                    titleJSON.getString(mContext.getString(R.string.jsontitle)),
                    titleJSON.getBoolean(mContext.getString(R.string.jsonvideo)),
                    titleJSON.getDouble(mContext.getString(R.string.jsonvoteaverage)),
                    titleJSON.getLong(mContext.getString(R.string.jsonvotecount)),
                    trimLeadingThe(title));
            list.add(movie);
        }
        return;
    }

    private String trimLeadingThe(String title) {
        if (title.startsWith("The ")) {
            return title.substring(4);
        } else {
            return title;
        }
    }
}