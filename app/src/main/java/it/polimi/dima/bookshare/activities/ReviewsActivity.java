package it.polimi.dima.bookshare.activities;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.fragments.MyReviewsFragment;
import it.polimi.dima.bookshare.fragments.ReviewFragment;
import it.polimi.dima.bookshare.fragments.ReviewsAboutMeFragment;
import it.polimi.dima.bookshare.tables.Review;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

public class ReviewsActivity extends AppCompatActivity {

    private boolean reviewsLoaded;
    private final String GENERAL_TAG = "general_reviews";
    private final String DETAIL_TAG = "reviews_detail";
    private ProgressDialog progressDialog;
    private ArrayList<Review> myReviews, reviewsAM;
    private ArrayList<User> mReviewers, mTargets;
    private boolean firstFinish = false, secondFinish = false;
    private float myAvgRating = 0, aboutMeAvgRating = 0;
    private int numMyRev = 0, numAboutMeRev = 0;
    private Calendar lastRevDate, lastAboutMeRevDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        reviewsLoaded = false;

        loadReviews();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            if (reviewsLoaded) {

                reviewsLoaded = false;

                if (getFragmentManager().findFragmentByTag(DETAIL_TAG) != null)
                    getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag(DETAIL_TAG)).commit();

                Fragment fragment = ReviewFragment.newInstance();

                Bundle args = new Bundle();
                args.putFloat("myAvgRating", myAvgRating);
                args.putFloat("aboutMeAvgRating", aboutMeAvgRating);
                args.putInt("numMyRev", numMyRev);
                args.putInt("numAboutMeRev", numAboutMeRev);
                args.putString("lastRevDate", getFormattedDate(lastRevDate));
                args.putString("lastAboutMeRevDate", getFormattedDate(lastAboutMeRevDate));
                fragment.setArguments(args);

                getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

