package it.polimi.dima.bookshare.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;

import java.io.IOException;
import java.net.URL;

import it.polimi.dima.bookshare.Book;
import it.polimi.dima.bookshare.DynamoDBManager;
import it.polimi.dima.bookshare.DynamoDBManagerTask;
import it.polimi.dima.bookshare.DynamoDBManagerType;
import it.polimi.dima.bookshare.R;

public class BookActivity extends AppCompatActivity {

    private Book book=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Book");
        setSupportActionBar(toolbar);

        book=new Book();

        ImageView bookImage = (ImageView) findViewById(R.id.book_image);
        TextView bookTitle = (TextView) findViewById(R.id.book_title);
        TextView bookAuthor = (TextView) findViewById(R.id.book_author);
        TextView bookPublisher = (TextView) findViewById(R.id.book_publisher);
        TextView bookDescription = (TextView) findViewById(R.id.book_description);
        Button addButton = (Button) findViewById(R.id.add_button);

        Typeface aller = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");

        bookTitle.setTypeface(aller);
        bookAuthor.setTypeface(aller);
        bookPublisher.setTypeface(aller);
        bookDescription.setTypeface(aller);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                book.setOwnerID(Profile.getCurrentProfile().getId());

                // add book to DynamoDB
                new DynamoDBManagerTask(BookActivity.this,book).execute(DynamoDBManagerType.INSERT_BOOK);

            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            book.setIsbn(extras.getString("ISBN"));

            bookTitle.setText(extras.getString("title"));

            book.setTitle(extras.getString("title"));

            int numAuth = extras.getInt("numAuth");

            for (int i = 0; i < numAuth; i++) {

                bookAuthor.append(extras.getString("author" + i) + " ");

                book.setAuthor(extras.getString("author" + i) + " ");
            }

            try {

                int pageCount = extras.getInt("pageCount");
                bookPublisher.append(pageCount + " ");
                book.setPageCount(pageCount);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (extras.getString("publisher") != null)
                bookPublisher.append(extras.getString("publisher") + " ");
            if (extras.getString("publishedDate") != null)
                bookPublisher.append(extras.getString("publishedDate"));
            if (extras.getString("description") != null){

                bookDescription.append(extras.getString("description"));

                book.setDescription(extras.getString("description"));
            }

            try {

                URL url = new URL(extras.getString("imgURL"));

                book.setImgURL(extras.getString("imgURL"));

                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                bookImage.setImageBitmap(image);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*private class DynamoDBManagerTask extends
            AsyncTask<DynamoDBManagerType, Void, DynamoDBManagerTaskResult> {

        protected DynamoDBManagerTaskResult doInBackground(DynamoDBManagerType... types) {

            DynamoDBManager DDBM=new DynamoDBManager(BookActivity.this);

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
                    Log.i("BOOKLIST",DDBM.getBookList().toString());
                }
            } else if (types[0] == DynamoDBManagerType.CLEAN_UP) {
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    DDBM.cleanUp();
                }
            }

            return result;
        }

        protected void onPostExecute(DynamoDBManagerTaskResult result) {

            if (result.getTaskType() == DynamoDBManagerType.CREATE_TABLE) {

                if (result.getTableStatus().length() != 0) {
                    Toast.makeText(
                            BookActivity.this,
                            "The table already exists.\nTable Status: "
                                    + result.getTableStatus(),
                            Toast.LENGTH_LONG).show();
                }

            } else if (result.getTaskType() == DynamoDBManagerType.LIST_BOOKS
                    && result.getTableStatus().equalsIgnoreCase("ACTIVE")) {

            } else if (!result.getTableStatus().equalsIgnoreCase("ACTIVE")) {

                Toast.makeText(
                        BookActivity.this,
                        "The table is not ready yet.\nTable Status: "
                                + result.getTableStatus(), Toast.LENGTH_LONG)
                        .show();
            } else if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                    && result.getTaskType() == DynamoDBManagerType.INSERT_BOOK) {
                Toast.makeText(BookActivity.this,
                        "Book inserted successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private enum DynamoDBManagerType {
        GET_TABLE_STATUS, CREATE_TABLE, INSERT_BOOK, LIST_BOOKS, CLEAN_UP
    }

    private class DynamoDBManagerTaskResult {
        private DynamoDBManagerType taskType;
        private String tableStatus;

        public DynamoDBManagerType getTaskType() {
            return taskType;
        }

        public void setTaskType(DynamoDBManagerType taskType) {
            this.taskType = taskType;
        }

        public String getTableStatus() {
            return tableStatus;
        }

        public void setTableStatus(String tableStatus) {
            this.tableStatus = tableStatus;
        }
    }*/

}
