package it.polimi.dima.bookshare.utils;

import java.util.ArrayList;

import it.polimi.dima.bookshare.tables.BookRequest;

/**
 * Created by matteo on 15/04/16.
 */
public interface OnBookRequestsLoadingCompleted {
    void onBookRequestsLoadingCompleted(ArrayList<BookRequest> bookRequests);
}
