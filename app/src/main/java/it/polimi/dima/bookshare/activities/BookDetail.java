package it.polimi.dima.bookshare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import it.polimi.dima.bookshare.Book;
import it.polimi.dima.bookshare.R;

public class BookDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        Book book = (Book) i.getParcelableExtra("Book");

        ImageView bookImage = (ImageView) findViewById(R.id.book_image);
        TextView bookTitle = (TextView) findViewById(R.id.book_title);
        TextView bookAuthor = (TextView) findViewById(R.id.book_author);
        TextView bookDescription = (TextView) findViewById(R.id.book_description);
        TextView owner = (TextView) findViewById(R.id.owner);
        CircularImageView ownerImage=(CircularImageView) findViewById(R.id.owner_image);
        Button lendButton = (Button) findViewById(R.id.lend_button);

        Picasso.with(this).load(book.getImgURL()).into(bookImage);
        bookTitle.setText(book.getTitle());
        bookAuthor.setText(book.getAuthor());
        bookDescription.setText(book.getDescription());
        lendButton.setText("I want the BOOK");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
