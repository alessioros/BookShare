package it.polimi.dima.bookshare.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.RequestsAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerTask;
import it.polimi.dima.bookshare.tables.BookRequest;
import it.polimi.dima.bookshare.utils.OnBookRequestsLoadingCompleted;

public class RequestsActivity extends AppCompatActivity {

    private static final String TAG="RequestsActivity";
    private RecyclerView recyclerView;
    private ArrayList<BookRequest> bookRequests =new ArrayList<>();
    private RequestsAdapter requestsAdapter;

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

            requestsAdapter=new RequestsAdapter(bookRequests, this);
            recyclerView.setAdapter(requestsAdapter);

        }

    }

    class LoadRequests extends AsyncTask<Void, ArrayList<BookRequest>, ArrayList<BookRequest>> {
        private OnBookRequestsLoadingCompleted listener;

        public LoadRequests(OnBookRequestsLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<BookRequest> doInBackground(Void... params) {

            bookRequests =new DynamoDBManager(RequestsActivity.this).getMyBookRequests();
            bookRequests.addAll(new DynamoDBManager(RequestsActivity.this).getReceivedRequests());
            for(BookRequest bookRequest : bookRequests){
                if (bookRequest.getAskerID().equals(PreferenceManager.getDefaultSharedPreferences(RequestsActivity.this).getString("ID", null))) {

                    bookRequest.setUser(new DynamoDBManager(RequestsActivity.this).getUser(bookRequest.getReceiverID()));
                    bookRequest.setBook(new DynamoDBManager(RequestsActivity.this).getBook(bookRequest.getBookISBN(), bookRequest.getReceiverID()));

                } else {

                    bookRequest.setUser(new DynamoDBManager(RequestsActivity.this).getUser(bookRequest.getAskerID()));
                    bookRequest.setBook(new DynamoDBManager(RequestsActivity.this).getBook(bookRequest.getBookISBN(), bookRequest.getReceiverID()));
                }
            }

            return bookRequests;
        }

        protected void onPostExecute(ArrayList<BookRequest> bookRequests) {

            listener.onBookRequestsLoadingCompleted(bookRequests);
        }
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

                toast = Toast.makeText(this, getResources().getString(R.string.no_scandata), Toast.LENGTH_SHORT);
                toast.show();

            } else {

                String ownerID=PreferenceManager.getDefaultSharedPreferences(this).getString("EXCHANGE_ID",null);
                PreferenceManager.getDefaultSharedPreferences(this).edit().remove("EXCHANGE_ID").apply();

                for(BookRequest bookReq : bookRequests){
                    if(bookReq.getReceiverID().equals(ownerID) && bookReq.getBookISBN().equals(scanContent)){
                        bookReq.getBook().setReceiverID(PreferenceManager.getDefaultSharedPreferences(this).getString("ID",null));
                        new DynamoDBManager(this).updateBook(bookReq.getBook());
                        bookReq.setAccepted(3);
                        new DynamoDBManager(this).updateBookRequest(bookReq);
                        requestsAdapter.notifyDataSetChanged();
                        Toast.makeText(this, getResources().getString(R.string.exchange_confirmed), Toast.LENGTH_SHORT).show();
                    }
                }


            }

        } else {
            //invalid scan data or scan canceled
            toast = Toast.makeText(this, getResources().getString(R.string.no_scandata), Toast.LENGTH_SHORT);
            toast.show();
        }

    }

}
