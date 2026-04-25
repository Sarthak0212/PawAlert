package com.example.pawalert.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Centralized helper for Firebase operations.
 *
 * Usage:
 *   FirebaseHelper helper = FirebaseHelper.getInstance();
 *   FirebaseAuth auth = helper.getAuth();
 *   FirebaseFirestore db = helper.getFirestore();
 */
public class FirebaseHelper {

    private static FirebaseHelper instance;

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    private FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    /** Returns the singleton instance of FirebaseHelper. */
    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    /** Returns the FirebaseAuth instance. */
    public FirebaseAuth getAuth() {
        return auth;
    }

    /** Returns the Cloud Firestore instance. */
    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    /**
     * Returns the currently signed-in Firebase user, or null if not signed in.
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Returns true if a user is currently signed in.
     */
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    /**
     * Signs out the current user.
     */
    public void signOut() {
        auth.signOut();
    }
}
