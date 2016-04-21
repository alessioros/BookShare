package it.polimi.dima.bookshare.amazon;

/**
 * Created by matteo on 26/03/16.
 */
public enum DynamoDBManagerType {
    GET_TABLE_STATUS, CREATE_TABLE, INSERT_BOOK, LIST_BOOKS, CLEAN_UP,
    GET_USER_BOOKS, CREATE_TABLE_USERS,INSERT_USER, INSERT_BOOKREQUEST, CONFIRM_BOOKREQUEST
}