                getSupportActionBar().setTitle(getResources().getString(R.string.reviews_title));

            } else {

                startActivity(new Intent(ReviewsActivity.this, MainActivity.class));
                finish();
            }

        }

        return super.onOptionsItemSelected(item);
    }

    public void goToMyRev() {

        reviewsLoaded = true;

        if (getFragmentManager().findFragmentByTag(GENERAL_TAG) != null)
            getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag(GENERAL_TAG)).commit();

        Fragment fragment = MyReviewsFragment.newInstance();
        Bundle args = new Bundle();
        args.putParcelableArrayList("reviews", myReviews);
        args.putParcelableArrayList("reviewers", mTargets);
        fragment.setArguments(args);

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(DETAIL_TAG).commit();

        getSupportActionBar().setTitle(getResources().getString(R.string.title_myrev));

    }

    public void goToRevOfMe() {

        reviewsLoaded = true;

        if (getFragmentManager().findFragmentByTag(GENERAL_TAG) != null)
            getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag(GENERAL_TAG)).commit();

        Fragment fragment = ReviewsAboutMeFragment.newInstance();
        Bundle args = new Bundle();
        args.putParcelableArrayList("reviews", reviewsAM);
        args.putParcelableArrayList("reviewers", mReviewers);
        fragment.setArguments(args);

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(DETAIL_TAG).commit();

        getSupportActionBar().setTitle(getResources().getString(R.string.title_revofme));
    }

    @Override
    public void onBackPressed() {

        if (reviewsLoaded) {

            reviewsLoaded = false;

            if (getFragmentManager().findFragmentByTag(DETAIL_TAG) != null)
                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag(DETAIL_TAG)).commit();

            Fragment fragment = ReviewFragment.newInstance();

            Bundle args = new Bundle();
            args.putFloat("myAvgRating", myAvgRating);
            args.putFloat("aboutMeAvgRating", aboutMeAvgRating);
            args.putInt("numMyRev", numMyRev);
            args.putInt("numAboutMeRev", numAboutMeRev);
            args.putString("lastRevDate", getFormattedDate(lastRevDate));
            args.putString("lastAboutMeRevDate", getFormattedDate(lastAboutMeRevDate));
            fragment.setArguments(args);

            getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

            getSupportActionBar().setTitle(getResources().getString(R.string.reviews_title));

        } else {

            startActivity(new Intent(ReviewsActivity.this, MainActivity.class));
            finish();
        }
    }

    public void loadReviews() {

        progressDialog =
                ProgressDialog.show(ReviewsActivity.this,
                        getResources().getString(R.string.wait),
                        getResources().getString(R.string.loading_reviews), true, false);

        firstFinish = false;
        secondFinish = false;

        new LoadMyReviews(new OnReviewLoadingCompleted() {
            @Override
            public void onReviewLoadingCompleted(ArrayList<Review> reviews) {

                myReviews = reviews;

                numMyRev = reviews.size();

                for (Review review : reviews) {

                    myAvgRating += review.getRating();
                }

                myAvgRating = myAvgRating / numMyRev;

                for (Review rev : reviews) {

                    rev.setReviewerID(rev.getTargetUserID());

                    DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

                    try {

                        Calendar date = Calendar.getInstance();
                        date.setTime(format.parse(rev.getDate()));
                        if (lastRevDate == null) {

                            lastRevDate = date;

                        } else if (lastRevDate.after(date)) {

                            lastRevDate = date;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                new LoadReviewers(new OnReviewersLoadingCompleted() {
                    @Override
                    public void onReviewersLoadingCompleted(ArrayList<User> reviewers) {

                        mTargets = reviewers;

                        firstFinish = true;

                        // If all the queries are completed
                        if (secondFinish) {

                            progressDialog.dismiss();
                            Fragment fragment = ReviewFragment.newInstance();

                            Bundle args = new Bundle();
                            args.putFloat("myAvgRating", myAvgRating);
                            args.putFloat("aboutMeAvgRating", aboutMeAvgRating);
                            args.putInt("numMyRev", numMyRev);
                            args.putInt("numAboutMeRev", numAboutMeRev);
                            args.putString("lastRevDate", getFormattedDate(lastRevDate));
                            args.putString("lastAboutMeRevDate", getFormattedDate(lastAboutMeRevDate));
                            fragment.setArguments(args);

                            getFragmentManager().beginTransaction()
                                    .replace(R.id.content_frame, fragment)
                                    .addToBackStack(GENERAL_TAG)
                                    .commit();

                            getSupportActionBar().setTitle(getResources().getString(R.string.reviews_title));
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                        }
                    }
                }).execute(reviews);

            }
        }).execute();

        new LoadReviewsAM(new OnReviewLoadingCompleted() {
            @Override
            public void onReviewLoadingCompleted(ArrayList<Review> reviews) {

                reviewsAM = reviews;

                numAboutMeRev = reviews.size();

                for (Review review : reviews) {

                    aboutMeAvgRating += review.getRating();
                }

                aboutMeAvgRating = aboutMeAvgRating / numAboutMeRev;

                for (Review rev : reviews) {

                    DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                    try {

                        Calendar date = Calendar.getInstance();
                        date.setTime(format.parse(rev.getDate()));
                        if (lastAboutMeRevDate == null) {

                            lastAboutMeRevDate = date;

                        } else if (lastAboutMeRevDate.after(date)) {

                            lastAboutMeRevDate = date;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }

                new LoadReviewers(new OnReviewersLoadingCompleted() {
                    @Override
                    public void onReviewersLoadingCompleted(ArrayList<User> reviewers) {

                        mReviewers = reviewers;

                        secondFinish = true;

                        // If all the queries are completed
                        if (firstFinish) {

                            progressDialog.dismiss();
                            Fragment fragment = ReviewFragment.newInstance();

                            Bundle args = new Bundle();
                            args.putFloat("myAvgRating", myAvgRating);
                            args.putFloat("aboutMeAvgRating", aboutMeAvgRating);
                            args.putInt("numMyRev", numMyRev);
                            args.putInt("numAboutMeRev", numAboutMeRev);
                            args.putString("lastRevDate", getFormattedDate(lastRevDate));
                            args.putString("lastAboutMeRevDate", getFormattedDate(lastAboutMeRevDate));
                            fragment.setArguments(args);

                            getFragmentManager().beginTransaction()
                                    .replace(R.id.content_frame, fragment)
                                    .addToBackStack(GENERAL_TAG)
                                    .commit();

                            getSupportActionBar().setTitle(getResources().getString(R.string.reviews_title));
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                        }
                    }
                }).execute(reviewsAM);
            }
        }).execute();

    }


    private interface OnReviewLoadingCompleted {
        void onReviewLoadingCompleted(ArrayList<Review> reviews);
    }

    private interface OnReviewersLoadingCompleted {
        void onReviewersLoadingCompleted(ArrayList<User> reviewers);
    }

    class LoadMyReviews extends AsyncTask<Void, ArrayList<Review>, ArrayList<Review>> {
        private OnReviewLoadingCompleted listener;

        public LoadMyReviews(OnReviewLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<Review> doInBackground(Void... params) {

            DynamoDBManager DDBM = new DynamoDBManager(ReviewsActivity.this);

            return DDBM.getMyReviews();
        }

        protected void onPostExecute(ArrayList<Review> reviews) {

            listener.onReviewLoadingCompleted(reviews);
        }
    }

    class LoadReviewsAM extends AsyncTask<Void, ArrayList<Review>, ArrayList<Review>> {
        private OnReviewLoadingCompleted listener;

        public LoadReviewsAM(OnReviewLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<Review> doInBackground(Void... params) {

            return new DynamoDBManager(ReviewsActivity.this).getReviewsAbout(new ManageUser(ReviewsActivity.this).getUser().getUserID());
        }

        protected void onPostExecute(ArrayList<Review> reviews) {

            listener.onReviewLoadingCompleted(reviews);
        }
    }

    class LoadReviewers extends AsyncTask<ArrayList<Review>, ArrayList<User>, ArrayList<User>> {
        private OnReviewersLoadingCompleted listener;

        public LoadReviewers(OnReviewersLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<User> doInBackground(ArrayList<Review>... params) {

            DynamoDBManager DDBM = new DynamoDBManager(ReviewsActivity.this);

            return DDBM.getReviewers(params[0]);
        }

        protected void onPostExecute(ArrayList<User> reviewers) {

            listener.onReviewersLoadingCompleted(reviewers);
        }
    }

    public String getFormattedDate(Calendar date) {


        if (date.get(Calendar.MONTH) > 8) {

            return date.get(Calendar.DAY_OF_MONTH) + "/" + (date.get(Calendar.MONTH) + 1) + "/" + date.get(Calendar.YEAR);

        } else {

            return date.get(Calendar.DAY_OF_MONTH) + "/0" + (date.get(Calendar.MONTH) + 1) + "/" + date.get(Calendar.YEAR);
        }

    }
}
