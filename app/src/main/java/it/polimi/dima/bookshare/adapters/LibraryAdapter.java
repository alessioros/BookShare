package it.polimi.dima.bookshare.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.polimi.dima.bookshare.Book;
import it.polimi.dima.bookshare.R;

public class LibraryAdapter extends RecyclerView.Adapter {

    private List<Book> mBooks;

    public LibraryAdapter(List<Book> mBooks) {

        this.mBooks = mBooks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_library, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Book book = mBooks.get(position);

    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;

        public ViewHolder(View view) {

            super(view);
            mView = view;

        }
    }
}
