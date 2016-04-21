package it.polimi.dima.bookshare.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.VerticalOrientationCA;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.fragments.FragmentIntentIntegrator;
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
    private Fragment myFragment;

    public RequestsAdapter(ArrayList<BookRequest> mBookRequests, Context context,Fragment fragment) {
        this.mBookRequests = mBookRequests;
        this.context = context;
        this.myFragment=fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_result, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        Typeface aller = Typeface.createFromAsset(context.getAssets(), "fonts/Aller_Rg.ttf");

        holder.setIsRecyclable(false);
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

                holder.infoIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.accepted_icon,context.getTheme()));

                holder.buttonConfirm.setVisibility(Button.VISIBLE);
                holder.buttonConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final BookRequest bookRequest = mBookRequests.get(position);
                        user = bookRequest.getUser();
                        book = bookRequest.getBook();
                        if(user.getCredits() < 10) {

                            Toast.makeText(context,R.string.not_enough_credits,Toast.LENGTH_SHORT).show();

                        }else{

                            FragmentIntentIntegrator scanIntegrator = new FragmentIntentIntegrator(myFragment);
                            scanIntegrator.setCaptureActivity(VerticalOrientationCA.class);
                            scanIntegrator.setPrompt(context.getResources().getString(R.string.scan_isbn));
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("EXCHANGE_ID", bookRequest.getReceiverID()).apply();
                            scanIntegrator.initiateScan();

                        }
                    }
                });

                holder.buttonContact.setVisibility(Button.VISIBLE);
                holder.buttonContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final BookRequest bookRequest = mBookRequests.get(position);
                        user = bookRequest.getUser();
                        book = bookRequest.getBook();
                        DialogContact dialogContact = new DialogContact();
                        Bundle args = new Bundle();
                        args.putParcelable("user", user);
                        dialogContact.setArguments(args);
                        dialogContact.show(((FragmentActivity) context).getFragmentManager(), "Contact dialog");
                    }
                });

            } else if (bookRequest.getAccepted() == 0){


                holder.infoIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.pending_icon,context.getTheme()));


            } else if (bookRequest.getAccepted() == 1) {

                holder.infoIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.refused_icon,context.getTheme()));

            } else if(bookRequest.getAccepted() == 3){
                holder.infoIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.confirmed_icon,context.getTheme()));
            }


        } else {

            if (bookRequest.getAccepted() == 2) {

                holder.buttonRefuse.setVisibility(Button.GONE);
                holder.buttonAccept.setVisibility(Button.GONE);
                holder.infoIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.accepted_icon,context.getTheme()));


                holder.buttonContact.setVisibility(Button.VISIBLE);
                holder.buttonContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final BookRequest bookRequest = mBookRequests.get(position);
                        user = bookRequest.getUser();
                        book = bookRequest.getBook();
                        DialogContact dialogContact = new DialogContact();
                        Bundle args = new Bundle();
                        args.putParcelable("user", user);
                        dialogContact.setArguments(args);
                        dialogContact.show(((FragmentActivity) context).getFragmentManager(), "Contact dialog");
                    }
                });
            } else if (bookRequest.getAccepted() == 0) {

                holder.buttonRefuse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final BookRequest bookRequest = mBookRequests.get(position);
                        user = bookRequest.getUser();
                        book = bookRequest.getBook();
                        bookRequest.setAccepted(1);
                        new DynamoDBManager(context).updateBookRequest(bookRequest);
                        notifyDataSetChanged();
                    }
                });

                holder.buttonAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final BookRequest bookRequest = mBookRequests.get(position);
                        user = bookRequest.getUser();
                        book = bookRequest.getBook();
                        bookRequest.setAccepted(2);
                        new DynamoDBManager(context).updateBookRequest(bookRequest);
                        notifyDataSetChanged();
                    }
                });
            } else if (bookRequest.getAccepted() == 1) {

                holder.buttonRefuse.setVisibility(Button.GONE);
                holder.buttonAccept.setVisibility(Button.GONE);
                holder.infoIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.refused_icon,context.getTheme()));

            } else if(bookRequest.getAccepted() == 3){

                holder.buttonContact.setVisibility(Button.VISIBLE);
                holder.buttonContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final BookRequest bookRequest = mBookRequests.get(position);
                        user = bookRequest.getUser();
                        book = bookRequest.getBook();
                        DialogContact dialogContact = new DialogContact();
                        Bundle args = new Bundle();
                        args.putParcelable("user", user);
                        dialogContact.setArguments(args);
                        dialogContact.show(((FragmentActivity) context).getFragmentManager(), "Contact dialog");
                    }
                });
                holder.buttonRefuse.setVisibility(Button.GONE);
                holder.buttonAccept.setVisibility(Button.GONE);
                holder.infoIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.confirmed_icon,context.getTheme()));

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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{

        public final View mView;
        public final ImageView mImage,infoIcon;
        public final TextView mTitle, mAuthor, mOwner, mLocation;
        public final Button buttonAccept, buttonRefuse, buttonContact, buttonConfirm;

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
            buttonConfirm=(Button) view.findViewById(R.id.confirm_isbn);
            infoIcon=(ImageView) view.findViewById(R.id.info_icon);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            view.setClickable(true);

        }

        @Override
        public void onClick(View view) {

        }

        @Override
        public boolean onLongClick(View view){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.delete_request)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            BookRequest bookRequest=mBookRequests.get(getAdapterPosition());
                            if(bookRequest.getAccepted()<2){
                                new DynamoDBManager(context).deleteBookRequest(bookRequest);
                                notifyDataSetChanged();
                            }else{
                                Toast.makeText(context,R.string.cant_delete,Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    })
                    .show();
            return true;
        }
    }
}
