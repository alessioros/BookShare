package it.polimi.dima.bookshare.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.InternalStorage;
import it.polimi.dima.bookshare.utils.ManageUser;

public class SplashScreen extends AppCompatActivity {

    private User user;
    private ManageUser manageUser;
    private final String MYBOOKS_KEY = "MYBOOKS";
    private final String RECBOOKS_KEY = "RECBOOKS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View decorView = getWindow().getDecorView();
        int uiOptions = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        decorView.setSystemUiVisibility(uiOptions);

        manageUser = new ManageUser(SplashScreen.this);

        try {

            user = manageUser.getUser();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (user == null || !manageUser.verifyRegistered()) {

            float DEFAULT_MAX_DIST = 200000f;
            manageUser.setDistance(DEFAULT_MAX_DIST);
            new LoadUser(new OnUserLoadingCompleted() {
                @Override
                public void onUserLoadingCompleted() {

                    if (user == null) {

                        user = new User();
                        manageUser.setBookCount(0);
                        manageUser.setRecBookCount(0);

                        GraphRequest request = GraphRequest.newMeRequest(
                                AccessToken.getCurrentAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {

                                        try {
                                            JSONObject jsonObject = response.getJSONObject().getJSONObject("location");

                                            String[] columns = jsonObject.getString("name").split(",");
                                            user.setCity(columns[0]);
                                            user.setCountry(columns[1]);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        try {
                                            user.setEmail(response.getJSONObject().get("email").toString());

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        Geocoder geocoder = new Geocoder(SplashScreen.this, Locale.ITALY);

                                        try {
                                            List<Address> addresses = geocoder.getFromLocationName(user.getCity(), 1);

                                            user.setLatitude(addresses.get(0).getLatitude());
                                            user.setLongitude(addresses.get(0).getLongitude());

                                        } catch (Exception e) {
                                            e.printStackTrace();

                                            user.setCity(null);
                                            user.setCountry(null);
                                        }

                                        try {

                                            user.setUserID(Profile.getCurrentProfile().getId());
                                            user.setName(Profile.getCurrentProfile().getFirstName());
                                            user.setSurname(Profile.getCurrentProfile().getLastName());
                                            user.setImgURL(Profile.getCurrentProfile().getProfilePictureUri(300, 300).toString());
                                            user.setCredits(20);
                                            manageUser.saveUser(user);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        Intent askLocationIntent = new Intent(SplashScreen.this, MapsActivity.class);
                                        startActivity(askLocationIntent);

                                    }
                                });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "location,email");
                        request.setParameters(parameters);
                        request.executeAsync();

                    } else {

                        new LoadBooks(new OnBooksLoadingCompleted() {
                            @Override
                            public void onBooksLoadingCompleted(ArrayList<Book> myBooks, ArrayList<Book> recBooks) {

                                manageUser.setBookCount(myBooks.size());
                                manageUser.setRecBookCount(recBooks.size());

                                try {

                                    InternalStorage.cacheObject(SplashScreen.this, MYBOOKS_KEY, myBooks);
                                    InternalStorage.cacheObject(SplashScreen.this, RECBOOKS_KEY, recBooks);

                                } catch (IOException ignored) {

                                }

                            }
                        }).execute(user.getUserID());

                        manageUser.saveUser(user);
                        redirectToHome();

                    }
                }

            }).execute(AccessToken.getCurrentAccessToken().getUserId());


        } else {

            new LoadBooks(new OnBooksLoadingCompleted() {
                @Override
                public void onBooksLoadingCompleted(ArrayList<Book> myBooks, ArrayList<Book> recBooks) {

                    manageUser.setBookCount(myBooks.size());
                    manageUser.setRecBookCount(recBooks.size());

                    try {

                        InternalStorage.cacheObject(SplashScreen.this, MYBOOKS_KEY, myBooks);
                        InternalStorage.cacheObject(SplashScreen.this, RECBOOKS_KEY, recBooks);

                    } catch (IOException ignored) {

                    }

                }
            }).execute(user.getUserID());
            redirectToHome();
        }

    }

    private void redirectToHome() {

        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(i);

        finish();

    }

    public interface OnUserLoadingCompleted {
        void onUserLoadingCompleted();
    }

    class LoadUser extends AsyncTask<String, Void, Void> {
        private OnUserLoadingCompleted listener;

        public LoadUser(OnUserLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(String... params) {

            user = new DynamoDBManager(SplashScreen.this).getUser(params[0]);
            listener.onUserLoadingCompleted();

            return null;
        }

    }

    public interface OnBooksLoadingCompleted {
        void onBooksLoadingCompleted(ArrayList<Book> myBooks, ArrayList<Book> recBooks);
    }


    class LoadBooks extends AsyncTask<String, Void, Void> {
        private OnBooksLoadingCompleted listener;

        public LoadBooks(OnBooksLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(String... params) {

            DynamoDBManager DDBM = new DynamoDBManager(SplashScreen.this);

            listener.onBooksLoadingCompleted(DDBM.getBooks(params[0]), DDBM.getReceivedBooks(params[0]));

            return null;
        }

    }

}