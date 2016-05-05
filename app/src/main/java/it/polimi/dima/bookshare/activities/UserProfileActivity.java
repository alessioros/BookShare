package it.polimi.dima.bookshare.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.tables.Review;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

public class UserProfileActivity extends AppCompatActivity {

    private User owner;
    private ArrayList<Review> userReviews;

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

            userReviews = extras.getParcelableArrayList("user_reviews");
            owner = extras.getParcelable("user");
            float avgRating = extras.getFloat("avg_rating");

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

                numReviews.setText(userReviews.size() + " " + getResources().getString(R.string.owner_reviews_count));
                avgRatingText.setText("" + avgRating);
                avgRatingCount.setRating(avgRating);

            } else if (userReviews.size() == 1) {

                numReviews.setText(userReviews.size() + " " + getResources().getString(R.string.owner_review_count));
                avgRatingText.setText("" + avgRating);
                avgRatingCount.setRating(avgRating);

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

    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
            getWindow().setReturnTransition(transition);
        }
    }
}
