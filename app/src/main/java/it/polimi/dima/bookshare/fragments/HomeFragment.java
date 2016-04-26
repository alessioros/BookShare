package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.MapsActivity;
import it.polimi.dima.bookshare.adapters.LibraryAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;
import it.polimi.dima.bookshare.utils.OnBookLoadingCompleted;

public class HomeFragment extends Fragment {

    private User user;
    private ManageUser manageUser;

    public HomeFragment() {

    }

    public static Fragment newInstance() {

        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        manageUser = new ManageUser(getActivity());

        CircularImageView userImage = (CircularImageView) view.findViewById(R.id.user_image);
        TextView userName = (TextView) view.findViewById(R.id.user_name);
        TextView userLocation = (TextView) view.findViewById(R.id.user_location);
        TextView userCredits = (TextView) view.findViewById(R.id.user_credits);
        TextView userBooks = (TextView) view.findViewById(R.id.user_books);
        TextView userRecBooks = (TextView) view.findViewById(R.id.user_borr_books);
        TextView booksNearby = (TextView) view.findViewById(R.id.books_nearby_title);

        final RelativeLayout userInfo = (RelativeLayout) view.findViewById(R.id.user_information);
        RelativeLayout locInfo = (RelativeLayout) view.findViewById(R.id.location_info);

        locInfo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                getActivity().startActivity(new Intent(getActivity(), MapsActivity.class));
                return true;
            }
        });

        Typeface aller = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Aller_Rg.ttf");

        userName.setTypeface(aller);
        userLocation.setTypeface(aller);
        userBooks.setTypeface(aller);
        userCredits.setTypeface(aller);
        userRecBooks.setTypeface(aller);
        booksNearby.setTypeface(aller);

        userImage.bringToFront();

        try {

            user = new ManageUser(getActivity()).getUser();

            Picasso.with(getActivity()).load(user.getImgURL()).into(userImage);

            userName.setText(user.getName() + " " + user.getSurname());

            userLocation.setText(user.getCity() + ", " + user.getCountry());

            userCredits.setText(user.getCredits() + "");

            userBooks.setText(manageUser.getBookCount() + "");

            userRecBooks.setText(manageUser.getRecBookCount() + "");

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*final ProgressBar recyclerProgress = (ProgressBar) view.findViewById(R.id.recycler_progressBar);
        recyclerProgress.setVisibility(View.VISIBLE);

        new LoadNearbyBooks(new OnBookLoadingCompleted() {
            @Override
            public void onBookLoadingCompleted(ArrayList<Book> books) {

                loadBooksNearby(books);

            }
        }).execute();*/

        return view;
    }

    public void loadBooksNearby(ArrayList<Book> books) {

        int bookSize = 0;

        try {

            bookSize = books.size();

        } catch (NullPointerException e) {

        }

        if (bookSize > 0) {

            try {

                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

                RecyclerView booksNearby = (RecyclerView) getActivity().findViewById(R.id.books_nearby);
                booksNearby.setLayoutManager(layoutManager);

                booksNearby.setAdapter(new LibraryAdapter(books, getActivity(), true, false));

            } catch (NullPointerException e) {

            }


        } else {

            try {

                TextView noBooks = (TextView) getActivity().findViewById(R.id.nobooks_nearby_txt);
                noBooks.setVisibility(View.VISIBLE);

            } catch (NullPointerException e) {

            }


        }

        try {

            ProgressBar recyclerProgress = (ProgressBar) getActivity().findViewById(R.id.recycler_progressBar);
            recyclerProgress.setVisibility(View.GONE);

        } catch (NullPointerException e) {

        }

    }

    class LoadNearbyBooks extends AsyncTask<Void, ArrayList<Book>, ArrayList<Book>> {
        private OnBookLoadingCompleted listener;

        public LoadNearbyBooks(OnBookLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<Book> doInBackground(Void... params) {

            DynamoDBManager DDBM = new DynamoDBManager(getActivity());
            ArrayList<Book> mBooks = null;
            try {

                mBooks = DDBM.getNearbyBooks(new ManageUser(getActivity()).getDistance());
            } catch (Exception e) {

            }

            return mBooks;
        }

        protected void onPostExecute(ArrayList<Book> books) {

            listener.onBookLoadingCompleted(books);
        }
    }
}
