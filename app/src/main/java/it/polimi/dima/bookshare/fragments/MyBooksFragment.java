package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.BookDetail;
import it.polimi.dima.bookshare.activities.VerticalOrientationCA;
import it.polimi.dima.bookshare.adapters.LibraryAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.utils.AmazonFinder;
import it.polimi.dima.bookshare.utils.GoogleBooksFinder;
import it.polimi.dima.bookshare.utils.InternalStorage;
import it.polimi.dima.bookshare.utils.LibraryThingFinder;
import it.polimi.dima.bookshare.utils.ManageUser;
import it.polimi.dima.bookshare.utils.MergeBookSources;
import it.polimi.dima.bookshare.utils.OnBookLoadingCompleted;

public class MyBooksFragment extends Fragment {

    private String GOOGLE_API = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    private Book amazonBook, googleBook, LTBook, book;
    private ArrayList<String> myBookIDs;
    private ManageUser manageUser;
    private String MYBOOKS_KEY = "MYBOOKS";

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

        manageUser = new ManageUser(getActivity());
        loadLibrary(view);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentIntentIntegrator scanIntegrator = new FragmentIntentIntegrator(MyBooksFragment.this);
                scanIntegrator.setCaptureActivity(VerticalOrientationCA.class);
                scanIntegrator.setPrompt(getResources().getString(R.string.scan_isbn));
                scanIntegrator.initiateScan();
            }
        });

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve result of scanning - instantiate ZXing object
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        Toast toast;

        //check we have a valid result
        if (scanningResult != null) {
            //get content from Intent Result
            final String scanContent = scanningResult.getContents();


            if (scanContent == null) {

                toast = Toast.makeText(getActivity(), getResources().getString(R.string.no_scandata), Toast.LENGTH_SHORT);
                toast.show();

            } else if (myBookIDs.contains(scanContent)) {

                toast = Toast.makeText(getActivity(), getResources().getString(R.string.book_alr_added), Toast.LENGTH_SHORT);
                toast.show();

            } else {

                toast = Toast.makeText(getActivity(), "ISBN " + scanContent + " " + getResources().getString(R.string.found), Toast.LENGTH_SHORT);
                toast.show();

                final ProgressDialog progressDialog =
                        ProgressDialog.show(getActivity(),
                                getResources().getString(R.string.wait),
                                getResources().getString(R.string.search_for_books), true, false);

                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                new SearchForBooks(new OnBookSearchCompleted() {
                    @Override
                    public void onBookSearchCompleted() {

                        progressDialog.dismiss();
                        if (book != null) {

                            Intent bookIntent = new Intent(getActivity(), BookDetail.class);
                            bookIntent.putExtra("book", (Parcelable) book);
                            bookIntent.putExtra("button", "add");
                            getActivity().startActivity(bookIntent);

                        } else {

                            Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.booksfinder_error), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }).execute(scanContent);

            }

        } else {
            //invalid scan data or scan canceled
            toast = Toast.makeText(getActivity(), getResources().getString(R.string.no_scandata), Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public void loadLibrary(View view) {

        ArrayList<Book> myBooks;
        final View mView = view;

        try {

            myBooks = (ArrayList<Book>) InternalStorage.readObject(getActivity(), MYBOOKS_KEY);

        } catch (IOException | ClassNotFoundException e) {
            myBooks = null;
        }

        if (myBooks == null) {

            final ProgressDialog progressDialog =
                    ProgressDialog.show(getActivity(),
                            getResources().getString(R.string.wait),
                            getResources().getString(R.string.loading_library), true, false);

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            try {

                new LoadBooks(new OnBookLoadingCompleted() {
                    @Override
                    public void onBookLoadingCompleted(ArrayList<Book> books) {

                        loadRecyclerView(books, mView);

                        try {
                            InternalStorage.cacheObject(getActivity(), MYBOOKS_KEY, books);

                        } catch (IOException e) {
                            System.out.println("Error while caching objects");
                        }

                        progressDialog.dismiss();

                    }
                }).execute();

            } catch (Exception e) {

                Toast.makeText(getActivity(), getResources().getString(R.string.error_loading_library), Toast.LENGTH_SHORT).show();
            }

        } else {

            loadRecyclerView(myBooks, mView);
        }

    }

    public void loadRecyclerView(ArrayList<Book> mBooks, View mView) {

        TextView noBooks = (TextView) mView.findViewById(R.id.nobooks_text);
        myBookIDs = new ArrayList<>();

        if (mBooks.isEmpty()) {

            noBooks.setVisibility(View.VISIBLE);

            Typeface aller = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Aller_Rg.ttf");

            noBooks.setTypeface(aller);

            noBooks.setText(getResources().getString(R.string.nobooks_inserted));
            manageUser.setBookCount(0);

        } else {

            for (Book book : mBooks) {

                myBookIDs.add(book.getIsbn());
            }

            manageUser.setBookCount(mBooks.size());

            noBooks.setVisibility(View.GONE);

            RecyclerView recyclerView = (RecyclerView) mView.findViewById(R.id.book_list);

            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);

            recyclerView.setLayoutManager(gridLayoutManager);

            recyclerView.setAdapter(new LibraryAdapter(mBooks, getActivity(), false, false));
        }

    }

    class LoadBooks extends AsyncTask<Void, ArrayList<Book>, ArrayList<Book>> {
        private OnBookLoadingCompleted listener;

        public LoadBooks(OnBookLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<Book> doInBackground(Void... params) {

            DynamoDBManager DDBM = new DynamoDBManager(getActivity());

            return DDBM.getBooks(new ManageUser(getActivity()).getUser().getUserID());
        }

        protected void onPostExecute(ArrayList<Book> books) {

            listener.onBookLoadingCompleted(books);
        }
    }

    class SearchForBooks extends AsyncTask<String, Void, Void> {
        private OnBookSearchCompleted listener;

        public SearchForBooks(OnBookSearchCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(String... isbn) {

            book = new Book();
            // Try to find book info on Amazon, Google Books and LibraryThings
            amazonBook = new AmazonFinder().getBook(isbn[0]);
            LTBook = new LibraryThingFinder(getActivity()).getBook(isbn[0]);
            googleBook = new Book();
            String url = GOOGLE_API + isbn[0];

            // Make AsyncRequest to Google Books
            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    googleBook = new GoogleBooksFinder().findBook(response, googleBook);

                    //  Mix info from different sources
                    //  Priority : Amazon -> Google -> LibraryThing
                    book = new MergeBookSources().mergeBooks(amazonBook, googleBook, LTBook);

                    SearchForBooks.this.onPostExecute();
                }

            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    //  Google failed mix info from different sources
                    book = new MergeBookSources().mergeBooks(amazonBook, null, LTBook);
                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
            requestQueue.add(jsObjRequest);

            return null;
        }

        protected void onPostExecute() {

            listener.onBookSearchCompleted();
        }
    }

    public interface OnBookSearchCompleted {
        void onBookSearchCompleted();
    }

}
