package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.MyBookDetail;
import it.polimi.dima.bookshare.activities.VerticalOrientationCA;
import it.polimi.dima.bookshare.adapters.LibraryAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Book;

public class LibraryFragment extends Fragment {

    public LibraryFragment() {

    }

    public static Fragment newInstance() {

        return new LibraryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_library_list, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.book_list);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);

        recyclerView.setLayoutManager(gridLayoutManager);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentIntentIntegrator scanIntegrator = new FragmentIntentIntegrator(LibraryFragment.this);
                scanIntegrator.setCaptureActivity(VerticalOrientationCA.class);
                scanIntegrator.setPrompt("Scan an ISBN");
                scanIntegrator.initiateScan();
            }
        });

        // retrieve books

        DynamoDBManager DDBM = new DynamoDBManager(getActivity());
        ArrayList<Book> mBooks = DDBM.getBooks(Profile.getCurrentProfile().getId());

        recyclerView.setAdapter(new LibraryAdapter(mBooks, getActivity()));

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

                    Intent bookIntent = new Intent(getActivity(), MyBookDetail.class);
                    Book book = new Book();
                    book.setIsbn(scanContent);


                    try {


                        JSONArray jArray = response.getJSONArray("items");

                        for (int i = 0; i < jArray.length(); i++) {

                            JSONObject volumeInfo = jArray.getJSONObject(i).getJSONObject("volumeInfo");

                            // ----- BOOK COVER -----
                            try {
                                JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");

                                book.setImgURL(imageLinks.getString("thumbnail"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            // ----- TITLE -----
                            try {

                                book.setTitle(URLDecoder.decode(volumeInfo.getString("title"), "UTF-8"));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // ----- PAGE COUNT -----
                            try {
                                book.setPageCount(Integer.parseInt(volumeInfo.getString("pageCount")));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            // ----- AUTHORS -----
                            try {
                                JSONArray authors = volumeInfo.getJSONArray("authors");

                                for (int j = 0; j < authors.length(); j++) {

                                    book.setAuthor(URLDecoder.decode(authors.getString(i), "UTF-8"));
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // ----- PUBLISHER -----
                            try {
                                book.setPublisher(URLDecoder.decode(volumeInfo.getString("publisher"), "UTF-8"));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // ----- PUBLISHED DATE -----
                            try {
                                book.setPublishedDate(volumeInfo.getString("publishedDate"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            // ----- DESCRIPTION -----
                            try {
                                book.setDescription(URLDecoder.decode(volumeInfo.getString("description"), "UTF-8"));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                        bookIntent.putExtra("book", book);
                        bookIntent.putExtra("button", "add");
                        getActivity().startActivity(bookIntent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(getActivity(),
                                "Sorry, no book founded on Google Books", Toast.LENGTH_SHORT);
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
