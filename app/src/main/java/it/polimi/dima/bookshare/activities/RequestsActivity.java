package it.polimi.dima.bookshare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.ViewPagerAdapter;
import it.polimi.dima.bookshare.amazon.CognitoSyncClientManager;
import it.polimi.dima.bookshare.fragments.RequestsReceivedFragment;
import it.polimi.dima.bookshare.fragments.RequestsSentFragment;

public class RequestsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static final String TAG = "RequestsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.title_activity_requests));
        setSupportActionBar(toolbar);


        if(CognitoSyncClientManager.getCredentialsProvider()==null){
            CognitoSyncClientManager.init(this);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        if (getIntent().hasExtra("tab"))
            viewPager.setCurrentItem(getIntent().getExtras().getInt("tab"));

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());

        adapter.addFragment(RequestsSentFragment.newInstance(), getResources().getString(R.string.tab_sent));
        adapter.addFragment(RequestsReceivedFragment.newInstance(), getResources().getString(R.string.tab_received));

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
