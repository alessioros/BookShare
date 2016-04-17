package it.polimi.dima.bookshare.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.SearchResultsAdapter;
import it.polimi.dima.bookshare.utils.MySuggestionProvider;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG="SearchActivity";
    private RecyclerView recyclerView;
    private ArrayList<Book> searchResults=new ArrayList<>();
    private SearchResultsAdapter sra;

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

        FloatingActionButton fab=(FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(searchResults, new CustomComparator());
                sra.notifyDataSetChanged();
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {

        setIntent(intent);
        handleIntent(intent);

    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            getSupportActionBar().setTitle("Books for: "+query);
            Log.i(TAG,query);
            DynamoDBManager DDBM=new DynamoDBManager(this);
            searchResults=DDBM.getBookListSearch(query);
            for(Book book : searchResults){
                book.setOwner(new DynamoDBManager(this).getUser(book.getOwnerID()));
            }
            sra=new SearchResultsAdapter(searchResults,this);
            recyclerView.setAdapter(sra);

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public class CustomComparator implements Comparator<Book> {
        @Override
        public int compare(Book book1, Book book2) {
            return book1.getTitle().compareTo(book2.getTitle());
        }
    }

}
