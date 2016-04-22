package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.ReviewAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Review;
import it.polimi.dima.bookshare.tables.User;

public class ReviewsAboutMeFragment extends Fragment {

    private ArrayList<Review> mReviews;
    private ArrayList<User> mReviewers;

    public ReviewsAboutMeFragment() {
    }

    public static ReviewsAboutMeFragment newInstance() {

        return new ReviewsAboutMeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        this.mReviews = getArguments().getParcelableArrayList("reviews");

        final ProgressDialog progressDialog =
                ProgressDialog.show(getActivity(),
                        getResources().getString(R.string.wait),
                        getResources().getString(R.string.loading_reviewers), true, false);

        new LoadReviewers(new OnReviewersLoadingCompleted() {
            @Override
            public void onReviewersLoadingCompleted(ArrayList<User> reviewers) {


                mReviewers = reviewers;
                loadRecView();
                progressDialog.dismiss();

            }
        }).execute(mReviews);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_review_list, container, false);

        return view;
    }

    public void loadRecView() {

        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.reviews_recyclerview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(new ReviewAdapter(mReviews, mReviewers, getActivity()));

    }

    private interface OnReviewersLoadingCompleted {
        void onReviewersLoadingCompleted(ArrayList<User> reviewers);
    }

    class LoadReviewers extends AsyncTask<ArrayList<Review>, ArrayList<User>, ArrayList<User>> {
        private OnReviewersLoadingCompleted listener;

        public LoadReviewers(OnReviewersLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<User> doInBackground(ArrayList<Review>... params) {

            DynamoDBManager DDBM = new DynamoDBManager(getActivity());
            ArrayList<User> mReviewers = DDBM.getReviewers(params[0]);

            return mReviewers;
        }

        protected void onPostExecute(ArrayList<User> reviewers) {

            listener.onReviewersLoadingCompleted(reviewers);
        }
    }
}
