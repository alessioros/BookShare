package it.polimi.dima.bookshare.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.fragments.GeneralSettingsFragment;
import it.polimi.dima.bookshare.fragments.HomeFragment;

public class SettingsActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getResources().getString(R.string.settings_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final RelativeLayout settingsHeaders = (RelativeLayout) findViewById(R.id.settings_activity);
        RelativeLayout generalHeader = (RelativeLayout) findViewById(R.id.settings_general);
        RelativeLayout locationHeader = (RelativeLayout) findViewById(R.id.settings_location);

        generalHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                settingsHeaders.setVisibility(View.GONE);
                Fragment fragment = GeneralSettingsFragment.newInstance();

                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .addToBackStack("general_settings")
                        .commit();

                getSupportActionBar().setTitle(getResources().getString(R.string.general_sett_title));

            }
        });

        locationHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(SettingsActivity.this, MapsActivity.class);
                i.putExtra("from_settings", "yes");
                startActivity(i);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            if (getFragmentManager().findFragmentByTag("general_settings") != null) {

                getFragmentManager().popBackStackImmediate("general_settings", 0);
            } else {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                finish();
            }

        }

        return super.onOptionsItemSelected(item);
    }

}
