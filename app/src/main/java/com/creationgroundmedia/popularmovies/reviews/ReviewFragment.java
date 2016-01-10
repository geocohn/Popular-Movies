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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.creationgroundmedia.popularmovies.R;
import com.creationgroundmedia.popularmovies.moviedb.MoviesContract;

import java.util.List;

/**
 * A fragment representing a list of reviews.
 */
public class ReviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<ReviewItem>> {
    final static private String LOG_TAG = ReviewFragment.class.getSimpleName();

    private static final int URL_REVIEWLOADER = 2;
    private static final int PREVIEW_LINES = 2;

    private long mMovieId;
    private View mView;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReviewFragment() {
    }

    @SuppressWarnings("unused")
    public static ReviewFragment newInstance(long movieId) {
        ReviewFragment fragment = new ReviewFragment();
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

        getLoaderManager().initLoader(URL_REVIEWLOADER, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_review_list, container, false);

        return mView;
    }


    @Override
    public Loader<List<ReviewItem>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_REVIEWLOADER:
                return new ReviewLoader(getContext(), mMovieId);
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<ReviewItem>> loader, final List<ReviewItem> data) {
        if (data != null) {
            for (ReviewItem review : data) {
                View tv = LayoutInflater.from(getContext()).inflate(R.layout.fragment_review, null);
                ((LinearLayout)mView).addView(tv);
                TextView authorView = (TextView) tv.findViewById(R.id.author);
                authorView.setText(String.format("Review by %s", review.getAuthor()));
                final TextView contentView = (TextView) tv.findViewById(R.id.content);
                contentView.setMaxLines(PREVIEW_LINES);
                contentView.setEllipsize(TextUtils.TruncateAt.END);
                contentView.setSelected(false);
                contentView.setText(review.getContent());
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (contentView.isSelected()) {
                            contentView.setEllipsize(TextUtils.TruncateAt.END);
                            contentView.setMaxLines(PREVIEW_LINES);
                            contentView.setSelected(false);
                        } else {
                            contentView.setEllipsize(null);
                            contentView.setMaxLines(Integer.MAX_VALUE);
                            contentView.setSelected(true);
                        }
                    }
                });
            }
            getLoaderManager().destroyLoader(URL_REVIEWLOADER);
        }
    }

    private void launchReviewViewer(CharSequence youtubeKey) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse((String) youtubeKey)));
    }

    @Override
    public void onLoaderReset(Loader<List<ReviewItem>> loader) {

    }
}
