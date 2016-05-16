package it.polimi.dima.bookshare.utils;

import java.util.ArrayList;

import it.polimi.dima.bookshare.tables.Book;

/**
 * Created by matteo on 16/05/16.
 */
public interface OnSearchResultsLoadingCompleted {
        void OnSearchResultsLoadingCompleted(ArrayList<Book> searchResults);
}
