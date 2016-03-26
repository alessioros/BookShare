package it.polimi.dima.bookshare.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

import it.polimi.dima.bookshare.DynamoDBManagerTask;
import it.polimi.dima.bookshare.DynamoDBManagerType;
import it.polimi.dima.bookshare.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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

        Button scanB = (Button) findViewById(R.id.scan_button);
        Button searchB = (Button) findViewById(R.id.search_button);

        scanB.setOnClickListener(this);
        Typeface aller = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");
        scanB.setTypeface(aller);
        searchB.setTypeface(aller);

        new DynamoDBManagerTask(MainActivity.this,null).execute(DynamoDBManagerType.CREATE_TABLE);

        new DynamoDBManagerTask(MainActivity.this,null).execute(DynamoDBManagerType.LIST_BOOKS);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve result of scanning - instantiate ZXing object
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        //check we have a valid result
        if (scanningResult != null) {
            //get content from Intent Result
            final String scanContent = scanningResult.getContents();
            //get format name of data scanned
            String scanFormat = scanningResult.getFormatName();

            Toast toast = Toast.makeText(getApplicationContext(),
                    "ISBN " + scanContent + " founded", Toast.LENGTH_SHORT);
            toast.show();

            String url = "https://www.googleapis.com/books/v1/volumes?" +
                    "q=isbn:" + scanContent + "&key=AIzaSyB7cvzVLJ1GLM7fqmoHNvYrkt4EAGR_sCA";

            final JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    Intent bookIntent = new Intent(MainActivity.this, BookActivity.class);
                    bookIntent.putExtra("ISBN",scanContent);

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


                            String title = volumeInfo.getString("title");
                            bookIntent.putExtra("title", title);

                            try {
                                JSONArray authors = volumeInfo.getJSONArray("authors");
                                bookIntent.putExtra("numAuth", authors.length());

                                for (int j = 0; j < authors.length(); j++) {

                                    String author = authors.getString(i);
                                    bookIntent.putExtra("author" + j, author);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                String publisher = volumeInfo.getString("publisher");
                                bookIntent.putExtra("publisher", publisher);

                            } catch (JSONException e) {
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
                                bookIntent.putExtra("description", description);

                            } catch (JSONException e) {
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

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.scan_button) {
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.setCaptureActivity(VerticalOrientationCA.class);
            scanIntegrator.setPrompt("Scan an ISBN");
            scanIntegrator.initiateScan();
        }
    }

}
