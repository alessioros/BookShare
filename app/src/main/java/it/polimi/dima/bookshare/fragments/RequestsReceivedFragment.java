package it.polimi.dima.bookshare.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.adapters.RequestsAdapter;
import it.polimi.dima.bookshare.amazon.Constants;
import it.polimi.dima.bookshare.amazon.DynamoDBManager;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerTask;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerType;
import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.tables.BookRequest;
import it.polimi.dima.bookshare.tables.User;
import it.polimi.dima.bookshare.utils.ManageUser;
import it.polimi.dima.bookshare.utils.OnBookRequestsLoadingCompleted;

/**
 * Created by matteo on 19/04/16.
 */
public class RequestsReceivedFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<BookRequest> bookRequests =new ArrayList<>();
    private RequestsAdapter requestsAdapter;

    public RequestsReceivedFragment() {
    }

    public static Fragment newInstance() {
        return new RequestsReceivedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_requests_received, container, false);

        loadRequests();

        final SwipeRefreshLayout mySwipeRefreshLayout=(SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_requests_received);

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        loadRequests();
                        mySwipeRefreshLayout.setRefreshing(false);

                    }
                }
        );

        return view;

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve result of scanning - instantiate ZXing object
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        Toast toast;

        //check we have a valid result
        if (scanningResult != null) {
            //get content from Intent Result
            final String scanContent = scanningResult.getContents();


            if (scanContent == null) {

                toast = Toast.makeText(getActivity(), getResources().getString(R.string.no_scandata), Toast.LENGTH_SHORT);
                toast.show();

            } else {

                String ownerID= PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("EXCHANGE_ID",null);
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove("EXCHANGE_ID").apply();

                for(BookRequest bookReq : bookRequests){
                    if(bookReq.getAskerID().equals(ownerID) && bookReq.getBookISBN().equals(scanContent)){

                        Book b=bookReq.getBook();
                        b.setReceiverID(null);
                        new DynamoDBManagerTask(getActivity(),b,bookReq).execute(DynamoDBManagerType.RETURN);
                        requestsAdapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), getResources().getString(R.string.return_confirmed), Toast.LENGTH_SHORT).show();

                    }
                }


            }

        } else {
            //invalid scan data or scan canceled
            toast = Toast.makeText(getActivity(), getResources().getString(R.string.no_scandata), Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public void loadRequests() {

        final ProgressDialog progressDialog =
                ProgressDialog.show(getActivity(),
                        getResources().getString(R.string.wait),
                        getResources().getString(R.string.loading_requests), true, false);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        try {

            new LoadRequests(new OnBookRequestsLoadingCompleted() {
                @Override
                public void onBookRequestsLoadingCompleted(ArrayList<BookRequest> bookRequests) {
                    loadRecyclerView(bookRequests);
                    progressDialog.dismiss();
                }

            }).execute();

        } catch (Exception e) {

            new Toast(getActivity()).makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadRecyclerView(ArrayList<BookRequest> mBookRequests) {

        TextView noBookRequests = (TextView) getActivity().findViewById(R.id.nobookrequests_text);

        if (mBookRequests.isEmpty()) {

            noBookRequests.setVisibility(View.VISIBLE);

            Typeface aller = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Aller_Rg.ttf");

            noBookRequests.setTypeface(aller);

            noBookRequests.setText(getResources().getString(R.string.nobook_requests));

        } else {

            recyclerView = (RecyclerView) getActivity().findViewById(R.id.requests_from_list);

            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            Collections.sort(bookRequests,Collections.reverseOrder(new BookRequestComparator()));
            requestsAdapter=new RequestsAdapter(bookRequests, getActivity(),this);
            recyclerView.setAdapter(requestsAdapter);

        }

    }

    class LoadRequests extends AsyncTask<Void, ArrayList<BookRequest>, ArrayList<BookRequest>> {
        private OnBookRequestsLoadingCompleted listener;

        public LoadRequests(OnBookRequestsLoadingCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<BookRequest> doInBackground(Void... params) {

            bookRequests =new DynamoDBManager(getActivity()).getReceivedRequests();
            for(BookRequest bookRequest : bookRequests){

                    bookRequest.setUser(new DynamoDBManager(getActivity()).getUser(bookRequest.getAskerID()));
                    bookRequest.setBook(new DynamoDBManager(getActivity()).getBook(bookRequest.getBookISBN(), bookRequest.getReceiverID()));

            }

            return bookRequests;
        }

        protected void onPostExecute(ArrayList<BookRequest> bookRequests) {

            listener.onBookRequestsLoadingCompleted(bookRequests);
        }
    }

    public class BookRequestComparator implements Comparator<BookRequest> {
        @Override
        public int compare(BookRequest br1, BookRequest br2) {
            return br1.getTime().compareTo(br2.getTime());
        }
    }
}
