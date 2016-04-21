package it.polimi.dima.bookshare.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.CognitoSyncClientManager;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

public class SplashScreen extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 500;
    private User user;
    private ManageUser manageUser;
    private final float DEFAULT_MAX_DIST = 200000f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        manageUser = new ManageUser(SplashScreen.this);

        try {

            user = manageUser.getUser();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (user == null || !manageUser.verifyRegistered()) {

            manageUser.setDistance(DEFAULT_MAX_DIST);
            new LoadUser(new OnUserLoadingCompleted() {
                @Override
                public void onUserLoadingCompleted() {

                    if (user == null) {

                        user = new User();

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

                        manageUser.saveUser(user);
                        redirectToHome();

                    }
                }

            }).execute(AccessToken.getCurrentAccessToken().getUserId());


        } else {

            redirectToHome();
        }

    }

    public void redirectToHome() {

        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        startActivity(i);

        finish();


        /*new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);

                finish();
            }
        }, SPLASH_TIME_OUT);*/

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

}