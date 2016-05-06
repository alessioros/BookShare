package it.polimi.dima.bookshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Rating;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.BookDetail;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.User;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private ArrayList<Book> mBooks;
    private Context context;
    private Typeface aller;

    public SearchResultsAdapter(ArrayList<Book> mBooks, Context context) {
        this.mBooks = mBooks;
        this.context = context;
        this.aller = Typeface.createFromAsset(context.getAssets(), "fonts/Aller_Rg.ttf");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mTitle.setTypeface(aller);
        holder.mAuthor.setTypeface(aller);
        holder.mOwner.setTypeface(aller);

        Book book = mBooks.get(position);
        User owner = book.getOwner();

        Picasso.with(context).load(book.getImgURL()).into(holder.mImage);
        Picasso.with(context).load(owner.getImgURL()).into(holder.mOwnerImage);

        if (book.getTitle().length() > 23) {

            holder.mTitle.setText(book.getTitle().substring(0, 23) + "..");
        } else {

            holder.mTitle.setText(book.getTitle());
        }

        holder.mOwnerRating.setRating((float)owner.getAvgrating());
        holder.mAuthor.setText(book.getAuthor());
        holder.mOwner.setText(owner.getName() + " " + owner.getSurname());
        holder.mLocation.setText(owner.getCity() + ", " + owner.getCountry());

    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final View mView;
        public final ImageView mImage;
        public final TextView mTitle, mAuthor, mOwner, mLocation;
        public final CircularImageView mOwnerImage;
        public final RatingBar mOwnerRating;

        public ViewHolder(View view) {

            super(view);
            mView = view;
            mImage = (ImageView) view.findViewById(R.id.card_image);
            mTitle = (TextView) view.findViewById(R.id.card_title);
            mAuthor = (TextView) view.findViewById(R.id.card_author);
            mOwner = (TextView) view.findViewById(R.id.card_owner);
            mLocation = (TextView) view.findViewById(R.id.card_owner_location);
            mOwnerImage=(CircularImageView) view.findViewById(R.id.card_owner_image);
            mOwnerRating=(RatingBar) view.findViewById(R.id.owner_rating);

            view.setOnClickListener(this);
            view.setClickable(true);
        }

        @Override
        public void onClick(View view) {

            Intent startDetail = new Intent(context, BookDetail.class);
            startDetail.putExtra("button", "ask");
            startDetail.putExtra("book", (Parcelable) mBooks.get(getLayoutPosition()));
            context.startActivity(startDetail);
        }
    }
}
