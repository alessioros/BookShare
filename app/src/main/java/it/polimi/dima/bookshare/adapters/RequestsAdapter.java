package it.polimi.dima.bookshare.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.BookRequest;
import it.polimi.dima.bookshare.tables.User;

/**
 * Created by matteo on 13/04/16.
 */
public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    private ArrayList<BookRequest> mBookRequests;
    private Context context;
    private User user;
    private Book book;

    public RequestsAdapter(ArrayList<BookRequest> mBookRequests, Context context) {
        this.mBookRequests = mBookRequests;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_result, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Typeface aller = Typeface.createFromAsset(context.getAssets(), "fonts/Aller_Rg.ttf");

        holder.mTitle.setTypeface(aller);
        holder.mAuthor.setTypeface(aller);
        holder.mOwner.setTypeface(aller);

        BookRequest bookRequest = mBookRequests.get(position);
        if (bookRequest.getAskerID().equals(PreferenceManager.getDefaultSharedPreferences(context).getString("ID", null))) {
            user = new DynamoDBManager(context).getUser(bookRequest.getReceiverID());
            book = new DynamoDBManager(context).getBook(bookRequest.getBookISBN(), bookRequest.getReceiverID());

        } else {
            user = new DynamoDBManager(context).getUser(bookRequest.getAskerID());
            book = new DynamoDBManager(context).getBook(bookRequest.getBookISBN(), bookRequest.getReceiverID());
        }

        Picasso.with(context).load(book.getImgURL()).into(holder.mImage);

        if (book.getTitle().length() > 23) {

            holder.mTitle.setText(book.getTitle().substring(0, 23) + "..");
        } else {

            holder.mTitle.setText(book.getTitle());
        }

        holder.mAuthor.setText(book.getAuthor());
        holder.mOwner.setText(user.getName() + " " + user.getSurname());
        holder.mLocation.setText(user.getCity() + ", " + user.getCountry());

    }

    @Override
    public int getItemCount() {
        return mBookRequests.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final View mView;
        public final ImageView mImage;
        public final TextView mTitle, mAuthor, mOwner, mLocation;

        public ViewHolder(View view) {

            super(view);
            mView = view;
            mImage = (ImageView) view.findViewById(R.id.card_image);
            mTitle = (TextView) view.findViewById(R.id.card_title);
            mAuthor = (TextView) view.findViewById(R.id.card_author);
            mOwner = (TextView) view.findViewById(R.id.card_owner);
            mLocation = (TextView) view.findViewById(R.id.card_owner_location);

            view.setOnClickListener(this);
            view.setClickable(true);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
