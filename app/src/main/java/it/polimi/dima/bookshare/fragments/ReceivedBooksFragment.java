package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.Profile;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.LibraryAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Book;

public class ReceivedBooksFragment extends Fragment {

    public ReceivedBooksFragment() {

    }

    public static Fragment newInstance() {

        return new ReceivedBooksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mybook_list, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.book_list);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);

        recyclerView.setLayoutManager(gridLayoutManager);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

        fab.setVisibility(View.GONE);

        // retrieve books

        DynamoDBManager DDBM = new DynamoDBManager(getActivity());
        ArrayList<Book> mBooks = DDBM.getReceivedBooks(Profile.getCurrentProfile().getId());

        TextView noBooks = (TextView) view.findViewById(R.id.nobooks_text);

        Typeface aller = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Aller_Rg.ttf");

        noBooks.setTypeface(aller);

        if (mBooks.isEmpty()) {

            noBooks.setVisibility(View.VISIBLE);
            noBooks.setText(getResources().getString(R.string.nobooks_received));

        } else {

            noBooks.setVisibility(View.GONE);
        }

        recyclerView.setAdapter(new LibraryAdapter(mBooks, getActivity()));

        return view;
    }

}
