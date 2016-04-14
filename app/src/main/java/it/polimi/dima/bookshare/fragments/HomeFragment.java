package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
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

        Typeface aller = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Aller_Rg.ttf");

        userName.setTypeface(aller);
        userLocation.setTypeface(aller);
        userBooks.setTypeface(aller);
        userCredits.setTypeface(aller);

        userImage.bringToFront();

        try {

            user = new ManageUser(getActivity()).getUser();

            Picasso.with(getActivity()).load(user.getImgURL()).into(userImage);

            userName.setText(user.getName() + " " + user.getSurname());

            userLocation.setText(user.getCity() + ", " + user.getCountry());

            new LoadBookCount(new OnBookCountCompleted() {
                @Override
                public void onBookCountCompleted(int count) {

                    refreshTextView(count);
                }
            }).execute(user.getUserID());

            userCredits.setText(user.getCredits() + "");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    public interface OnBookCountCompleted {
        void onBookCountCompleted(int count);
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
            listener.onBookCountCompleted(booksCount);

            return booksCount;
        }

    }

    public void refreshTextView(int booksCount) {

        final int count = booksCount;

        try {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    TextView userBooks = (TextView) getActivity().findViewById(R.id.user_books);
                    userBooks.setText(count + "");

                }
            });

        } catch (Exception e) {

        }
    }
}
