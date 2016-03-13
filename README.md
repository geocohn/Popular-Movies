# Popular-Movies

## Movie Database Browser for Android

Presents a grid of popular movie poster frames, and allows the user to view detailed information for each one.

![screen shot](https://github.com/geocohn/Popular-Movies/blob/master/Screenshot_20160313-113554.png?raw=true)

This was developed for Project 2 as part of the Android Developer Nanodegree program at Udacity.

### Prerequisities

This app isn't available via Google Play, but you can build it and sideload it onto any Android device or Android emulator.

### Installing

After cloning the repository, build and install using [this guideline](http://developer.android.com/tools/building/building-cmdline.html)

## Using

Click any movie poster frame to show details about the movie. While in the detail view, you can
- click the heart icon to toggle the movie as a favorite
- click any trailers that are available to view them
- click any reviews that are available to expand them beyond two lines
- click the share icon to share the first trailer to social media, email, SMS, etc.
In the main view you can
- click the heart icon in the upper right of the action bar to toggle the favorites-only view
- select sorting by Most popular (default), Highest Rated, Alphabetical, or Newest
- use Settings to change the number of most popular movies to browse, the choices being 20, 40, 60, 80, 100

Any movies marked as a favorite will not get dropped from the view. For example, if your view is the 100 most popular movies, and you mark the 85th most popular movie as a favorite, and then change your view to the top 20 movies, that 85th movie will still be present and you will be showing at least 21 movies.

## Authors

* **George Cohn** - *Initial work* - https://github.com/GeoCohn

## License

Copyright 2016 George Cohn III

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
