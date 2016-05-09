package it.polimi.dima.bookshare.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LayerDrawable;
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
import android.widget.RelativeLayout;
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
import it.polimi.dima.bookshare.tables.Review;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.InternalStorage;
import it.polimi.dima.bookshare.utils.ManageUser;

public class BookDetail extends AppCompatActivity {

    private Book book;
    private ManageUser manageUser;
    private User owner;
    private static int REDIRECT_TIME_OUT = 500;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private static final String TAG = "BookDetail";
    private String RECBOOKS_KEY = "RECBOOKS";
    private String MYBOOKS_KEY = "MYBOOKS";
    private RatingBar ownerRating;
    private View ruler, secondRuler;
    private boolean loadUserFinished = false, loadRevFinished = false;
    private boolean fromUserProfile = false;
    private RelativeLayout ownerInfoLayout;

    private Intent intent;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        ownerInfoLayout = (RelativeLayout) findViewById(R.id.layout_owner);
        ruler = findViewById(R.id.first_ruler);
        secondRuler = findViewById(R.id.ruler);
        ownerRating = (RatingBar) findViewById(R.id.owner_avgRating);

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

                    ruler.setVisibility(View.GONE);
                    ownerInfoLayout.setVisibility(View.GONE);

                } else {

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) secondRuler.getLayoutParams();
                    params.setMargins(0, 0, 0, 0); //substitute parameters for left, top, right, bottom
                    secondRuler.setLayoutParams(params);
                    intent = new Intent(BookDetail.this, UserProfileActivity.class);

                    try {

                        owner = i.getParcelableExtra("book_owner");
                        final ArrayList<Review> reviews = i.getParcelableArrayListExtra("owner_reviews");
                        intent.putParcelableArrayListExtra("user_reviews", reviews);
                        intent.putExtra("user", owner);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                float sumRat = 0;
                                for (Review rev : reviews) {
                                    sumRat += rev.getRating();
                                }
                                sumRat = sumRat / reviews.size();
                                ownerRating.setRating(sumRat);
                                intent.putExtra("avg_rating", sumRat);

                                Picasso.with(BookDetail.this).load(owner.getImgURL()).into(image_owner);
                                name_owner.setText(owner.getName() + " " + owner.getSurname());
                                location_owner.setText(owner.getCity() + ", " + owner.getCountry());

                                ownerInfoLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        startActivity(intent);
                                    }
                                });

                            }
                        });

                    } catch (Exception e) {

                        // load user's reviews to compute avg rating
                        new LoadReviews(new OnReviewLoadingCompleted() {
                            @Override
                            public void onReviewLoadingCompleted(final ArrayList<Review> reviews) {

                                intent.putParcelableArrayListExtra("user_reviews", reviews);
                                loadRevFinished = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        float sumRat = 0;
                                        for (Review rev : reviews) {
                                            sumRat += rev.getRating();
                                        }
                                        sumRat = sumRat / reviews.size();
                                        ownerRating.setRating(sumRat);
                                        intent.putExtra("avg_rating", sumRat);
                                        if (loadUserFinished) {
                                            ownerInfoLayout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    }
                                });


                            }
                        }).execute(book.getOwnerID());

                        // load user's info
                        new LoadUser(new OnUserLoadingCompleted() {
                            @Override
                            public void onUserLoadingCompleted() {

                                intent.putExtra("user", owner);
                                loadUserFinished = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Picasso.with(BookDetail.this).load(owner.getImgURL()).into(image_owner);
                                        name_owner.setText(owner.getName() + " " + owner.getSurname());
                                        location_owner.setText(owner.getCity() + ", " + owner.getCountry());

                                        if (loadRevFinished) {
                                            ownerInfoLayout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    }
                                });


                            }
                        }).execute(book.getOwnerID());
                    }

                }

            } else {

                ruler.setVisibility(View.GONE);
                ownerInfoLayout.setVisibility(View.GONE);
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
        updateRatingBackground(palette);
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

    private void updateRatingBackground(Palette palette) {

        int vibrantColor = palette.getVibrantColor(ContextCompat.getColor(this, R.color.colorAccent));

        LayerDrawable stars = (LayerDrawable) ownerRating.getProgressDrawable();
        stars.getDrawable(0).setColorFilter(ContextCompat.getColor(BookDetail.this, R.color.lightgrey_star), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(vibrantColor, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(vibrantColor, PorterDuff.Mode.SRC_ATOP);
    }

    private void deleteBook() {

        AlertDialog.Builder builder = new AlertDialog.Builder(BookDetail.this);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                try {

                    new DynamoDBManager(BookDetail.this).deleteBook(book);

                    ArrayList<Book> myNewBooks = (ArrayList<Book>) InternalStorage.readObject(BookDetail.this, MYBOOKS_KEY);
                    Book candidate = new Book();

                    for (Book item : myNewBooks) {

                        if (item.getIsbn().equals(book.getIsbn())) {

                            candidate = item;
                        }
                    }
                    myNewBooks.remove(candidate);

                    InternalStorage.cacheObject(BookDetail.this, MYBOOKS_KEY, myNewBooks);

                    Toast.makeText(BookDetail.this, getResources().getString(R.string.success_delete), Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(BookDetail.this, LibraryActivity.class));

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

            ArrayList<Book> myNewBooks = (ArrayList<Book>) InternalStorage.readObject(BookDetail.this, MYBOOKS_KEY);
            myNewBooks.add(book);
            InternalStorage.cacheObject(BookDetail.this, MYBOOKS_KEY, myNewBooks);

            Toast.makeText(BookDetail.this, getResources().getString(R.string.success_add), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            Toast.makeText(BookDetail.this, getResources().getString(R.string.error_add), Toast.LENGTH_SHORT).show();

        }

        // redirects to library after 0.5 seconds, allowing library to display the new book
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                startActivity(new Intent(BookDetail.this, LibraryActivity.class));

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

    private interface OnReviewLoadingCompleted {
        void onReviewLoadingCompleted(ArrayList<Review> reviews);
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

    class LoadReviews extends AsyncTask<String, ArrayList<Review>, ArrayList<Review>> {
        private OnReviewLoadingCompleted listener;

        public LoadReviews(OnReviewLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<Review> doInBackground(String... params) {

            DynamoDBManager DDBM = new DynamoDBManager(BookDetail.this);

            return DDBM.getReviewsAbout(params[0]);
        }

        protected void onPostExecute(ArrayList<Review> reviews) {

            listener.onReviewLoadingCompleted(reviews);
        }
    }


}



