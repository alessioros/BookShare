package it.polimi.dima.bookshare.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.Constants;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerTask;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerType;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.BookRequest;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

public class BookDetail extends AppCompatActivity {

    private Book book;
    private ManageUser manageUser;
    private User owner;
    private static int REDIRECT_TIME_OUT = 500;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private static final String TAG = "BookDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_book_detail);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        //noinspection ConstantConditions
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
        final TextView name_owner = (TextView) findViewById(R.id.name_owner);
        final TextView location_owner = (TextView) findViewById(R.id.location_owner);
        final CircularImageView image_owner = (CircularImageView) findViewById(R.id.owner_image);
        RatingBar userVal = (RatingBar) findViewById(R.id.revofme_ratingBar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        Typeface aller = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");

        bookTitle.setTypeface(aller);
        bookAuthor.setTypeface(aller);
        bookPageCount.setTypeface(aller);
        bookDescription.setTypeface(aller);
        bookPublisher.setTypeface(aller);
        name_owner.setTypeface(aller);
        location_owner.setTypeface(aller);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            book = i.getParcelableExtra("book");

            try {
                if (book.getDescription().length() > 595) {

                    bookDescription.setText(book.getDescription().substring(0, 595) + "..");

                } else if (!book.getDescription().equals("")) {

                    bookDescription.setText(book.getDescription());
                }

            } catch (NullPointerException e) {

            }

            bookAuthor.setText(book.getAuthor());

            if (book.getPageCount() != 0) {

                bookPageCount.setText(book.getPageCount() + " " + getResources().getString(R.string.book_pages));
            }

            bookTitle.setText(book.getTitle());

            if (book.getTitle().length() > 30) {

                collapsingToolbarLayout.setTitle(book.getTitle().substring(0, 29) + "..");

            } else {

                collapsingToolbarLayout.setTitle(book.getTitle());
            }

            try {
                if (!book.getPublisher().equals("") && !book.getPublishedDate().equals("")) {

                    bookPublisher.setText(book.getPublisher() + " - " + book.getPublishedDate());

                } else if (!book.getPublisher().equals("")) {

                    bookPublisher.setText(book.getPublisher());

                } else if (!book.getPublishedDate().equals("")) {

                    bookPublisher.setText(book.getPublishedDate());
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            if (book.getOwnerID() != null) {
                if (manageUser.getUser().getUserID().equals(book.getOwnerID())) {

                    owner = manageUser.getUser();
                    Picasso.with(BookDetail.this).load(owner.getImgURL()).into(image_owner);
                    name_owner.setText(owner.getName() + " " + owner.getSurname());
                    location_owner.setText(owner.getCity() + ", " + owner.getCountry());

                } else {

                    new LoadUser(new OnUserLoadingCompleted() {
                        @Override
                        public void onUserLoadingCompleted() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Picasso.with(BookDetail.this).load(owner.getImgURL()).into(image_owner);
                                    name_owner.setText(owner.getName() + " " + owner.getSurname());
                                    location_owner.setText(owner.getCity() + ", " + owner.getCountry());
                                }
                            });
                        }
                    }).execute(book.getOwnerID());
                }
            } else {
                image_owner.setVisibility(CircularImageView.GONE);
                name_owner.setVisibility(TextView.GONE);
                location_owner.setVisibility(TextView.GONE);
                userVal.setVisibility(RatingBar.GONE);
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
            Button deleteButton = (Button) findViewById(R.id.button_book_detail);
            deleteButton.setText(R.string.del_book);
            deleteButton.setVisibility(Button.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deleteBook();

                }
            });
        } else if (i.getStringExtra("button").equals("add")) {

            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add));
            Button addButton = (Button) findViewById(R.id.button_book_detail);
            addButton.setText(R.string.add_book);
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

        } else if (i.getStringExtra("button").equals("ask")) {

            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bookmark));
            Button lendButton = (Button) findViewById(R.id.button_book_detail);
            lendButton.setText(R.string.lend_book);
            lendButton.setVisibility(Button.VISIBLE);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    askBook();
                }
            });

            lendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    askBook();
                }
            });

        } else if (i.getStringExtra("button").equals("return")) {

            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.back_arrow_128));
            Button returnButton = (Button) findViewById(R.id.button_book_detail);
            returnButton.setText(R.string.return_book);
            returnButton.setVisibility(Button.VISIBLE);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    returnBook();
                }
            });

            returnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    returnBook();
                }
            });
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

        updateButtonBackground((Button) findViewById(R.id.button_book_detail), palette);

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
        }
    }

    private void deleteBook() {

        AlertDialog.Builder builder = new AlertDialog.Builder(BookDetail.this);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                try {
                    new DynamoDBManager(BookDetail.this).deleteBook(book);

                    Toast.makeText(BookDetail.this, getResources().getString(R.string.success_delete), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(BookDetail.this, MainActivity.class);

                    intent.putExtra("redirect", "library");
                    startActivity(intent);

                } catch (Exception e) {

                    Toast.makeText(BookDetail.this, getResources().getString(R.string.error_delete), Toast.LENGTH_SHORT).show();
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
        //book.setReceiverID("0000000000000000");

        try {

            // add book to DynamoDB
            new DynamoDBManagerTask(BookDetail.this, book).execute(DynamoDBManagerType.INSERT_BOOK);
            Toast.makeText(BookDetail.this, getResources().getString(R.string.success_add), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            Toast.makeText(BookDetail.this, getResources().getString(R.string.error_add), Toast.LENGTH_SHORT);

        }

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

    private void askBook() {

        ArrayList<BookRequest> myBookRequests;
        int id = PreferenceManager.getDefaultSharedPreferences(this).getInt("BookRequestID", 0);

        BookRequest bookRequest = new BookRequest();

        if (id == 0) {
            myBookRequests = new DynamoDBManager(this).getMyBookRequests();

            for (BookRequest br : myBookRequests) {
                if (br.getID() > id) id = br.getID();
            }

            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("BookRequestID", id).apply();
        }

        id += 1;
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("BookRequestID", id).apply();
        bookRequest.setID(id);
        bookRequest.setAskerID(PreferenceManager.getDefaultSharedPreferences(this).getString("ID", null));
        bookRequest.setReceiverID(book.getOwnerID());
        bookRequest.setBookISBN(book.getIsbn());
        bookRequest.setAccepted(0);
        bookRequest.setTime(String.valueOf(new Date().getTime()));

        try {

            Boolean flag = false;
            myBookRequests = new DynamoDBManager(this).getMyBookRequests();
            for (BookRequest existingBookRequest : myBookRequests) {
                if (existingBookRequest.getBookISBN().equals(bookRequest.getBookISBN())
                        && existingBookRequest.getReceiverID().equals(bookRequest.getReceiverID())) {
                    flag = true;
                    Toast.makeText(this, R.string.request_existing, Toast.LENGTH_SHORT).show();
                } else if (manageUser.getUser().getCredits() < Constants.STANDARD_CREDITS) {
                    flag = true;
                    Toast.makeText(this, R.string.not_enough_credits, Toast.LENGTH_SHORT).show();
                }
            }
            if (!flag) {
                // add request to DynamoDB
                new DynamoDBManagerTask(BookDetail.this, bookRequest).execute(DynamoDBManagerType.INSERT_BOOKREQUEST);
            }

        } catch (Exception e) {
            Toast.makeText(BookDetail.this, getResources().getString(R.string.error_ask), Toast.LENGTH_SHORT).show();
        }
    }

    private void returnBook() {
        ArrayList<BookRequest> bookRequests = new DynamoDBManager(this).getMyBookRequests();
        for (BookRequest br : bookRequests) {
            if (br.getBookISBN().equals(book.getIsbn()) && br.getReceiverID().equals(book.getOwnerID())) {
                br.setAccepted(4);
                new DynamoDBManager(this).updateBookRequest(br);
                Toast.makeText(this, R.string.return_sent, Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    public interface OnUserLoadingCompleted {
        void onUserLoadingCompleted();
    }

    class LoadUser extends AsyncTask<String, Void, Void> {
        private OnUserLoadingCompleted listener;

        public LoadUser(OnUserLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(String... params) {

            owner = new DynamoDBManager(BookDetail.this).getUser(params[0]);
            listener.onUserLoadingCompleted();

            return null;
        }

    }

}



