package it.polimi.dima.bookshare.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;

import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.SearchResultsAdapter;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG="SearchActivity";
    private RecyclerView recyclerView;
    private ArrayList<Book> searchResults=new ArrayList<>();

    public SearchActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.search_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            getSupportActionBar().setTitle("Books for: "+query);
            Log.i(TAG,query);
            DynamoDBManager DDBM=new DynamoDBManager(this);
            searchResults=DDBM.getBookListSearch(query);
            recyclerView.setAdapter(new SearchResultsAdapter(searchResults, this));

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            //startActivity(new Intent(SearchActivity.this,MainActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
