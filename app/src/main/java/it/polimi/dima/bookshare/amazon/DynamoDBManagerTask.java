package it.polimi.dima.bookshare.amazon;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Profile;

import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.BookRequest;
import it.polimi.dima.bookshare.tables.User;

/**
 * Created by matteo on 26/03/16.
 */
public class DynamoDBManagerTask extends AsyncTask<DynamoDBManagerType, Void, DynamoDBManagerTaskResult> {

    private Context context;
    private Book book;
    private User user,user2;
    private BookRequest bookRequest;
    private String id;

    public DynamoDBManagerTask(Context context, String id) {

        this.context = context;
        this.id = id;
    }

    public DynamoDBManagerTask(Context context, User user) {

        this.context = context;
        this.user = user;
    }

    public DynamoDBManagerTask(Context context, Book book) {
        this.context = context;
        this.book = book;
    }

    public DynamoDBManagerTask(Context context, BookRequest bookRequest) {
        this.context = context;
        this.bookRequest = bookRequest;
    }

    public DynamoDBManagerTask(Context context, BookRequest bookRequest,Book book,User user,User user2) {
        this.context = context;
        this.user = user;
        this.book = book;
        this.bookRequest = bookRequest;
        this.user2=user2;
    }

    protected DynamoDBManagerTaskResult doInBackground(DynamoDBManagerType... types) {

        DynamoDBManager DDBM = new DynamoDBManager(context);

        String tableStatus = DDBM.getBookTableStatus();

        DynamoDBManagerTaskResult result = new DynamoDBManagerTaskResult();
        result.setTableStatus(tableStatus);
        result.setTaskType(types[0]);

        if (types[0] == DynamoDBManagerType.CREATE_TABLE) {
            if (tableStatus.length() == 0) {
                DDBM.createTable();
            }
        } else if (types[0] == DynamoDBManagerType.INSERT_BOOK) {
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                DDBM.insertBook(book);
            }
        } else if (types[0] == DynamoDBManagerType.LIST_BOOKS) {
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                Log.i("BOOKLIST", DDBM.getBookList().toString());
            }
        } else if (types[0] == DynamoDBManagerType.CLEAN_UP) {
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                DDBM.cleanUp();
            }
        } else if (types[0] == DynamoDBManagerType.GET_USER_BOOKS) {
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {

                DDBM.getBooks(Profile.getCurrentProfile().getId());
            }
        } else if (types[0] == DynamoDBManagerType.CREATE_TABLE_USERS) {
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {

                DDBM.createTableUsers();
            }
        } else if (types[0] == DynamoDBManagerType.INSERT_USER) {
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                DDBM.insertUser(user);
            }
        } else if (types[0] == DynamoDBManagerType.INSERT_BOOKREQUEST) {
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                DDBM.insertBookRequest(bookRequest);
            }

        } else if (types[0] == DynamoDBManagerType.CONFIRM_BOOKREQUEST) {
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                DDBM.confirmExchange(bookRequest,book,user,user2);
            }
        }
        return result;

    }

    protected void onPostExecute(DynamoDBManagerTaskResult result) {

        if (result.getTaskType() == DynamoDBManagerType.CREATE_TABLE || result.getTaskType() == DynamoDBManagerType.CREATE_TABLE_USERS) {

            if (result.getTableStatus().length() != 0) {
                Toast.makeText(
                        context,
                        "The table already exists.\nTable Status: "
                                + result.getTableStatus(),
                        Toast.LENGTH_LONG).show();
            }

        } else if (result.getTaskType() == DynamoDBManagerType.LIST_BOOKS
                && result.getTableStatus().equalsIgnoreCase("ACTIVE")) {

        } else if (!result.getTableStatus().equalsIgnoreCase("ACTIVE")) {

            Toast.makeText(
                    context,
                    "The table is not ready yet.\nTable Status: "
                            + result.getTableStatus(), Toast.LENGTH_LONG)
                    .show();
        } else if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                && result.getTaskType() == DynamoDBManagerType.INSERT_BOOK) {

            //Toast.makeText(context, "Book inserted successfully!", Toast.LENGTH_SHORT).show();

        } else if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                && result.getTaskType() == DynamoDBManagerType.GET_USER_BOOKS) {

        } else if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                && result.getTaskType() == DynamoDBManagerType.INSERT_USER) {

            //Toast.makeText(context, "User inserted successfully!", Toast.LENGTH_SHORT).show();

        } else if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                && result.getTaskType() == DynamoDBManagerType.INSERT_BOOKREQUEST) {
            Toast.makeText(context, "Book requested!", Toast.LENGTH_SHORT).show();
        }
    }
}

