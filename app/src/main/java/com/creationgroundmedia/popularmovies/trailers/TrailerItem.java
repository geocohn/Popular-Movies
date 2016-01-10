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

/**
 * Holds the useful fields returned by themoviedb for a trailer when you query for trailers
 */
public class TrailerItem {
    private String name;
    private String youtubeKey;

    public TrailerItem(String name, String youtubeKey) {
        this.setName(name);
        this.setYoutubeKey(youtubeKey);
    }

    @Override
    public String toString() {
        return "TrailerItem {name = " + getName() + ", youtubeKey = " + getYoutubeKey() + "}";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getYoutubeKey() {
        return youtubeKey;
    }

    public void setYoutubeKey(String youtubeKey) {
        this.youtubeKey = youtubeKey;
    }
}
