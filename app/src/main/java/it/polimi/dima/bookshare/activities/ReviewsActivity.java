package it.polimi.dima.bookshare.activities;

import android.app.Fragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.fragments.HomeFragment;
import it.polimi.dima.bookshare.fragments.MyReviewsFragment;
import it.polimi.dima.bookshare.fragments.ReviewFragment;
import it.polimi.dima.bookshare.fragments.ReviewsAboutMeFragment;

public class ReviewsActivity extends AppCompatActivity {

    private boolean reviewsLoaded;
    private final String GENERAL_TAG = "general_reviews";
    private final String DETAIL_TAG = "reviews_detail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        reviewsLoaded = false;

        Fragment fragment = ReviewFragment.newInstance();

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(GENERAL_TAG)
                .commit();

        getSupportActionBar().setTitle(getResources().getString(R.string.reviews_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            if (reviewsLoaded) {

                reviewsLoaded = false;

                if (getFragmentManager().findFragmentByTag(DETAIL_TAG) != null)
                    getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag(DETAIL_TAG)).commit();

                Fragment fragment = ReviewFragment.newInstance();

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

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(DETAIL_TAG).commit();

        getSupportActionBar().setTitle(getResources().getString(R.string.title_revofme));
    }
}
