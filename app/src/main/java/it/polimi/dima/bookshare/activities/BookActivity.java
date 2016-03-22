package it.polimi.dima.bookshare.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

import it.polimi.dima.bookshare.R;

public class BookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Book");
        setSupportActionBar(toolbar);

        ImageView bookImage = (ImageView) findViewById(R.id.book_image);
        TextView bookTitle = (TextView) findViewById(R.id.book_title);
        TextView bookAuthor = (TextView) findViewById(R.id.book_author);
        TextView bookPublisher = (TextView) findViewById(R.id.book_publisher);
        TextView bookDescription = (TextView) findViewById(R.id.book_description);

        Typeface aller = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");

        bookTitle.setTypeface(aller);
        bookAuthor.setTypeface(aller);
        bookPublisher.setTypeface(aller);
        bookDescription.setTypeface(aller);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            bookTitle.setText(extras.getString("title"));

            int numAuth = extras.getInt("numAuth");

            for (int i = 0; i < numAuth; i++) {

                bookAuthor.append(extras.getString("author" + i) + " ");
            }

            if (extras.getString("publisher") != null)
                bookPublisher.append(extras.getString("publisher") + " ");
            if (extras.getString("publishedDate") != null)
                bookPublisher.append(extras.getString("publishedDate"));
            if (extras.getString("description") != null)
                bookDescription.append(extras.getString("description"));

            try {

                URL url = new URL(extras.getString("imgURL"));
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                bookImage.setImageBitmap(image);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
