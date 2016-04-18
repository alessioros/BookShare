package it.polimi.dima.bookshare.adapters;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.BookRequest;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.DialogContact;

/**
 * Created by matteo on 13/04/16.
 */
public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    private static final String TAG = "RequestsAdapter";
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

        final BookRequest bookRequest = mBookRequests.get(position);
        user = bookRequest.getUser();
        book = bookRequest.getBook();
        if (bookRequest.getAskerID().equals(PreferenceManager.getDefaultSharedPreferences(context).getString("ID", null))) {

            holder.buttonRefuse.setVisibility(Button.GONE);
            holder.buttonAccept.setVisibility(Button.GONE);
                if (bookRequest.getAccepted() == 2) {

                    holder.infoRequest.setText(R.string.info_accepted);
                    holder.infoRequest.setVisibility(TextView.VISIBLE);
                    holder.buttonContact.setVisibility(Button.VISIBLE);
                    holder.buttonContact.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogContact dialogContact = new DialogContact();
                            Bundle args = new Bundle();
                            args.putParcelable("user", user);
                            dialogContact.setArguments(args);
                            dialogContact.show(((FragmentActivity) context).getFragmentManager(), "Contact dialog");
                        }
                    });

                } else {

                holder.infoRequest.setText(R.string.info_pending);
                holder.infoRequest.setVisibility(TextView.VISIBLE);

            }


        } else {

            if (bookRequest.getAccepted() == 2) {
                holder.buttonRefuse.setVisibility(Button.GONE);
                holder.buttonAccept.setVisibility(Button.GONE);
                holder.infoRequest.setText(R.string.info_accepted);
                holder.infoRequest.setVisibility(TextView.VISIBLE);
                holder.buttonContact.setVisibility(Button.VISIBLE);
                holder.buttonContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogContact dialogContact = new DialogContact();
                        Bundle args = new Bundle();
                        args.putParcelable("user", user);
                        dialogContact.setArguments(args);
                        dialogContact.show(((FragmentActivity) context).getFragmentManager(), "Contact dialog");
                    }
                });
            } else {
                holder.buttonRefuse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bookRequest.setAccepted(1);
                        new DynamoDBManager(context).updateBookRequest(bookRequest);
                        notifyDataSetChanged();
                    }
                });

                holder.buttonAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bookRequest.setAccepted(2);
                        new DynamoDBManager(context).updateBookRequest(bookRequest);
                        notifyDataSetChanged();
                    }
                });
            }
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final View mView;
        public final ImageView mImage;
        public final TextView mTitle, mAuthor, mOwner, mLocation, infoRequest;
        public final Button buttonAccept, buttonRefuse, buttonContact;

        public ViewHolder(View view) {

            super(view);
            mView = view;
            mImage = (ImageView) view.findViewById(R.id.card_image);
            mTitle = (TextView) view.findViewById(R.id.card_title);
            mAuthor = (TextView) view.findViewById(R.id.card_author);
            mOwner = (TextView) view.findViewById(R.id.card_owner);
            mLocation = (TextView) view.findViewById(R.id.card_owner_location);
            buttonAccept = (Button) view.findViewById(R.id.accept_request);
            buttonRefuse = (Button) view.findViewById(R.id.refuse_request);
            buttonContact = (Button) view.findViewById(R.id.contact_user);
            infoRequest = (TextView) view.findViewById(R.id.info_request);


            view.setOnClickListener(this);
            view.setClickable(true);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
