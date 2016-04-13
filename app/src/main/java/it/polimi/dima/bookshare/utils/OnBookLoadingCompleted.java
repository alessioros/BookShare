package it.polimi.dima.bookshare.utils;

import java.util.ArrayList;

import it.polimi.dima.bookshare.tables.Book;

public interface OnBookLoadingCompleted {
    void onBookLoadingCompleted(ArrayList<Book> books);
}
