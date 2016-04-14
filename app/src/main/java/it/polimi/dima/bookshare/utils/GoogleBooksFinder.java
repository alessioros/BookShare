package it.polimi.dima.bookshare.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

import it.polimi.dima.bookshare.tables.Book;

public class GoogleBooksFinder {

    public GoogleBooksFinder() {
    }

    public Book findBook(JSONArray jArray, Book book) {

        try {

            JSONObject volumeInfo = jArray.getJSONObject(0).getJSONObject("volumeInfo");

            // ----- BOOK COVER -----
            try {
                JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");

                book.setImgURL(imageLinks.getString("thumbnail"));

            } catch (JSONException e) {
            }

            // ----- TITLE -----
            try {

                book.setTitle(URLDecoder.decode(volumeInfo.getString("title"), "UTF-8"));

            } catch (Exception e) {
            }

            // ----- PAGE COUNT -----
            try {
                book.setPageCount(Integer.parseInt(volumeInfo.getString("pageCount")));

            } catch (JSONException e) {
            }

            // ----- AUTHORS -----
            try {
                JSONArray authors = volumeInfo.getJSONArray("authors");

                book.setAuthor(URLDecoder.decode(authors.getString(0), "UTF-8"));

            } catch (Exception e) {
            }

            // ----- PUBLISHER -----
            try {
                book.setPublisher(URLDecoder.decode(volumeInfo.getString("publisher"), "UTF-8"));

            } catch (Exception e) {
            }

            // ----- PUBLISHED DATE -----
            try {
                book.setPublishedDate(volumeInfo.getString("publishedDate"));

            } catch (JSONException e) {
            }

            // ----- DESCRIPTION -----
            try {
                book.setDescription(URLDecoder.decode(volumeInfo.getString("description"), "UTF-8"));

            } catch (Exception e) {
            }

        } catch (JSONException e) {

            return null;
        }


        return book;
    }

}
