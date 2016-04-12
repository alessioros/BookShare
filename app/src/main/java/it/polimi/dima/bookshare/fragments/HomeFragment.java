package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

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
        TextView userBooks = (TextView) view.findViewById(R.id.user_books);
        TextView userCredits = (TextView) view.findViewById(R.id.user_credits);

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

            userBooks.setText(new ManageUser(getActivity()).getBooksCount() + "");

            userCredits.setText(user.getCredits() + "");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }
}
