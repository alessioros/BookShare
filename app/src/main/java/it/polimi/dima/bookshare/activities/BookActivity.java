package it.polimi.dima.bookshare.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Profile;

import java.io.IOException;
import java.net.URL;

import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerTask;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerType;
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
                new DynamoDBManagerTask(BookActivity.this,book,null).execute(DynamoDBManagerType.INSERT_BOOK);

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
                bookPublisher.append(pageCount + " pages");
                book.setPageCount(pageCount);

            } catch (Exception e) {
                e.printStackTrace();
            }

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

}
