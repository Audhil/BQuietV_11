package com.wordpress.smdaudhilbe.bquiet.map;

import android.content.Context;
//import android.content.res.Resources;

import com.google.android.gms.common.ConnectionResult;
//import com.wordpress.smdaudhilbe.bquiet.R;

/**
 * Map error codes to error messages.
 */
public class LocationServiceErrorMessages {
	
    // Don't allow instantiation
    private LocationServiceErrorMessages () {}

    public static String getErrorString(Context context, int errorCode) {

        // Get a handle to resources, to allow the method to retrieve messages.
//        Resources mResources = context.getResources();

        // Define a string to contain the error message
        String errorString;

        // Decide which error message to get, based on the error code.
        switch (errorCode) {

            case ConnectionResult.DEVELOPER_ERROR:
                errorString = "The application is misconfigured.";//mResources.getString(R.string.connection_error_misconfigured);
                break;

            case ConnectionResult.INTERNAL_ERROR:
                errorString = "An internal error occurred. Retrying should resolve the problem";//mResources.getString(R.string.connection_error_internal);
                break;

            case ConnectionResult.INVALID_ACCOUNT:
                errorString = "The client attempted to connect to the service with an invalid account name specified";//mResources.getString(R.string.connection_error_invalid_account);
                break;

            case ConnectionResult.LICENSE_CHECK_FAILED:
                errorString = "The application is not licensed to the user. This error is not recoverable and will be treated as fatal";//mResources.getString(R.string.connection_error_license_check_failed);
                break;

            case ConnectionResult.NETWORK_ERROR:
                errorString = "A network error occurred. Retrying should resolve the problem";//mResources.getString(R.string.connection_error_network);
                break;

            case ConnectionResult.RESOLUTION_REQUIRED:
                errorString = "Completing the connection requires some form of resolution";//mResources.getString(R.string.connection_error_needs_resolution);
                break;

            case ConnectionResult.SERVICE_DISABLED:
                errorString = "The installed version of Google Play services has been disabled on this device";//mResources.getString(R.string.connection_error_disabled);
                break;

            case ConnectionResult.SERVICE_INVALID:
                errorString = "The version of the Google Play services installed on this device is not authentic";//mResources.getString(R.string.connection_error_invalid);
                break;

            case ConnectionResult.SERVICE_MISSING:
                errorString = "Google Play services is missing on this device";//mResources.getString(R.string.connection_error_missing);
                break;

            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                errorString = "The installed version of Google Play services is out of date";//mResources.getString(R.string.connection_error_outdated);
                break;

            case ConnectionResult.SIGN_IN_REQUIRED:
                errorString = "The client attempted to connect to the service but the user is not signed in";//mResources.getString(R.string.connection_error_sign_in_required);
                break;

            default:
                errorString = "Connection Error unknown";//mResources.getString(R.string.connection_error_unknown);
                break;
        }
        // Return the error message
        return errorString;
    }
}