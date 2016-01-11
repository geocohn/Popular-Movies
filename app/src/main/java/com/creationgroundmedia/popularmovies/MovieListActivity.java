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
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.view.MenuItem;

import android.widget.Spinner;

import com.creationgroundmedia.popularmovies.moviedb.MoviesContract;
import com.creationgroundmedia.popularmovies.sync.MovieSyncAdapter;
import com.squareup.picasso.Picasso;


/**
 * An activity representing a list of movies as a grid of poster frames. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 *
 * From the menu bar, the user can show all popular movies, or only the ones
 * user-selected as favorites. The user can also sort them by popularity,
 * by rating, alphabetically, or by release date. The user can also choose
 * how many items get downloaded from the Internet (via TheMovieDb API).
 * There is some churn, as they can change daily, and if the user wants one
 * to stick, it must be marked favorite.
 *
 * The movie info is saved using sqlite via a content provider, and kept in
 * sync with the Internet via a sync adapter.
 */
public class MovieListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    final static private String LOG_TAG = MovieListActivity.class.getSimpleName();

    private static final String SELECTED_POSITION = "selectedPosition";
    private static final String SELECT_FAVORITES = "SelectFavorites";
    private static final String FAVORITES_ONLY = "FavoritesOnly";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private RecyclerView recyclerView;
    private Loader<Cursor> movieCursorLoader;
    private static final int URL_LOADER = 0;

    private static final String[] PROJECTION = new String[] {
            MoviesContract.MovieEntry.COLUMN_ID_KEY,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH
    };
    // The following must agree with the PROJECTION above
    private static final int ID_KEY = 0;
    private static final int POSTER_PATH = 1;

    // The following must correspond with the sorting_modes string array resource.
    private static final String SORT_MOST_POPULAR = MoviesContract.MovieEntry.COLUMN_POPULARITY + " DESC";
    private static final String SORT_HIGHEST_RATED = MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
    private static final String SORT_ALPHABETICAL = MoviesContract.MovieEntry.COLUMN_SORTTITLE + " ASC";
    private static final String SORT_NEWEST = MoviesContract.MovieEntry.COLUMN_RELEASE_DATE + " DESC";
    private static final String[] sortOrders = {
            SORT_MOST_POPULAR,
            SORT_HIGHEST_RATED,
            SORT_ALPHABETICAL,
            SORT_NEWEST
            };
    private String sortOrder = SORT_MOST_POPULAR;

    private String mSelectFavorites = null;
    private boolean mFavoritesOnly = false;
    private int mSelectedPosition;
    private Context mContext;

    // had to make this a member variable instead of local in onCreate(), otherwise it vanishes
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        mContext = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (savedInstanceState == null) {
            Log.d(LOG_TAG, "about to call updateProviderFromInternet");
            updateProviderFromInternet(this);
        } else {
            mSelectFavorites = savedInstanceState.getString(SELECT_FAVORITES);
            mFavoritesOnly = savedInstanceState.getBoolean(FAVORITES_ONLY);
            mSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION);
        }

        movieCursorLoader = getSupportLoaderManager().initLoader(URL_LOADER, null, this);

        recyclerView = (RecyclerView) findViewById(R.id.movie_list);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);

        /**
         * listener on number of movies to load:
         */
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.contentEquals(mContext.getString(R.string.movie_list_size_name))) {
                    Log.d(LOG_TAG, "about to call updateProviderFromInternet due to preference change");
                    updateProviderFromInternet(mContext);
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(mPrefListener);


        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        /**
         * if we're in dual pane mode we need the share fab to show from the list activity
         * otherwise we need to hide it and let the detail activity take care of it
         */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (mTwoPane) {
            if (fab != null) {
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Snackbar.make(v, "Please select a movie first", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
            }
        } else {
            fab.setVisibility(View.GONE);
        }

        MovieSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        inflater.inflate(R.menu.menu_fragment, menu);
/**
 * use a spinner to select sort order, that way the user can always see what the order is
 */
        MenuItem item = menu.findItem(R.id.action_sorting_spinner);
        Spinner sortingSpinner = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.sorting_modes, R.layout.spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        sortingSpinner.setAdapter(spinnerAdapter);
        sortingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortOrder = sortOrders[position];
                movieCursorLoader = getSupportLoaderManager().restartLoader(URL_LOADER, null, MovieListActivity.this);
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        item = menu.findItem(R.id.action_faves_only);
        Button faveButton = (Button) MenuItemCompat.getActionView(item);
        faveButton.setSelected(mFavoritesOnly);
        faveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFavoritesOnly = !v.isSelected();
                v.setSelected(mFavoritesOnly);
                mSelectFavorites = mFavoritesOnly ? MoviesContract.MovieEntry.COLUMN_FAVORITE + " = 1" : null;
                movieCursorLoader = getSupportLoaderManager().restartLoader(URL_LOADER, null, MovieListActivity.this);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(SELECT_FAVORITES, mSelectFavorites);
        outState.putBoolean(FAVORITES_ONLY, mFavoritesOnly);
        outState.putInt(SELECTED_POSITION, mSelectedPosition);
        super.onSaveInstanceState(outState);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new AutofitGridLayoutManager(this, 400));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new SimpleImageCursorRecyclerViewAdapter(this, null));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                return new CursorLoader(
                        this,                                   // context
                        MoviesContract.MovieEntry.CONTENT_URI,  // Table to query
                        PROJECTION,                             // Projection to return
                        mSelectFavorites,                       // Favorites only per user's choice
                        null,                                   // No selection arguments
                        sortOrder                               // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        SimpleImageCursorRecyclerViewAdapter adapter = (SimpleImageCursorRecyclerViewAdapter) recyclerView.getAdapter();
        adapter.changeCursor(data);
        recyclerView.smoothScrollToPosition(mSelectedPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((SimpleImageCursorRecyclerViewAdapter)(recyclerView.getAdapter())).changeCursor(null);
    }

    private void updateProviderFromInternet(Context context) {
        Log.d(LOG_TAG, "updateProviderFromInternet (" + context.toString() + ")");
        MovieSyncAdapter.syncImmediately(context);
    }

    public class AutofitGridLayoutManager extends GridLayoutManager {
        /**
         * the number of columns in the grid depends on how much width we've got to play with
         */
        private int itemWidth;

        public AutofitGridLayoutManager(Context context, int itemWidth) {
            super (context, 1);
            this.itemWidth = itemWidth;
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            int spanCount = getWidth() / itemWidth;
            if (spanCount < 1) {
                spanCount = 1;
            }
            setSpanCount(spanCount);
            super.onLayoutChildren(recycler, state);
        }
    }

    public class SimpleImageCursorRecyclerViewAdapter
            extends CursorRecyclerViewAdapter<SimpleImageCursorRecyclerViewAdapter.ViewHolder> {

        private Context mContext;
        private ViewHolder mSelectedHolder = null;

        public SimpleImageCursorRecyclerViewAdapter(Context context, Cursor movieCursor) {
            super(context, movieCursor);
            mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return super.getItemCount();
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
            String url = getString(R.string.tmdbposterpath) + cursor.getString(POSTER_PATH);
            Picasso.with(mContext).load(url).into(viewHolder.imageView);
            final long id = cursor.getLong(ID_KEY);

            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectedHolder != null) {
                        mSelectedHolder.imageView.setSelected(false);
                    }
                    mSelectedHolder = viewHolder;
                    viewHolder.imageView.setSelected(true);
                    mSelectedPosition = viewHolder.getLayoutPosition();
                    if (mTwoPane) {
                        Bundle bundle = new Bundle();
                        bundle.putLong(MoviesContract.MovieEntry.COLUMN_ID_KEY, id);
                        MovieDetailFragment fragment = new MovieDetailFragment();
                        fragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.movie_detail_container, fragment)
                                .commit();
                    } else {
                        Intent detailIntent = new Intent(v.getContext(), MovieDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putLong(MoviesContract.MovieEntry.COLUMN_ID_KEY, id);
                        detailIntent.putExtras(bundle);
                        startActivity(detailIntent);
                    }
                }
            });

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            View mView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                imageView = (ImageView) view.findViewById(R.id.posterView);
                imageView.setLayoutParams(new FrameLayout.LayoutParams(400, 625));
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setAdjustViewBounds(true);
                imageView.setPadding(8, 8, 8, 8);
            }
        }
    }
}
