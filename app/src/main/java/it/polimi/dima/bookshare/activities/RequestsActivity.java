package it.polimi.dima.bookshare.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.RequestsAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.BookRequest;

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

        recyclerView = (RecyclerView) findViewById(R.id.requests_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchResults=new DynamoDBManager(this).getMyBookRequests();
        searchResults.addAll(new DynamoDBManager(this).getReceivedRequests());
        for(BookRequest bookRequest : searchResults){
            if (bookRequest.getAskerID().equals(PreferenceManager.getDefaultSharedPreferences(this).getString("ID", null))) {

                bookRequest.setUser(new DynamoDBManager(this).getUser(bookRequest.getReceiverID()));
                bookRequest.setBook(new DynamoDBManager(this).getBook(bookRequest.getBookISBN(), bookRequest.getReceiverID()));

            } else {

                bookRequest.setUser(new DynamoDBManager(this).getUser(bookRequest.getAskerID()));
                bookRequest.setBook(new DynamoDBManager(this).getBook(bookRequest.getBookISBN(), bookRequest.getAskerID()));

            }
        }
        recyclerView.setAdapter(new RequestsAdapter(searchResults, this));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
