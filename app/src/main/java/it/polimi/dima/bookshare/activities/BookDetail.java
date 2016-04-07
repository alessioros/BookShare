package it.polimi.dima.bookshare.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.transition.Slide;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.facebook.Profile;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import it.polimi.dima.bookshare.amazon.DynamoDBManagerTask;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerType;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.utils.ManageUser;

public class BookDetail extends AppCompatActivity {

    private Book book;
    private ManageUser manageUser;
    private static int REDIRECT_TIME_OUT = 500;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_book_detail);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(this, android.R.color.transparent));

        book = new Book();
        manageUser = new ManageUser(this);
        Intent i = getIntent();

        final ImageView bookImage = (ImageView) findViewById(R.id.book_image);
        TextView bookTitle = (TextView) findViewById(R.id.book_title);
        TextView bookAuthor = (TextView) findViewById(R.id.book_author);
        TextView bookPageCount = (TextView) findViewById(R.id.book_pagecount);
        TextView bookDescription = (TextView) findViewById(R.id.book_description);
        TextView bookPublisher = (TextView) findViewById(R.id.book_publisher);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

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

            if (book.getTitle().length() > 30) {

                collapsingToolbarLayout.setTitle(book.getTitle().substring(0, 29) + "..");

            } else {

                collapsingToolbarLayout.setTitle(book.getTitle());
            }


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


            Picasso.with(this).load(book.getImgURL()).into(bookImage, new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap bitmap = ((BitmapDrawable) bookImage.getDrawable()).getBitmap();
                    Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            applyPalette(palette);
                        }
                    });
                }

                @Override
                public void onError() {

                }
            });

        }

        if (i.getStringExtra("button").equals("delete")) {

            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_delete));
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deleteBook();
                }
            });
            Button deleteButton = (Button) findViewById(R.id.delete_button);
            deleteButton.setVisibility(Button.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deleteBook();

                }
            });
        }
        else if(i.getStringExtra("button").equals("add")){

            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add));
            Button addButton = (Button) findViewById(R.id.add_button);
            addButton.setVisibility(Button.VISIBLE);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    addBook();

                }
            });

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    addBook();
                }
            });

        } else if(i.getStringExtra("button").equals("lend")){

            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bookmark));
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

    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
            getWindow().setReturnTransition(transition);
        }
    }

    private void applyPalette(Palette palette) {

        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(ContextCompat.getColor(this, R.color.colorPrimary)));
        collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(ContextCompat.getColor(this, R.color.colorPrimaryDark)));
        updateBackground((FloatingActionButton) findViewById(R.id.fab), palette);
        supportStartPostponedEnterTransition();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            updateButtonBackground((Button) findViewById(R.id.add_button), palette);
            updateButtonBackground((Button) findViewById(R.id.delete_button), palette);
            updateButtonBackground((Button) findViewById(R.id.lend_button), palette);
        }
    }

    private void updateBackground(FloatingActionButton fab, Palette palette) {
        int lightVibrantColor = palette.getLightVibrantColor(ContextCompat.getColor(this, R.color.white));
        int vibrantColor = palette.getVibrantColor(ContextCompat.getColor(this, R.color.colorAccent));

        fab.setRippleColor(lightVibrantColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
    }

    private void updateButtonBackground(Button button, Palette palette) {
        int lightVibrantColor = palette.getLightVibrantColor(ContextCompat.getColor(this, R.color.white));
        int vibrantColor = palette.getVibrantColor(ContextCompat.getColor(this, R.color.colorAccent));

        button.setBackgroundColor(lightVibrantColor);
        button.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));

    }
    private void deleteBook() {

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

    private void addBook() {

        book.setOwnerID(manageUser.getUser().getUserID());

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
}

