package it.polimi.dima.bookshare.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import it.polimi.dima.bookshare.adapters.RequestsAdapter;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.SearchResultsAdapter;
import it.polimi.dima.bookshare.tables.BookRequest;
import it.polimi.dima.bookshare.utils.MySuggestionProvider;
import it.polimi.dima.bookshare.utils.OnBookRequestsLoadingCompleted;
import it.polimi.dima.bookshare.utils.OnSearchResultsLoadingCompleted;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG="SearchActivity";
    private RecyclerView recyclerView;
    private ArrayList<Book> searchResults=new ArrayList<>();
    private SearchResultsAdapter sra;
    private String query;

    public SearchActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

            query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            getSupportActionBar().setTitle("Books for: "+query);

            Log.i(TAG,query);
            loadSearchResults();
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

    public void loadSearchResults() {

        final ProgressDialog progressDialog =
                ProgressDialog.show(this,
                        getResources().getString(R.string.wait),
                        getResources().getString(R.string.loading_search_results), true, false);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        try {

            new LoadSearchResults(new OnSearchResultsLoadingCompleted() {
                @Override
                public void OnSearchResultsLoadingCompleted(ArrayList<Book> searchResults) {
                    loadRecyclerView(searchResults);
                    progressDialog.dismiss();
                }

            }).execute();

        } catch (Exception e) {

            new Toast(this).makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadRecyclerView(ArrayList<Book> booksSearched) {

        TextView noResults = (TextView) this.findViewById(R.id.nobooks_found_text);

        if (booksSearched.isEmpty()) {

            noResults.setVisibility(View.VISIBLE);

            Typeface aller = Typeface.createFromAsset(this.getAssets(), "fonts/Aller_Rg.ttf");

            noResults.setTypeface(aller);

            noResults.setText(getResources().getString(R.string.nobooks_for_that_title));

        } else {

            recyclerView = (RecyclerView) findViewById(R.id.search_list);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            sra=new SearchResultsAdapter(searchResults,this);
            recyclerView.setAdapter(sra);
        }

    }

    class LoadSearchResults extends AsyncTask<Void, ArrayList<Book>, ArrayList<Book>> {
        private OnSearchResultsLoadingCompleted listener;

        public LoadSearchResults(OnSearchResultsLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<Book> doInBackground(Void... params) {

            DynamoDBManager DDBM=new DynamoDBManager(SearchActivity.this);
            searchResults=DDBM.getBookListSearch(query);
            for(Book book : searchResults){
                book.setOwner(DDBM.getUser(book.getOwnerID()));
            }

            return searchResults;
        }

        protected void onPostExecute(ArrayList<Book> searchResults) {

            listener.OnSearchResultsLoadingCompleted(searchResults);
        }
    }

}
