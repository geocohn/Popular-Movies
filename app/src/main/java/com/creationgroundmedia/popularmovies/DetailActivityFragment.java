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
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Show movie details via the parcel sent from the main activity.
 */
public class DetailActivityFragment extends Fragment {

    public static final double MAX_VOTE_AVERAGE = 10.0;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        MovieFields movieFields = getActivity().getIntent().getParcelableExtra(MovieFields.class.getSimpleName());
/**
 * todo: make the detail layout not suck
 */
        Context context = getContext();

        ImageView posterView = (ImageView) view.findViewById(R.id.posterView);
        Picasso.with(context).load(movieFields.getPoster_path()).into(posterView);

        TextView titleView = (TextView) view.findViewById(R.id.titleView);
        titleView.setText(movieFields.getTitle());

        TextView dateView = (TextView) view.findViewById(R.id.dateView);

        SimpleDateFormat inFormat = new SimpleDateFormat(context.getString(R.string.jsondateformat));
        Date date = null;
        try {
            date = inFormat.parse(movieFields.getRelease_date());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        dateView.setText(new SimpleDateFormat("yyyy").format(date));

        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        ratingBar.setIsIndicator(true);
        int numStars = ratingBar.getNumStars();
        float range = numStars / ratingBar.getStepSize();
        double vote_average = movieFields.getVote_average();
        ratingBar.setRating((float) ((vote_average * range) / (MAX_VOTE_AVERAGE * numStars)));

        TextView votesView = (TextView) view.findViewById(R.id.votesView);
        votesView.setText(Double.toString(vote_average) + "/" + Integer.toString((int) MAX_VOTE_AVERAGE));

        TextView overviewView = (TextView) view.findViewById(R.id.overviewView);
        overviewView.setText(movieFields.getOverview());


        return view;

    }
}
