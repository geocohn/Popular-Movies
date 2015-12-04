/*
 * Copyright (C) 2015 George Cohn III
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.creationgroundmedia.popularmovies;

import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;


/**
 * The main view: a grid of movie poster frame images
 */
public class MainActivityFragment extends Fragment {

    MovieData movieData;
    ImageAdapter movieAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        movieData = new MovieData(getContext());

        GridView gridView = (GridView) view.findViewById(R.id.gridview);
        gridView.setAdapter(movieAdapter = new ImageAdapter(movieData, getContext()));

/**
 * when the user clicks a grid item, parcel up the data for that movie and fire off an activity to show a detail screen
 *
 */
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(getContext(), DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(MovieFields.class.getSimpleName(), movieData.getMovieList().get(position));
                detailIntent.putExtras(bundle);
                startActivity(detailIntent);
            }
        });

       return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_fragment, menu);

/**
 * use a spinner to select sort order, that way the user can always see what the order is
 */

        MenuItem item = menu.findItem(R.id.action_sorting_spinner);
        Spinner sortingSpinner = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sorting_modes, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        sortingSpinner.setAdapter(spinnerAdapter);
        sortingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                movieData.SortMovieList(position);
                movieAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
//                Toast.makeText(getContext(), "spinner nothing selected", Toast.LENGTH_SHORT).show();
            }
        });

        return;
    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    }
}






















