package com.example.pawalert.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * General-purpose utility methods for PawAlert.
 * Add helper functions here as the app grows.
 */
public class AppUtils {

    private AppUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Shows a short Toast message.
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows a long Toast message.
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Checks whether the device has an active internet connection.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    /**
     * Validates that an email address is in a basic valid format.
     */
    public static boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validates that a phone number has at least 10 digits.
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.replaceAll("[^0-9]", "").length() >= 10;
    }
}
