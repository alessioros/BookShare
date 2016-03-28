package it.polimi.dima.bookshare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.polimi.dima.bookshare.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("BookShare");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button scanB = (Button) findViewById(R.id.scan_button);

        scanB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
                scanIntegrator.setCaptureActivity(VerticalOrientationCA.class);
                scanIntegrator.setPrompt("Scan an ISBN");
                scanIntegrator.initiateScan();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve result of scanning - instantiate ZXing object
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        //check we have a valid result
        if (scanningResult != null) {
            //get content from Intent Result
            final String scanContent = scanningResult.getContents();

            Toast toast = Toast.makeText(getApplicationContext(),
                    "ISBN " + scanContent + " founded", Toast.LENGTH_SHORT);
            toast.show();

            String url = "https://www.googleapis.com/books/v1/volumes?" +
                    "q=isbn:" + scanContent + "&key=AIzaSyB7cvzVLJ1GLM7fqmoHNvYrkt4EAGR_sCA";

            final JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    Intent bookIntent = new Intent(MainActivity.this, BookActivity.class);
                    bookIntent.putExtra("ISBN", scanContent);

                    try {


                        JSONArray jArray = response.getJSONArray("items");

                        for (int i = 0; i < jArray.length(); i++) {

                            JSONObject volumeInfo = jArray.getJSONObject(i).getJSONObject("volumeInfo");

                            try {
                                JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");

                                String imgURL = imageLinks.getString("thumbnail");

                                bookIntent.putExtra("imgURL", imgURL);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                JSONObject saleInfo = volumeInfo.getJSONObject("saleInfo");

                                JSONObject listPrice = saleInfo.getJSONObject("listPrice");

                                Float price = Float.parseFloat(listPrice.getString("amount"));

                                bookIntent.putExtra("price", price);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                String title = new String(volumeInfo.getString("title").getBytes("ISO-8859-1"), "UTF-8");
                                bookIntent.putExtra("title", title);

                            } catch (Exception e) {

                                e.printStackTrace();
                            }

                            try {

                                int pageCount = Integer.parseInt(volumeInfo.getString("pageCount"));

                                bookIntent.putExtra("pageCount", pageCount);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                JSONArray authors = volumeInfo.getJSONArray("authors");
                                bookIntent.putExtra("numAuth", authors.length());

                                for (int j = 0; j < authors.length(); j++) {

                                    String author = new String(authors.getString(i).getBytes("ISO-8859-1"), "UTF-8");
                                    bookIntent.putExtra("author" + j, author);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                String publisher = volumeInfo.getString("publisher");
                                bookIntent.putExtra("publisher", new String(publisher.getBytes("ISO-8859-1"), "UTF-8"));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                String publishedDate = volumeInfo.getString("publishedDate");
                                bookIntent.putExtra("publishedDate", publishedDate);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {

                                String description = volumeInfo.getString("description");
                                bookIntent.putExtra("description", new String(description.getBytes("ISO-8859-1"), "UTF-8"));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                        startActivity(bookIntent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "No book founded", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {


                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(jsObjRequest);


        } else {
            //invalid scan data or scan canceled
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No book scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}
