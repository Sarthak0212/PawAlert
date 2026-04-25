package com.example.pawalert.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Handles incoming Firebase Cloud Messaging (FCM) push notifications.
 *
 * This service is automatically invoked by FCM when:
 *   1. A new FCM registration token is generated (onNewToken)
 *   2. A push notification message is received (onMessageReceived)
 *
 * Register this service in AndroidManifest.xml (already done).
 */
public class PawAlertMessagingService extends FirebaseMessagingService {

    private static final String TAG = "PawAlertFCM";

    /**
     * Called when a new FCM token is generated.
     * Save this token to Firestore under the user's document so the server
     * can send targeted notifications to this device.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM Token: " + token);

        // TODO: Save this token to Firestore
        // Example:
        // FirebaseHelper.getInstance().getFirestore()
        //     .collection("users")
        //     .document(userId)
        //     .update("fcmToken", token);
    }

    /**
     * Called when a push notification is received while the app is in the foreground.
     * Build a local notification here to alert the user.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Handle data payload
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Data payload: " + remoteMessage.getData());
            // TODO: Parse and act on the data (e.g., show a notification, update UI)
        }

        // Handle notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification: " + title + " - " + body);
            // TODO: Show a heads-up notification using NotificationManager
        }
    }
}
