package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;

import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.LibraryAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.utils.ManageUser;
import it.polimi.dima.bookshare.utils.OnBookLoadingCompleted;

public class ReceivedBooksFragment extends Fragment {

    private ManageUser manageUser;

    public ReceivedBooksFragment() {

    }

    public static Fragment newInstance() {

        return new ReceivedBooksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_receivedbook_list, container, false);

        manageUser = new ManageUser(getActivity());
        loadReceivedLibrary();

        return view;
    }

    public void loadReceivedLibrary() {

        final ProgressDialog progressDialog =
                ProgressDialog.show(getActivity(),
                        getResources().getString(R.string.wait),
                        getResources().getString(R.string.loading_library), true, false);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        try {

            new LoadReceivedBooks(new OnBookLoadingCompleted() {
                @Override
                public void onBookLoadingCompleted(ArrayList<Book> books) {

                    loadRecyclerView(books);
                    progressDialog.dismiss();

                }
            }).execute();

        } catch (Exception e) {

            new Toast(getActivity()).makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadRecyclerView(ArrayList<Book> mBooks) {


        TextView noBooks = (TextView) getActivity().findViewById(R.id.recnobooks_text);

        if (mBooks.isEmpty()) {

            manageUser.setRecBookCount(0);
            noBooks.setVisibility(View.VISIBLE);

            Typeface aller = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Aller_Rg.ttf");

            noBooks.setTypeface(aller);

            noBooks.setText(getResources().getString(R.string.nobooks_received));

        } else {

            manageUser.setRecBookCount(mBooks.size());
            noBooks.setVisibility(View.GONE);

            RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recbook_list);

            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);

            recyclerView.setLayoutManager(gridLayoutManager);

            recyclerView.setAdapter(new LibraryAdapter(mBooks, getActivity(), false, true));
        }

    }

    class LoadReceivedBooks extends AsyncTask<Void, ArrayList<Book>, ArrayList<Book>> {
        private OnBookLoadingCompleted listener;

        public LoadReceivedBooks(OnBookLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<Book> doInBackground(Void... params) {

            DynamoDBManager DDBM = new DynamoDBManager(getActivity());
            ArrayList<Book> mBooks = DDBM.getReceivedBooks(new ManageUser(getActivity()).getUser().getUserID());

            return mBooks;
        }

        protected void onPostExecute(ArrayList<Book> books) {

            listener.onBookLoadingCompleted(books);
        }
    }
}

