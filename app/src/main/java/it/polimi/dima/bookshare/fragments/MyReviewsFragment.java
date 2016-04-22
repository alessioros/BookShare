package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.ReviewAdapter;
import it.polimi.dima.bookshare.tables.Review;

public class MyReviewsFragment extends Fragment {

    private ArrayList<Review> mReviews;

    public MyReviewsFragment() {
    }

    public static MyReviewsFragment newInstance() {

        return new MyReviewsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        this.mReviews = getArguments().getParcelableArrayList("reviews");

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_review_list, container, false);

        if (view instanceof RecyclerView) {

            RecyclerView recyclerView = (RecyclerView) view;

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

            recyclerView.setLayoutManager(linearLayoutManager);

            recyclerView.setAdapter(new ReviewAdapter(mReviews, getActivity()));

        }

        return view;
    }
}
