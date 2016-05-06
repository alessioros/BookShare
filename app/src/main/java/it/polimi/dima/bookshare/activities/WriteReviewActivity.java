package it.polimi.dima.bookshare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.WrapTogetherSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerTask;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerType;
import it.polimi.dima.bookshare.tables.Review;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

public class WriteReviewActivity extends AppCompatActivity {

    private final static String TAG="WriteReviewActivity";
    private EditText revTitle, revDescription;
    private TextView maxChar;
    private RatingBar revRating;
    private Button addReview,skipReview;
    private String targetUser;

    private final TextWatcher descCharWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

            maxChar.setText(String.valueOf(s.length()) + getResources().getString(R.string.maxchar_incomplete));
        }

        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle(getResources().getString(R.string.review_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        if(getIntent().hasExtra("targetUser")) {
            targetUser = getIntent().getExtras().getString("targetUser");
        }
        revTitle = (EditText) findViewById(R.id.review_title);
        revDescription = (EditText) findViewById(R.id.review_description);
        assert revDescription != null;
        revDescription.addTextChangedListener(descCharWatcher);

        maxChar = (TextView) findViewById(R.id.max_characters);

        revRating = (RatingBar) findViewById(R.id.review_rating);

        addReview = (Button) findViewById(R.id.add_review);

        skipReview=(Button) findViewById(R.id.skip_review);

        addReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Review review = new Review();

                String title = revTitle.getText().toString();
                String description = revDescription.getText().toString();
                float rating = revRating.getRating();

                if (title.length() > 0 && description.length() > 0 && rating > 0) {

                    review.setReviewerID(new ManageUser(WriteReviewActivity.this).getUser().getUserID());
                    review.setTitle(title);
                    review.setRating(rating);
                    review.setDescription(description);
                    Calendar cal = Calendar.getInstance();

                    if (cal.get(Calendar.DAY_OF_MONTH) < 10 && cal.get(Calendar.MONTH) < 9) {

                        review.setDate("0" + cal.get(Calendar.DAY_OF_MONTH) + "/0" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));

                    } else if (cal.get(Calendar.DAY_OF_MONTH) < 10) {

                        review.setDate("0" + cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));

                    } else if (cal.get(Calendar.MONTH) < 9) {

                        review.setDate(cal.get(Calendar.DAY_OF_MONTH) + "/0" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));

                    } else {

                        review.setDate(cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
                    }


                    // CODE TO ADD TARGET USER ID AND REVIEW TO DYNAMODB

                    review.setTargetUserID(targetUser);

                    try {

                        new DynamoDBManagerTask(WriteReviewActivity.this,review).execute(DynamoDBManagerType.INSERT_REVIEW);

                    } catch (Exception e) {

                        Toast.makeText(WriteReviewActivity.this, getResources().getString(R.string.error_add), Toast.LENGTH_SHORT).show();

                    }

                    Intent home=new Intent(WriteReviewActivity.this,MainActivity.class);
                    startActivity(home);
                    finish();

                } else if (title.length() <= 0 && description.length() <= 0 && rating <= 0) {

                    Toast.makeText(WriteReviewActivity.this, getResources().getString(R.string.rev_incomplete_err), Toast.LENGTH_SHORT).show();

                } else if (rating <= 0) {

                    Toast.makeText(WriteReviewActivity.this, getResources().getString(R.string.rev_norating_err), Toast.LENGTH_SHORT).show();

                } else if (title.length() <= 0) {

                    Toast.makeText(WriteReviewActivity.this, getResources().getString(R.string.rev_notitle_err), Toast.LENGTH_SHORT).show();

                } else if (description.length() <= 0) {

                    Toast.makeText(WriteReviewActivity.this, getResources().getString(R.string.rev_nodesc_err), Toast.LENGTH_SHORT).show();

                }
            }
        });

        skipReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent home=new Intent(WriteReviewActivity.this,MainActivity.class);
                startActivity(home);
                finish();

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
