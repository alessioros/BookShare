package it.polimi.dima.bookshare.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

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

        searchResults=new DynamoDBManager(this).getBookRequest(PreferenceManager.getDefaultSharedPreferences(this).getString("ID",null));
        recyclerView.setAdapter(new RequestsAdapter(searchResults, this));

    }

}
