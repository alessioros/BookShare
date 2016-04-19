package it.polimi.dima.bookshare.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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
import it.polimi.dima.bookshare.adapters.ViewPagerAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerTask;
import it.polimi.dima.bookshare.fragments.MyBooksFragment;
import it.polimi.dima.bookshare.fragments.ReceivedBooksFragment;
import it.polimi.dima.bookshare.fragments.RequestsFromFragment;
import it.polimi.dima.bookshare.fragments.RequestsToFragment;
import it.polimi.dima.bookshare.tables.BookRequest;
import it.polimi.dima.bookshare.utils.OnBookRequestsLoadingCompleted;

public class RequestsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static final String TAG="RequestsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.title_activity_requests));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());

        adapter.addFragment(RequestsToFragment.newInstance(), getResources().getString(R.string.tab_sent));
        adapter.addFragment(RequestsFromFragment.newInstance(), getResources().getString(R.string.tab_received));

        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            startActivity(new Intent(RequestsActivity.this, MainActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
