package it.polimi.dima.bookshare.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.ReviewsActivity;
import it.polimi.dima.bookshare.utils.ManageUser;

public class ReviewFragment extends Fragment {

    private TextView firstTitle, secondTitle;
    private CircularImageView myImg, revImg;
    private RatingBar aboutMeRatings, myRatings;
    private LinearLayout myRev, revOfMe;

    public ReviewFragment() {
    }

    public static Fragment newInstance() {
        return new ReviewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reviews, container, false);

        firstTitle = (TextView) view.findViewById(R.id.title_revofme);
        secondTitle = (TextView) view.findViewById(R.id.title_myrev);

        Typeface aller = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Aller_Rg.ttf");

        firstTitle.setTypeface(aller);
        secondTitle.setTypeface(aller);

        myImg = (CircularImageView) view.findViewById(R.id.rev_myimg);
        revImg = (CircularImageView) view.findViewById(R.id.myrev_img);

        Picasso.with(getActivity()).load(new ManageUser(getActivity()).getUser().getImgURL()).into(myImg);
        Picasso.with(getActivity()).load("http://technologyadvice.com/wp-content/themes/techadvice/library/images/icon2-review.png").into(revImg);

        myRatings = (RatingBar) view.findViewById(R.id.my_ratingBar);
        aboutMeRatings = (RatingBar) view.findViewById(R.id.revofme_ratingBar);

        LayerDrawable stars = (LayerDrawable) myRatings.getProgressDrawable();
        stars.getDrawable(0).setColorFilter(ContextCompat.getColor(getActivity(), R.color.lightgrey_star), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(ContextCompat.getColor(getActivity(), R.color.yellowstar), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(ContextCompat.getColor(getActivity(), R.color.yellowstar), PorterDuff.Mode.SRC_ATOP);

        LayerDrawable stars2 = (LayerDrawable) aboutMeRatings.getProgressDrawable();
        stars2.getDrawable(0).setColorFilter(ContextCompat.getColor(getActivity(), R.color.lightgrey_star), PorterDuff.Mode.SRC_ATOP);
        stars2.getDrawable(1).setColorFilter(ContextCompat.getColor(getActivity(), R.color.yellowstar), PorterDuff.Mode.SRC_ATOP);
        stars2.getDrawable(2).setColorFilter(ContextCompat.getColor(getActivity(), R.color.yellowstar), PorterDuff.Mode.SRC_ATOP);

        myRev = (LinearLayout) view.findViewById(R.id.my_reviews);

        revOfMe = (LinearLayout) view.findViewById(R.id.reviews_of_me);


        myRev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReviewsActivity rA = (ReviewsActivity) getActivity();
                rA.goToMyRev();
            }
        });

        revOfMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReviewsActivity rA = (ReviewsActivity) getActivity();
                rA.goToRevOfMe();
            }
        });

        return view;
    }

}
