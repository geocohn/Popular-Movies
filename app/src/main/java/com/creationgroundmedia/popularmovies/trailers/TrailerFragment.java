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

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.creationgroundmedia.popularmovies.R;
import com.creationgroundmedia.popularmovies.moviedb.MoviesContract;

import java.util.List;

/**
 * A fragment representing a list of Trailers.
 */
public class TrailerFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<TrailerItem>> {
    final static private String LOG_TAG = TrailerFragment.class.getSimpleName();

    private static final int URL_TRAILERLOADER = 1;

    private long mMovieId;
    private View mView;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrailerFragment() {
    }

    @SuppressWarnings("unused")
    public static TrailerFragment newInstance(long movieId) {
        TrailerFragment fragment = new TrailerFragment();
        Bundle args = new Bundle();
        args.putLong(MoviesContract.MovieEntry.COLUMN_ID_KEY, movieId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mMovieId = getArguments().getLong(MoviesContract.MovieEntry.COLUMN_ID_KEY);
        }

        getLoaderManager().initLoader(URL_TRAILERLOADER, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_trailer_list, container, false);

        return mView;
    }


    @Override
    public Loader<List<TrailerItem>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_TRAILERLOADER:
                return new TrailerLoader(getContext(), mMovieId);
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<TrailerItem>> loader, final List<TrailerItem> data) {
        if (data != null) {
            FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (data.size() > 0) {
                        TrailerItem trailer = data.get(0);
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/*");
                        intent.putExtra(Intent.EXTRA_SUBJECT, trailer.getName());
                        intent.putExtra(Intent.EXTRA_TEXT,
                                "https://www.youtube.com/watch?v="
                                        + trailer.getYoutubeKey()
                                        + "\n\n#PopularMoviesApp");
                        ShareActionProvider shareActionProvider = new ShareActionProvider(getContext());
                        shareActionProvider.setShareIntent(intent);
                        startActivity(intent);
                    } else {
                        Snackbar.make(view, "No trailers to share for this movie", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
            });
            for (TrailerItem trailer : data) {
                View tv = LayoutInflater.from(getContext()).inflate(R.layout.fragment_trailer, null);
                ((LinearLayout)mView).addView(tv);
                TextView nameView = (TextView) tv.findViewById(R.id.content);
                nameView.setText(trailer.getName());
                /**
                 * the YouTube key is tucked into a hidden (GONE) TextView so that it can be used
                 * to build a URL if the user clicks the enrty
                 * */
                TextView keyView = (TextView) tv.findViewById(R.id.youtube_key) ;
                keyView.setText(trailer.getYoutubeKey());
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchTrailerViewer(((TextView)v.findViewById(R.id.youtube_key)).getText());
                    }
                });
            }
            getLoaderManager().destroyLoader(URL_TRAILERLOADER);
        }
    }

    private void launchTrailerViewer(CharSequence youtubeKey) {
//        Log.d(LOG_TAG, "launching YouTube viewer for key " + youtubeKey);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + youtubeKey)));
    }

    @Override
    public void onLoaderReset(Loader<List<TrailerItem>> loader) {

    }
}
