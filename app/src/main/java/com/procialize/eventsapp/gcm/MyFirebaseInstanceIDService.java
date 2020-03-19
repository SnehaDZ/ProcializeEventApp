package com.procialize.eventsapp.gcm;

/**
 * Created by Rahul on 22-02-2018.
 */

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.procialize.eventsapp.ApiConstant.ApiConstant;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        // If you want to send messages to this application instance or // manage this apps subscriptions on the server side, send the // Instance ID token to your app server.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ApiConstant.FIREBASE_TOKEN = refreshedToken;
        preferences.edit().putString(ApiConstant.FIREBASE_TOKEN, refreshedToken).apply();
    }
}


