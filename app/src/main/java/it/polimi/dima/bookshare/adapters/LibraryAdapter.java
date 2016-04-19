package it.polimi.dima.bookshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import it.polimi.dima.bookshare.activities.BookDetail;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.R;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private List<Book> mBooks;
    private Context context;
    private boolean booksNearby;

    public LibraryAdapter(List<Book> mBooks, Context context, boolean booksNearby) {

        this.mBooks = mBooks;
        this.context = context;
        this.booksNearby = booksNearby;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        if (booksNearby) {

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_booknearby, parent, false);

        } else {

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_mybook, parent, false);
        }


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Book book = mBooks.get(position);

        Picasso.with(context).load(book.getImgURL()).into(holder.mImage);

        if (booksNearby) {

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, BookDetail.class);
                    intent.putExtra("book", book);
                    intent.putExtra("button", "lend");

                    context.startActivity(intent);
                }
            });

        } else {

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, BookDetail.class);
                    intent.putExtra("book", book);
                    intent.putExtra("button", "delete");

                    context.startActivity(intent);
                }
            });

        }

    }

    @Override
    public int getItemCount() {

        try {
            return mBooks.size();

        } catch (NullPointerException e) {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final ImageView mImage;

        public ViewHolder(View view) {

            super(view);
            mView = view;
            mImage = (ImageView) view.findViewById(R.id.card_image);

        }
    }
}
