package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.BookDetail;
import it.polimi.dima.bookshare.activities.VerticalOrientationCA;
import it.polimi.dima.bookshare.adapters.LibraryAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Book;

public class MyBooksFragment extends Fragment {

    public MyBooksFragment() {

    }

    public static Fragment newInstance() {

        return new MyBooksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mybook_list, container, false);

        loadLibrary();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentIntentIntegrator scanIntegrator = new FragmentIntentIntegrator(MyBooksFragment.this);
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

            String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + scanContent;

            final JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    Intent bookIntent = new Intent(getActivity(), BookDetail.class);
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

    public void loadLibrary() {

        final ProgressDialog progressDialog =
                ProgressDialog.show(getActivity(),
                        getResources().getString(R.string.wait),
                        getResources().getString(R.string.loading_library), true, false);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        try {

            new LoadBooks(new OnLoadingCompleted() {
                @Override
                public void onLoadingCompleted(ArrayList<Book> books) {

                    loadRecyclerView(books);
                    progressDialog.dismiss();

                }
            }).execute();

        } catch (Exception e) {

            new Toast(getActivity()).makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadRecyclerView(ArrayList<Book> mBooks) {

        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.book_list);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);

        recyclerView.setLayoutManager(gridLayoutManager);

        TextView noBooks = (TextView) getActivity().findViewById(R.id.nobooks_text);

        Typeface aller = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Aller_Rg.ttf");

        noBooks.setTypeface(aller);

        if (mBooks.isEmpty()) {

            noBooks.setVisibility(View.VISIBLE);
            noBooks.setText(getResources().getString(R.string.nobooks_inserted));

        } else {

            noBooks.setVisibility(View.GONE);
        }

        recyclerView.setAdapter(new LibraryAdapter(mBooks, getActivity()));


    }

    public interface OnLoadingCompleted {
        void onLoadingCompleted(ArrayList<Book> books);
    }

    public class LoadBooks extends AsyncTask<Void, ArrayList<Book>, ArrayList<Book>> {
        private OnLoadingCompleted listener;

        public LoadBooks(OnLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<Book> doInBackground(Void... params) {

            DynamoDBManager DDBM = new DynamoDBManager(getActivity());
            ArrayList<Book> mBooks = DDBM.getBooks(Profile.getCurrentProfile().getId());

            return mBooks;
        }

        protected void onPostExecute(ArrayList<Book> books) {

            listener.onLoadingCompleted(books);
        }
    }
}
