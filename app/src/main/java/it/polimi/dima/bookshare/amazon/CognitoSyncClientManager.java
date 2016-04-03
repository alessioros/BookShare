package it.polimi.dima.bookshare.amazon;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.regions.Regions;
import com.facebook.login.LoginResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by matteo on 25/03/16.
 */
public class CognitoSyncClientManager {

    private static final String TAG = "CognitoSyncClientManager";

    /**
     * Enter here the Identity Pool associated with your app and the AWS
     * region where it belongs. Get this information from the AWS console.
     */

    private static final String IDENTITY_POOL_ID = "eu-west-1:cbf27841-7455-4e20-91dc-1cd49cc55558";
    private static final Regions REGION = Regions.EU_WEST_1;

    private static CognitoSyncManager syncClient;
    protected static CognitoCachingCredentialsProvider credentialsProvider = null;


    /**
     * Initializes the Cognito Identity and Sync clients. This must be called before getInstance().
     *
     * @param context a context of the app
     */
    public static void init(Context context) {

        if (syncClient != null) return;

        credentialsProvider = new CognitoCachingCredentialsProvider(context, IDENTITY_POOL_ID, REGION);
        Log.i(TAG, "Developer authenticated identities is not configured");

        syncClient = new CognitoSyncManager(context, REGION, credentialsProvider);
    }

    /**
     * Sets the login so that you can use authorized identity. This requires a
     * network request, so you should call it in a background thread.
     *
     * @param providerName the name of the external identity provider
     * @param token openId token
     */
    public static void addLogins(String providerName, String token) {
        if (syncClient == null) {
            throw new IllegalStateException("CognitoSyncClientManager not initialized yet");
        }

        Map<String, String> logins = credentialsProvider.getLogins();
        if (logins == null) {
            logins = new HashMap<String, String>();
        }
        logins.put(providerName, token);
        credentialsProvider.setLogins(logins);
    }

    /**
     * Gets the singleton instance of the CognitoClient. init() must be called
     * prior to this.
     *
     * @return an instance of CognitoClient
     */
    public static CognitoSyncManager getInstance() {
        if (syncClient == null) {
            throw new IllegalStateException("CognitoSyncClientManager not initialized yet");
        }
        return syncClient;
    }

    /**
     * Returns a credentials provider object
     *
     * @return
     */
    public static CognitoCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }
}
