package it.polimi.dima.bookshare.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
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

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.CognitoSyncClientManager;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerTask;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerType;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;

public class SplashScreen extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;
    private User user;
    private ManageUser manageUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        /**
         * Initializes the sync client. This must be call before you can use it.
         */

        try {

            manageUser = new ManageUser(SplashScreen.this);
            user = manageUser.getUser();

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (user == null) {

            try {

                user = new DynamoDBManager(SplashScreen.this).getUser(AccessToken.getCurrentAccessToken().getUserId());


            } catch (Exception e) {
                e.printStackTrace();
            }

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
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "location");
                request.setParameters(parameters);
                request.executeAndWait();

                try {

                    user.setUserID(Profile.getCurrentProfile().getId());
                    user.setName(Profile.getCurrentProfile().getFirstName());
                    user.setSurname(Profile.getCurrentProfile().getLastName());
                    user.setImgURL(Profile.getCurrentProfile().getProfilePictureUri(300, 300).toString());
                    user.setCredits(20);
                    new DynamoDBManagerTask(SplashScreen.this, user).execute(DynamoDBManagerType.INSERT_USER);
                    manageUser.saveUser(user);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent askLocationIntent = new Intent(SplashScreen.this, MapsActivity.class);
                startActivity(askLocationIntent);

            } else {

                manageUser.saveUser(user);
                //redirectToHome();
                Intent askLocationIntent = new Intent(SplashScreen.this, MapsActivity.class);
                startActivity(askLocationIntent);
            }

        } else {

            //redirectToHome();
            Intent askLocationIntent = new Intent(SplashScreen.this, MapsActivity.class);
            startActivity(askLocationIntent);
        }



    }

    public void redirectToHome() {

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);

                finish();
            }
        }, SPLASH_TIME_OUT);

    }

}