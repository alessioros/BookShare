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
        userInfo.setVisibility(View.GONE);

        try {

            user = new ManageUser(getActivity()).getUser();

            Picasso.with(getActivity()).load(user.getImgURL()).into(userImage);

            userName.setText(user.getName() + " " + user.getSurname());

            userLocation.setText(user.getCity() + ", " + user.getCountry());

            new LoadBookCount(new OnBookCountCompleted() {
                @Override
                public void onBookCountCompleted(int count, int recCount) {

                    refreshTextView(count, recCount);

                }
            }).execute(user.getUserID());

            userCredits.setText(user.getCredits() + "");

        } catch (Exception e) {
            e.printStackTrace();
        }

        final ProgressBar recyclerProgress = (ProgressBar) view.findViewById(R.id.recycler_progressBar);
        recyclerProgress.setVisibility(View.VISIBLE);

        new LoadBooks(new OnBookLoadingCompleted() {
            @Override
            public void onBookLoadingCompleted(ArrayList<Book> books) {

                loadBooksNearby(books);

            }
        }).execute();

        return view;
    }

    public void loadBooksNearby(ArrayList<Book> books) {

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        RecyclerView booksNearby = (RecyclerView) getActivity().findViewById(R.id.books_nearby);
        booksNearby.setLayoutManager(layoutManager);

        booksNearby.setAdapter(new LibraryAdapter(books, getActivity(), true));

        ProgressBar recyclerProgress = (ProgressBar) getActivity().findViewById(R.id.recycler_progressBar);
        recyclerProgress.setVisibility(View.GONE);
    }

    public interface OnBookCountCompleted {
        void onBookCountCompleted(int count, int recCount);
    }


    class LoadBookCount extends AsyncTask<String, Integer, Integer> {
        private OnBookCountCompleted listener;

        public LoadBookCount(OnBookCountCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected Integer doInBackground(String... params) {

            DynamoDBManager DDBM = new DynamoDBManager(getActivity());
            int booksCount = DDBM.getBooksCount(params[0]);
            int receivBooksCount = DDBM.getReceivedBooksCount(params[0]);
            listener.onBookCountCompleted(booksCount, receivBooksCount);

            return booksCount;
        }

    }

    public void refreshTextView(int booksCount, int recBooksCount) {

        final int count = booksCount;
        final int recCount = recBooksCount;

        try {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    RelativeLayout userInfo = (RelativeLayout) getActivity().findViewById(R.id.user_information);
                    userInfo.setVisibility(View.VISIBLE);

                    TextView userBooks = (TextView) getActivity().findViewById(R.id.user_books);
                    userBooks.setText(count + "");

                    TextView userBorrBooks = (TextView) getActivity().findViewById(R.id.user_borr_books);
                    userBorrBooks.setText(recCount + "");

                    ProgressBar userInfoProgress = (ProgressBar) getActivity().findViewById(R.id.userinfo_progressBar);
                    userInfoProgress.setVisibility(View.GONE);

                }
            });

        } catch (Exception e) {

        }
    }

    class LoadBooks extends AsyncTask<Void, ArrayList<Book>, ArrayList<Book>> {
        private OnBookLoadingCompleted listener;

        public LoadBooks(OnBookLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<Book> doInBackground(Void... params) {

            DynamoDBManager DDBM = new DynamoDBManager(getActivity());
            ArrayList<Book> mBooks = null;
            try {

                mBooks = DDBM.getBooks(new ManageUser(getActivity()).getUser().getUserID());
            } catch (Exception e) {

            }

            return mBooks;
        }

        protected void onPostExecute(ArrayList<Book> books) {

            listener.onBookLoadingCompleted(books);
        }
    }
}
