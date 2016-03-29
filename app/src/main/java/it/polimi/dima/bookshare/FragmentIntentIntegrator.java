package it.polimi.dima.bookshare;

import android.app.Fragment;
import android.content.Intent;

import com.google.zxing.integration.android.IntentIntegrator;

/**
 * Created by matteo on 29/03/16.
 */
public final class FragmentIntentIntegrator extends IntentIntegrator {

    private final Fragment fragment;

    public FragmentIntentIntegrator(Fragment fragment) {
        super(fragment.getActivity());
        this.fragment = fragment;
    }

    @Override
    protected void startActivityForResult(Intent intent, int code) {
        fragment.startActivityForResult(intent, code);
    }
}