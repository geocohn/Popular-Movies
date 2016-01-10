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

/**
 * Holds all the fields returned by themoviedb for a review when you query for reviews
 */
public class ReviewItem {
    private String id;
    private String author;
    private String content;
    private String url;


    public ReviewItem(String id, String author, String content, String url) {
        this.setId(id);
        this.setAuthor(author);
        this.setContent(content);
        this.setUrl(url);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "ReviewItem {author = " + getAuthor() + ", url = " + getUrl() + "}";
    }
}
