package com.creationgroundmedia.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.creationgroundmedia.popularmovies.moviedb.MoviesContract;
import com.creationgroundmedia.popularmovies.reviews.ReviewFragment;
import com.creationgroundmedia.popularmovies.trailers.TrailerFragment;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A fragment representing a single movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 *
 * This one uses the database to display movie details, and launches two additional fragments:
 * 1) a trailer fragment to display a variable list of buttons to launch trailers, and
 * 2) a reviews fragment to display a variable list of reviews
 *
 */
public class MovieDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    final static private String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    public static final double MAX_VOTE_AVERAGE = 10.0;

    private static final int URL_LOADER = 0;
    private static final String[] PROJECTION = new String[] {
            MoviesContract.MovieEntry.COLUMN_ADULT,
            MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MoviesContract.MovieEntry.COLUMN_ID_KEY,
            MoviesContract.MovieEntry.COLUMN_FAVORITE,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE,
            MoviesContract.MovieEntry.COLUMN_POPULARITY,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_VIDEO,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_VOTE_COUNT
    };

    /**
     *  the numbering below has to correspond to the order in the PROJECTION above
     */
    private static final int ADULT = 0;
    private static final int BACKDROP_PATH = 1;
    private static final int ID_KEY = 2;
    private static final int FAVORITE = 3;
    private static final int OVERVIEW = 4;
    private static final int ORIGINAL_LANGUAGE = 5;
    private static final int POPULARITY = 6;
    private static final int POSTER_PATH = 7;
    private static final int RELEASE_DATE = 8;
    private static final int TITLE = 9;
    private static final int VIDEO = 10;
    private static final int VOTE_AVERAGE = 11;
    private static final int VOTE_COUNT = 12;

    private static final String WHERELAUSE = MoviesContract.MovieEntry.COLUMN_ID_KEY + " = ?";
    private static long movieId;

    @Bind(R.id.backdropView) ImageView backdropView;
    @Bind(R.id.posterView) ImageView posterView;
    @Bind(R.id.titleView) TextView titleView;
    @Bind(R.id.dateView) TextView dateView;
    @Bind(R.id.ratingBar) RatingBar ratingBar;
    @Bind(R.id.favorite) Button favorite;
    @Bind(R.id.votesView) TextView votesView;
    @Bind(R.id.overviewView) TextView overviewView;


    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        movieId = getArguments().getLong(MoviesContract.MovieEntry.COLUMN_ID_KEY);

        getLoaderManager().initLoader(URL_LOADER, null, this);

        if (savedInstanceState == null) {
            TrailerFragment trailerFragment = TrailerFragment.newInstance(movieId);
            getFragmentManager().beginTransaction()
                    .add(R.id.trailer_list, trailerFragment)
                    .commit();

            ReviewFragment reviewFragment = ReviewFragment.newInstance(movieId);
            getFragmentManager().beginTransaction()
                    .add(R.id.review_list, reviewFragment)
                    .commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);

        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                String[] whereArgs = {Long.toString(movieId)};
                return new CursorLoader(
                        getContext(),                           // context
                        MoviesContract.MovieEntry.CONTENT_URI,  // Table to query
                        PROJECTION,                             // Projection to return
                        WHERELAUSE,                             // get the row with the specific id
                        whereArgs,                              // specify the id
                        null                                    // sort order irrelevant for single row
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        if (data == null || data.getCount() <= 0) {
            return;
        }

        data.moveToFirst();

        Context context = getContext();

        String title = data.getString(TITLE);

        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) this.getActivity().findViewById(R.id.toolbar_layout);

        if (appBarLayout != null) {
            appBarLayout.setTitle(title);
        }

        String url = context.getString(R.string.tmdbbackdroppath) + data.getString(BACKDROP_PATH);
        Picasso.with(context).load(url).into(backdropView);

        url = context.getString(R.string.tmdbposterpath) + data.getString(POSTER_PATH);
        Picasso.with(context).load(url).into(posterView);

        titleView.setText(title);

        SimpleDateFormat inFormat = new SimpleDateFormat(context.getString(R.string.jsondateformat));
        Date date = null;
        try {
            date = inFormat.parse(data.getString(RELEASE_DATE));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        dateView.setText(new SimpleDateFormat("yyyy").format(date));

        ratingBar.setIsIndicator(true);
        int numStars = ratingBar.getNumStars();
        float range = numStars / ratingBar.getStepSize();
        double vote_average = Double.parseDouble(data.getString(VOTE_AVERAGE));
        ratingBar.setRating((float) ((vote_average * range) / (MAX_VOTE_AVERAGE * numStars)));

        favorite.setSelected(data.getInt(FAVORITE) != 0);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFavorite = !v.isSelected();
                v.setSelected(isFavorite);
                setFavorite(data.getLong(ID_KEY), isFavorite);
            }
            private void setFavorite(long id, boolean favorite) {
                final String SELECTION = MoviesContract.MovieEntry.COLUMN_ID_KEY + " = ?";
                String[] selectArgs = {Long.toString(id)};
                ContentValues values = new ContentValues();
                values.put(MoviesContract.MovieEntry.COLUMN_FAVORITE, favorite? 1 : 0);
                getContext().getContentResolver().update(MoviesContract.MovieEntry.CONTENT_URI, values, SELECTION, selectArgs);
            }
       });

        votesView.setText(String.format("%s/%s", Double.toString(vote_average), Integer.toString((int) MAX_VOTE_AVERAGE)));

        overviewView.setText(data.getString(OVERVIEW));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
