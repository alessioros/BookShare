package it.polimi.dima.bookshare.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.ViewPagerAdapter;
import it.polimi.dima.bookshare.fragments.MyBooksFragment;
import it.polimi.dima.bookshare.fragments.ReceivedBooksFragment;

public class LibraryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.library_title));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());

        adapter.addFragment(MyBooksFragment.newInstance(), getResources().getString(R.string.firsttab_name));
        adapter.addFragment(ReceivedBooksFragment.newInstance(), getResources().getString(R.string.secondtab_name));

        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            startActivity(new Intent(LibraryActivity.this, MainActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
