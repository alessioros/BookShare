package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.LibraryAdapter;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.utils.InternalStorage;
import it.polimi.dima.bookshare.utils.ManageUser;
import it.polimi.dima.bookshare.utils.OnBookLoadingCompleted;

public class ReceivedBooksFragment extends Fragment {

    private ManageUser manageUser;
    private String RECBOOKS_KEY = "RECBOOKS";

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
        loadReceivedLibrary(view);

        return view;
    }

    public void loadReceivedLibrary(View view) {

        ArrayList<Book> recBooks;
        final View mView = view;

        try {

            recBooks = (ArrayList<Book>) InternalStorage.readObject(getActivity(), RECBOOKS_KEY);

        } catch (IOException | ClassNotFoundException e) {
            recBooks = null;
        }

        if (recBooks == null) {

            final ProgressDialog progressDialog =
                    ProgressDialog.show(getActivity(),
                            getResources().getString(R.string.wait),
                            getResources().getString(R.string.loading_library), true, false);

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            try {

                new LoadReceivedBooks(new OnBookLoadingCompleted() {
                    @Override
                    public void onBookLoadingCompleted(ArrayList<Book> books) {

                        loadRecyclerView(books, mView);

                        try {

                            InternalStorage.cacheObject(getActivity(), RECBOOKS_KEY, books);

                        } catch (IOException e) {
                            System.out.println("Error while caching objects");
                        }

                        progressDialog.dismiss();

                    }
                }).execute();

            } catch (Exception e) {

                Toast.makeText(getActivity(), getResources().getString(R.string.error_loading_library), Toast.LENGTH_SHORT).show();
            }

        } else {

            loadRecyclerView(recBooks, mView);
        }

    }

    public void loadRecyclerView(ArrayList<Book> mBooks, View mView) {


        TextView noBooks = (TextView) mView.findViewById(R.id.recnobooks_text);

        if (mBooks.isEmpty()) {

            manageUser.setRecBookCount(0);
            noBooks.setVisibility(View.VISIBLE);

            Typeface aller = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Aller_Rg.ttf");

            noBooks.setTypeface(aller);

            noBooks.setText(getResources().getString(R.string.nobooks_received));

        } else {

            manageUser.setRecBookCount(mBooks.size());
            noBooks.setVisibility(View.GONE);

            RecyclerView recyclerView = (RecyclerView) mView.findViewById(R.id.recbook_list);

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

            return DDBM.getReceivedBooks(new ManageUser(getActivity()).getUser().getUserID());
        }

        protected void onPostExecute(ArrayList<Book> books) {

            listener.onBookLoadingCompleted(books);
        }
    }
}

