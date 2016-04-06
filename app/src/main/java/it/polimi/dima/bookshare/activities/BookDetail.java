package it.polimi.dima.bookshare.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.squareup.picasso.Picasso;

import it.polimi.dima.bookshare.amazon.DynamoDBManagerTask;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerType;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.R;

public class BookDetail extends AppCompatActivity {

    private Book book;
    private static int REDIRECT_TIME_OUT = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        getSupportActionBar().setTitle("Book Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        book = new Book();
        Intent i = getIntent();

        ImageView bookImage = (ImageView) findViewById(R.id.book_image);
        TextView bookTitle = (TextView) findViewById(R.id.book_title);
        TextView bookAuthor = (TextView) findViewById(R.id.book_author);
        TextView bookPageCount = (TextView) findViewById(R.id.book_pagecount);
        TextView bookDescription = (TextView) findViewById(R.id.book_description);
        TextView bookPublisher = (TextView) findViewById(R.id.book_publisher);


        Typeface aller = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");

        bookTitle.setTypeface(aller);
        bookAuthor.setTypeface(aller);
        bookPageCount.setTypeface(aller);
        bookDescription.setTypeface(aller);
        bookPublisher.setTypeface(aller);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            book = (Book) i.getParcelableExtra("book");
            bookDescription.setText(book.getDescription());
            bookAuthor.setText(book.getAuthor());

            if (book.getPageCount() != 0) {

                bookPageCount.setText(book.getPageCount() + " pages");
            }

            bookTitle.setText(book.getTitle());

            try {
                if (!book.getPublisher().equals(null) && !book.getPublishedDate().equals(null)) {

                    bookPublisher.setText(book.getPublisher() + " - " + book.getPublishedDate());

                } else if (!book.getPublisher().equals(null)) {

                    bookPublisher.setText(book.getPublisher());

                } else if (!book.getPublishedDate().equals(null)) {

                    bookPublisher.setText(book.getPublishedDate());
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }


            Picasso.with(BookDetail.this).load(book.getImgURL()).into(bookImage);

        }

        if (i.getStringExtra("button").equals("delete")) {

            Button deleteButton = (Button) findViewById(R.id.delete_button);
            deleteButton.setVisibility(Button.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(BookDetail.this);

                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            try {
                                DynamoDBManager DDMB = new DynamoDBManager(BookDetail.this);
                                DDMB.deleteBook(book);

                                Toast.makeText(BookDetail.this, "book deleted succesfully", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(BookDetail.this, MainActivity.class);

                                intent.putExtra("redirect", "library");
                                startActivity(intent);

                            } catch (Exception e) {

                                Toast.makeText(BookDetail.this, "Error, action failed", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

                    builder.setMessage(R.string.delete_confirm)
                            .setTitle(R.string.del_book);

                    AlertDialog dialog = builder.create();
                    dialog.show();


                }
            });
        }
        else if(i.getStringExtra("button").equals("add")){
            Button addButton = (Button) findViewById(R.id.add_button);
            addButton.setVisibility(Button.VISIBLE);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    book.setOwnerID(Profile.getCurrentProfile().getId());

                    // add book to DynamoDB
                    new DynamoDBManagerTask(BookDetail.this, book).execute(DynamoDBManagerType.INSERT_BOOK);

                    // redirects to library after 0.5 seconds, allowing library to display the new book
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {

                            Intent intent = new Intent(BookDetail.this, MainActivity.class);

                            intent.putExtra("redirect", "library");
                            startActivity(intent);

                            finish();
                        }
                    }, REDIRECT_TIME_OUT);

                }
            });
        } else if(i.getStringExtra("button").equals("lend")){

            Button lendButton = (Button) findViewById(R.id.lend_button);
            lendButton.setVisibility(Button.VISIBLE);


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
