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

public class ReviewsActivity extends AppCompatActivity {

    private boolean reviewsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        reviewsLoaded = false;

        Fragment fragment = ReviewFragment.newInstance();

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack("general_reviews")
                .commit();

        getSupportActionBar().setTitle("Reviews");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            if (reviewsLoaded) {

                reviewsLoaded = false;

                if (getFragmentManager().findFragmentByTag("reviews_detail") != null)
                    getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag("reviews_detail")).commit();

                Fragment fragment = ReviewFragment.newInstance();

                getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();

                getSupportActionBar().setTitle("Reviews");

            } else {

                startActivity(new Intent(ReviewsActivity.this, MainActivity.class));
                finish();
            }

        }

        return super.onOptionsItemSelected(item);
    }

    public void goToMyRev() {

        reviewsLoaded = true;

        if (getFragmentManager().findFragmentByTag("general_reviews") != null)
            getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag("general_reviews")).commit();

        Fragment fragment = MyReviewsFragment.newInstance();

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack("reviews_detail").commit();

        getSupportActionBar().setTitle("My Reviews");

    }

    public void goToRevOfMe() {

        reviewsLoaded = true;

        if (getFragmentManager().findFragmentByTag("general_reviews") != null)
            getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag("general_reviews")).commit();

        Fragment fragment = MyReviewsFragment.newInstance();

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack("reviews_detail").commit();

        getSupportActionBar().setTitle("Reviews About Me");
    }
}
