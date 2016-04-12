package it.polimi.dima.bookshare.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.MainActivity;
import it.polimi.dima.bookshare.activities.MapsActivity;

public class SettingsActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getResources().getString(R.string.settings_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RelativeLayout locationHeader = (RelativeLayout) findViewById(R.id.settings_location);

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

            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
