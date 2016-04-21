package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.polimi.dima.bookshare.R;

public class MyReviewsFragment extends Fragment {

    public MyReviewsFragment() {
    }

    public static MyReviewsFragment newInstance() {

        return new MyReviewsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_review_list, container, false);

        return view;
    }
}
