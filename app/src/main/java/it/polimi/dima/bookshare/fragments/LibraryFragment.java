package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import it.polimi.dima.bookshare.Book;
import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.LibraryAdapter;

public class LibraryFragment extends Fragment {

    public LibraryFragment() {

    }

    public static Fragment newInstance() {

        return new LibraryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_library_list, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.book_list);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);

        recyclerView.setLayoutManager(gridLayoutManager);

        // retrieve books
        ArrayList<Book> mBooks = new ArrayList<>();

        recyclerView.setAdapter(new LibraryAdapter(mBooks));

        return view;
    }
}
