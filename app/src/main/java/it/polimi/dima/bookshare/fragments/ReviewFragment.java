package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.polimi.dima.bookshare.R;

public class ReviewFragment extends Fragment {

    private TextView firstTitle, secondTitle;

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

        //firstTitle.setTypeface(aller);
        //secondTitle.setTypeface(aller);

        return view;
    }

}
