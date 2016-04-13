package it.polimi.dima.bookshare.amazon;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.regions.Regions;

import java.util.HashMap;
import java.util.Map;

public class CognitoSyncClientManager {

    private static final String IDENTITY_POOL_ID = "eu-west-1:cbf27841-7455-4e20-91dc-1cd49cc55558";
    private static final Regions REGION = Regions.EU_WEST_1;

    protected static CognitoCachingCredentialsProvider credentialsProvider = null;


    public static void init(Context context) {

        credentialsProvider = new CognitoCachingCredentialsProvider(context, IDENTITY_POOL_ID, REGION);
    }

    public static void addLogins(String providerName, String token) {

        Map<String, String> logins = credentialsProvider.getLogins();
        if (logins == null) {
            logins = new HashMap<String, String>();
        }
        logins.put(providerName, token);
        credentialsProvider.withLogins(logins);
    }

    public static CognitoCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }
}
