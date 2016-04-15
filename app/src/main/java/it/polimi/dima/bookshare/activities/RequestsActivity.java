package it.polimi.dima.bookshare.activities;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.LibraryAdapter;
import it.polimi.dima.bookshare.adapters.RequestsAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.BookRequest;
import it.polimi.dima.bookshare.utils.ManageUser;
import it.polimi.dima.bookshare.utils.OnBookLoadingCompleted;
import it.polimi.dima.bookshare.utils.OnBookRequestsLoadingCompleted;

public class RequestsActivity extends AppCompatActivity {

    private static final String TAG="RequestsActivity";
    private RecyclerView recyclerView;
    private ArrayList<BookRequest> searchResults=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadRequests();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadRequests() {

        final ProgressDialog progressDialog =
                ProgressDialog.show(this,
                        getResources().getString(R.string.wait),
                        getResources().getString(R.string.loading_requests), true, false);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        try {

            new LoadRequests(new OnBookRequestsLoadingCompleted() {
                @Override
                public void onBookRequestsLoadingCompleted(ArrayList<BookRequest> bookRequests) {
                    loadRecyclerView(bookRequests);
                    progressDialog.dismiss();
                }

            }).execute();

        } catch (Exception e) {

            new Toast(this).makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadRecyclerView(ArrayList<BookRequest> mBookRequests) {

        TextView noBookRequests = (TextView) findViewById(R.id.nobookrequests_text);

        if (mBookRequests.isEmpty()) {

            noBookRequests.setVisibility(View.VISIBLE);

            Typeface aller = Typeface.createFromAsset(this.getAssets(), "fonts/Aller_Rg.ttf");

            noBookRequests.setTypeface(aller);

            noBookRequests.setText(getResources().getString(R.string.nobook_requests));

        } else {

            recyclerView = (RecyclerView) findViewById(R.id.requests_list);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            recyclerView.setAdapter(new RequestsAdapter(searchResults, this));

        }

    }

    class LoadRequests extends AsyncTask<Void, ArrayList<BookRequest>, ArrayList<BookRequest>> {
        private OnBookRequestsLoadingCompleted listener;

        public LoadRequests(OnBookRequestsLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<BookRequest> doInBackground(Void... params) {

            searchResults=new DynamoDBManager(RequestsActivity.this).getMyBookRequests();
            searchResults.addAll(new DynamoDBManager(RequestsActivity.this).getReceivedRequests());
            for(BookRequest bookRequest : searchResults){
                if (bookRequest.getAskerID().equals(PreferenceManager.getDefaultSharedPreferences(RequestsActivity.this).getString("ID", null))) {

                    bookRequest.setUser(new DynamoDBManager(RequestsActivity.this).getUser(bookRequest.getReceiverID()));
                    bookRequest.setBook(new DynamoDBManager(RequestsActivity.this).getBook(bookRequest.getBookISBN(), bookRequest.getReceiverID()));

                } else {

                    bookRequest.setUser(new DynamoDBManager(RequestsActivity.this).getUser(bookRequest.getAskerID()));
                    bookRequest.setBook(new DynamoDBManager(RequestsActivity.this).getBook(bookRequest.getBookISBN(), bookRequest.getAskerID()));

                }
            }

            return searchResults;
        }

        protected void onPostExecute(ArrayList<BookRequest> bookRequests) {

            listener.onBookRequestsLoadingCompleted(bookRequests);
        }
    }

}
