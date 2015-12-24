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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
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
        mContext = context;
        updateMovies(context);
    }

    private Context mContext;

    private void updateMovies(Context context) {
        FetchMovieTask movieTask = new FetchMovieTask(context);
        movieTask.execute();
        try {
            movieTask.get();
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

