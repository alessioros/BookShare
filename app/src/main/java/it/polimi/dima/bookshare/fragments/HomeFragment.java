package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import it.polimi.dima.bookshare.FragmentIntentIntegrator;
import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.BookActivity;
import it.polimi.dima.bookshare.activities.VerticalOrientationCA;

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

        Button scanB = (Button) view.findViewById(R.id.scan_button);

        scanB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentIntentIntegrator scanIntegrator = new FragmentIntentIntegrator(HomeFragment.this);
                scanIntegrator.setCaptureActivity(VerticalOrientationCA.class);
                scanIntegrator.setPrompt("Scan an ISBN");
                scanIntegrator.initiateScan();
            }
        });

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve result of scanning - instantiate ZXing object
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        //check we have a valid result
        if (scanningResult != null) {
            //get content from Intent Result
            final String scanContent = scanningResult.getContents();

            Toast toast = Toast.makeText(getActivity(), "ISBN " + scanContent + " founded", Toast.LENGTH_SHORT);
            toast.show();

            String url = "https://www.googleapis.com/books/v1/volumes?" + "q=isbn:" + scanContent + "&key=AIzaSyB7cvzVLJ1GLM7fqmoHNvYrkt4EAGR_sCA";

            final JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    Intent bookIntent = new Intent(getActivity(), BookActivity.class);
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
                        getActivity().startActivity(bookIntent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(getActivity(),
                                "No book founded", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {


                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
            requestQueue.add(jsObjRequest);


        } else {
            //invalid scan data or scan canceled
            Toast toast = Toast.makeText(getActivity(),
                    "No book scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}
