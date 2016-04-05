package it.polimi.dima.bookshare.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.CognitoSyncClientManager;
import it.polimi.dima.bookshare.amazon.Constants;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class RegistrationIntentService extends IntentService {

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    private static final String ROLE_ARN = "arn:aws:sns:eu-west-1:133731989228:app/GCM/Bookshare";
    private static final Regions REGION = Regions.EU_WEST_1;

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"Bookshare"};
    private static String token;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);


            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer();

            // Subscribe to topic channels
            //subscribeTopics();

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * This method give us a call so as to get the tokenID of the phone's app
     */
    protected static String getToken(){
        return token;
    }


    /**
     * Persist registration to third-party servers.
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     */

    private void sendRegistrationToServer() {
        // initialize a credentials provider with your Activity’s context
        AmazonSNSClient client= new AmazonSNSClient(CognitoSyncClientManager.getCredentialsProvider());

        client.setRegion(Region.getRegion(REGION));//VERY VERY IMPORTANT

        String customPushData = "It's in DB";//Custom id of the registered person

        CreatePlatformEndpointRequest cpeReq =
                new CreatePlatformEndpointRequest()
                        .withCustomUserData(customPushData)
                        .withPlatformApplicationArn(ROLE_ARN)
                        .withToken(token);

        CreatePlatformEndpointResult cpeRes = client.createPlatformEndpoint(cpeReq);
        client.createPlatformEndpoint(cpeReq).withEndpointArn(cpeRes.getEndpointArn());


    }



    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics() throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

}
