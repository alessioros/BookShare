package it.polimi.dima.bookshare.activities;

import android.app.Fragment;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.LibraryAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.fragments.ReviewsAboutMeFragment;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.Review;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;
import it.polimi.dima.bookshare.utils.OnBookLoadingCompleted;

public class UserProfileActivity extends AppCompatActivity {

    private User owner;
    private ArrayList<Review> userReviews;
    private boolean reviewsFragmentLoaded = false;
    private final String TAG = "review_tag";
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        Bundle extras = null;
        try {

            extras = getIntent().getExtras();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (extras != null) {

            userReviews = (ArrayList<Review>) extras.getParcelableArrayList("user_reviews").clone();
            owner = extras.getParcelable("user");
            float avgRating = extras.getFloat("avg_rating");

            loadRecyclerView();
            toolbar.setTitle(owner.getName() + " " + owner.getSurname());

            ImageView ownerImage = (ImageView) findViewById(R.id.owner_image);
            Picasso.with(UserProfileActivity.this).load(owner.getImgURL()).into(ownerImage);

            TextView ownerLoc = (TextView) findViewById(R.id.owner_location_text);
            ownerLoc.setText(owner.getCity() + "," + owner.getCountry());

            Typeface aller = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");

            ownerLoc.setTypeface(aller);

            TextView ownerDistance = (TextView) findViewById(R.id.owner_distance);

            ManageUser manageUser = new ManageUser(UserProfileActivity.this);

            Location myLoc = new Location(LocationManager.GPS_PROVIDER);
            myLoc.setLatitude(manageUser.getUser().getLatitude());
            myLoc.setLongitude(manageUser.getUser().getLongitude());

            Location ownerLocat = new Location(LocationManager.GPS_PROVIDER);
            ownerLocat.setLatitude(owner.getLatitude());
            ownerLocat.setLongitude(owner.getLongitude());

            int distance = (int) myLoc.distanceTo(ownerLocat) / 1000;

            if (distance < 1000) {

                ownerDistance.setText(distance + " Km far from you");
            } else {

                ownerDistance.setText(distance / 1000 + "." + distance % 1000 + " Km far from you");
            }

            TextView avgRatingText = (TextView) findViewById(R.id.owner_reviews_avg);

            RatingBar avgRatingCount = (RatingBar) findViewById(R.id.owner_reviews_avgrating);

            avgRatingText.setTypeface(aller);

            TextView numReviews = (TextView) findViewById(R.id.owner_reviews_count);

            if (userReviews.size() > 0) {

                assert numReviews != null;
                numReviews.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new LoadReviewers(new OnReviewersLoadingCompleted() {
                            @Override
                            public void onReviewersLoadingCompleted(ArrayList<User> reviewers) {

                                CoordinatorLayout cl = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
                                cl.setVisibility(View.GONE);

                                fragment = ReviewsAboutMeFragment.newInstance();
                                Bundle args = new Bundle();
                                args.putParcelableArrayList("reviews", userReviews);
                                args.putParcelableArrayList("reviewers", reviewers);
                                fragment.setArguments(args);

                                reviewsFragmentLoaded = true;
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.content_frame, fragment)
                                        .addToBackStack(TAG)
                                        .commit();
                            }
                        }).execute(userReviews);

                    }
                });

                if (userReviews.size() > 1) {

                    numReviews.setText(userReviews.size() + " " + getResources().getString(R.string.owner_reviews_count));

                    avgRatingText.setText("" + new DecimalFormat("#.##").format(avgRating));
                    avgRatingCount.setRating(avgRating);

                } else {

                    numReviews.setText(userReviews.size() + " " + getResources().getString(R.string.owner_review_count));

                    avgRatingText.setText("" + new DecimalFormat("#.##").format(avgRating));
                    avgRatingCount.setRating(avgRating);
                }

            } else {

                numReviews.setText(getResources().getString(R.string.no_owner_reviews));
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (reviewsFragmentLoaded) {

            getFragmentManager().beginTransaction().remove(fragment).commit();

            CoordinatorLayout cl = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
            cl.setVisibility(View.VISIBLE);

            reviewsFragmentLoaded = false;

        } else {

            super.onBackPressed();
        }
    }

    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
            getWindow().setReturnTransition(transition);
        }
    }

    private void loadRecyclerView() {

        new LoadBooks(new OnBookLoadingCompleted() {
            @Override
            public void onBookLoadingCompleted(ArrayList<Book> books) {

                final ArrayList<Book> ownerBooks = books;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.owner_books_recyclerview);

                        GridLayoutManager gridLayoutManager = new GridLayoutManager(UserProfileActivity.this, 3);

                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.setNestedScrollingEnabled(false);

                        recyclerView.setAdapter(new LibraryAdapter(ownerBooks, owner, userReviews, UserProfileActivity.this));
                    }
                });
            }
        }).execute();
    }

    private interface OnReviewersLoadingCompleted {
        void onReviewersLoadingCompleted(ArrayList<User> reviewers);
    }

    class LoadReviewers extends AsyncTask<ArrayList<Review>, ArrayList<User>, ArrayList<User>> {
        private OnReviewersLoadingCompleted listener;

        public LoadReviewers(OnReviewersLoadingCompleted listener) {
            this.listener = listener;
        }

        @SafeVarargs
        @Override
        protected final ArrayList<User> doInBackground(ArrayList<Review>... params) {

            DynamoDBManager DDBM = new DynamoDBManager(UserProfileActivity.this);

            return DDBM.getReviewers(params[0]);
        }

        protected void onPostExecute(ArrayList<User> reviewers) {

            listener.onReviewersLoadingCompleted(reviewers);
        }
    }

    class LoadBooks extends AsyncTask<Void, ArrayList<Book>, ArrayList<Book>> {
        private OnBookLoadingCompleted listener;

        public LoadBooks(OnBookLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<Book> doInBackground(Void... params) {

            DynamoDBManager DDBM = new DynamoDBManager(UserProfileActivity.this);

            return DDBM.getBooks(owner.getUserID());
        }

        protected void onPostExecute(ArrayList<Book> books) {

            listener.onBookLoadingCompleted(books);
        }
    }
}
