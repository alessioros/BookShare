package it.polimi.dima.bookshare.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.MapsActivity;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {

    }

    public static SettingsFragment newInstance() {

        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        RelativeLayout locationHeader = (RelativeLayout) view.findViewById(R.id.settings_location);

        locationHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), MapsActivity.class);
                i.putExtra("from_settings", "yes");
                getActivity().startActivity(i);
            }
        });

        return view;
    }

}
