package it.polimi.dima.bookshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.BookDetail;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.Review;
import it.polimi.dima.bookshare.tables.User;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private List<Book> mBooks;
    private ArrayList<Review> ownerReviews;
    private User bookOwner;
    private Context context;
    private boolean booksNearby;
    private boolean booksBorrowed;
    private boolean ownersBooks;

    public LibraryAdapter(List<Book> mBooks, Context context, boolean booksNearby, boolean booksBorrowed) {

        this.mBooks = mBooks;
        this.context = context;
        this.booksNearby = booksNearby;
        this.booksBorrowed = booksBorrowed;
        this.ownersBooks = false;
    }

    public LibraryAdapter(List<Book> mBooks, User bookOwner, ArrayList<Review> ownerRev, Context context) {

        this.mBooks = mBooks;
        this.context = context;
        this.ownerReviews = ownerRev;
        this.bookOwner = bookOwner;
        this.booksNearby = false;
        this.booksBorrowed = false;
        this.ownersBooks = true;
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
                    intent.putExtra("book", (Parcelable) book);
                    intent.putExtra("button", "ask");

                    context.startActivity(intent);
                }
            });

        } else if (booksBorrowed) {

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, BookDetail.class);
                    intent.putExtra("book", (Parcelable) book);
                    intent.putExtra("button", "return");

                    context.startActivity(intent);
                }
            });

        } else if (ownersBooks) {

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, BookDetail.class);
                    intent.putExtra("book", (Parcelable) book);
                    intent.putParcelableArrayListExtra("owner_reviews", ownerReviews);
                    intent.putExtra("book_owner", bookOwner);
                    intent.putExtra("button", "ask");

                    context.startActivity(intent);
                }
            });


        } else {

            try {
                if (!book.getReceiverID().equals("") && !book.getReceiverID().equals("0")) {

                    System.out.println(book.getTitle() + book.getReceiverID());
                    holder.mImage.setAlpha(0.1f);

                }
            } catch (NullPointerException ignored) {

            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, BookDetail.class);
                    intent.putExtra("book", (Parcelable) book);
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
