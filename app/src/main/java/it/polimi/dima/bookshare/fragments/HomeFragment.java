package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.Profile;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.MyBookDetail;
import it.polimi.dima.bookshare.activities.VerticalOrientationCA;
import it.polimi.dima.bookshare.tables.Book;

public class HomeFragment extends Fragment {

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

            Profile userProfile = Profile.getCurrentProfile();

            Uri imageUri = userProfile.getProfilePictureUri(300, 300);

            Picasso.with(getActivity()).load(imageUri).into(userImage);

            userName.setText(userProfile.getName());

            userLocation.setText("Milan, Italy");

            userBooks.setText("17 books shared");

            userCredits.setText("140 credits");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }
}
