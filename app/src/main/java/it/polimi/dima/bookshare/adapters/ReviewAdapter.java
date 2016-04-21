package it.polimi.dima.bookshare.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Review;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> mReviews;
    private ArrayList<User> mReviewers;
    private Context context;
    private boolean myReviews;

    public ReviewAdapter(List<Review> mReviews, Context context, boolean myReviews) {

        this.mReviews = mReviews;
        this.context = context;
        this.myReviews = myReviews;

        if (!myReviews) {

            // load users that have done the reviews
            this.mReviewers = new DynamoDBManager(context).getReviewers(mReviews);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_mybook, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Review review = mReviews.get(position);

        if (myReviews) {

            Picasso.with(context).load(new ManageUser(context).getUser().getImgURL()).into(holder.mImage);

        } else {

            for (User user : mReviewers) {

                if (user.getUserID().equals(review.getReviewerID())) {

                    Picasso.with(context).load(user.getImgURL()).into(holder.mImage);
                }
            }
        }

        holder.mTitle.setText(review.getTitle());
        holder.mDate.setText(review.getDate());
        holder.mDescription.setText(review.getDescription());
        holder.mRating.setRating(review.getRating());

    }

    @Override
    public int getItemCount() {

        try {
            return mReviews.size();

        } catch (NullPointerException e) {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final CircularImageView mImage;
        public final TextView mTitle;
        public final TextView mDate;
        public final TextView mDescription;
        public final RatingBar mRating;

        public ViewHolder(View view) {

            super(view);
            mView = view;
            mImage = (CircularImageView) view.findViewById(R.id.reviewer_img);
            mTitle = (TextView) view.findViewById(R.id.review_title);
            mDate = (TextView) view.findViewById(R.id.review_date);
            mDescription = (TextView) view.findViewById(R.id.review_description);
            mRating = (RatingBar) view.findViewById(R.id.review_rating);

        }
    }
}
