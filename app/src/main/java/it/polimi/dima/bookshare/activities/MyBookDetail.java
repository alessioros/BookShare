package it.polimi.dima.bookshare.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.R;

public class MyBookDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_book_detail);

        getSupportActionBar().setTitle("Book Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final Book book = new Book();

        ImageView bookImage = (ImageView) findViewById(R.id.book_image);
        TextView bookTitle = (TextView) findViewById(R.id.book_title);
        TextView bookAuthor = (TextView) findViewById(R.id.book_author);
        TextView bookPageCount = (TextView) findViewById(R.id.book_pagecount);
        TextView bookDescription = (TextView) findViewById(R.id.book_description);
        Button deleteButton = (Button) findViewById(R.id.delete_button);

        Typeface aller = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");

        bookTitle.setTypeface(aller);
        bookAuthor.setTypeface(aller);
        bookPageCount.setTypeface(aller);
        bookDescription.setTypeface(aller);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            book.setIsbn(extras.getString("isbn"));

            bookTitle.setText(extras.getString("title"));

            book.setTitle(extras.getString("title"));

            bookAuthor.append(extras.getString("author") + " ");
            book.setAuthor(extras.getString("author") + " ");


            try {

                int pageCount = extras.getInt("pageCount");
                bookPageCount.append(pageCount+" pages");
                book.setPageCount(pageCount);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (extras.getString("description") != null) {

                bookDescription.append(extras.getString("description"));

                book.setDescription(extras.getString("description"));
            }

            try {



                book.setImgURL(extras.getString("imgURL"));

                Picasso.with(MyBookDetail.this).load(extras.getString("imgURL")).into(bookImage);

                /*URL url = new URL(extras.getString("imgURL"));
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                bookImage.setImageBitmap(image);*/

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MyBookDetail.this);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        try {
                            DynamoDBManager DDMB = new DynamoDBManager(MyBookDetail.this);
                            DDMB.deleteBook(book);

                            Toast.makeText(MyBookDetail.this, "book deleted succesfully", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(MyBookDetail.this,MainActivity.class);

                            intent.putExtra("redirect", "library");
                            startActivity(intent);

                        }catch (Exception e){

                            Toast.makeText(MyBookDetail.this, "Error, action failed", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
