package com.wordpress.smdaudhilbe.bquiet.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.wordpress.smdaudhilbe.bquiet.MainActivity;

/**
 * Storage for GeoFence values, implemented in SharedPreferences.
 * For a production app, use a content provider that's synced to the
 * web or loads GeoFence data based on current location.
 */
public class SimpleGeoFenceStore {

    // The SharedPreferences object in which GeoFences are stored
    private final SharedPreferences mPrefs;

    // The name of the resulting SharedPreferences
    private static final String SHARED_PREFERENCE_NAME = MainActivity.class.getSimpleName();

    // Create the SharedPreferences storage with private access only
    public SimpleGeoFenceStore(Context context) {    	
        mPrefs = context.getSharedPreferences(SHARED_PREFERENCE_NAME,Context.MODE_PRIVATE);
    }

    /**
     * Returns a stored GeoFence by its id, or returns {@code null}
     * if it's not found.
     *
     * @param id The ID of a stored GeoFence
     * @return A GeoFence defined by its center and radius. See
     * {@link SimpleGeoFence}
     */
    public SimpleGeoFence getGeoFence(String id) {

        /*
         * Get the latitude for the GeoFence identified by id, or GeoFenceUtils.INVALID_VALUE
         * if it doesn't exist
         */
        double lat = mPrefs.getFloat(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_LATITUDE),GeoFenceUtils.INVALID_FLOAT_VALUE);

        /*
         * Get the longitude for the GeoFence identified by id, or
         * -999 if it doesn't exist
         */
        double lng = mPrefs.getFloat(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_LONGITUDE),GeoFenceUtils.INVALID_FLOAT_VALUE);

        /*
         * Get the radius for the GeoFence identified by id, or GeoFenceUtils.INVALID_VALUE
         * if it doesn't exist
         */
        float radius = mPrefs.getFloat(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_RADIUS),GeoFenceUtils.INVALID_FLOAT_VALUE);

        /*
         * Get the expiration duration for the GeoFence identified by
         * id, or GeoFenceUtils.INVALID_VALUE if it doesn't exist
         */
        long expirationDuration = mPrefs.getLong(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_EXPIRATION_DURATION),GeoFenceUtils.INVALID_LONG_VALUE);

        /*
         * Get the transition type for the GeoFence identified by
         * id, or GeoFenceUtils.INVALID_VALUE if it doesn't exist
         */
        int transitionType = mPrefs.getInt(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_TRANSITION_TYPE),GeoFenceUtils.INVALID_INT_VALUE);

        // If none of the values is incorrect, return the object
        if (lat != GeoFenceUtils.INVALID_FLOAT_VALUE && lng != GeoFenceUtils.INVALID_FLOAT_VALUE 
        		&& radius != GeoFenceUtils.INVALID_FLOAT_VALUE && expirationDuration != GeoFenceUtils.INVALID_LONG_VALUE 
        		&& transitionType != GeoFenceUtils.INVALID_INT_VALUE) {

            // Return a true GeoFence object
            return new SimpleGeoFence(id, lat, lng, radius, expirationDuration, transitionType);

        // Otherwise, return null.
        } else {
            return null;
        }
    }

    /**
     * Save a GeoFence.

     * @param GeoFence The {@link SimpleGeoFence} containing the
     * values you want to save in SharedPreferences
     */
    public void setGeoFence(String id, SimpleGeoFence GeoFence) {

        /*
         * Get a SharedPreferences editor instance. Among other
         * things, SharedPreferences ensures that updates are atomic
         * and non-concurrent
         */
    	
        Editor editor = mPrefs.edit();

        // Write the GeoFence values to SharedPreferences
        editor.putFloat(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_LATITUDE),(float) GeoFence.getLatitude());

        editor.putFloat(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_LONGITUDE),(float) GeoFence.getLongitude());

        editor.putFloat(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_RADIUS),GeoFence.getRadius());

        editor.putLong(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_EXPIRATION_DURATION),GeoFence.getExpirationDuration());

        editor.putInt(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_TRANSITION_TYPE),GeoFence.getTransitionType());

        // Commit the changes
        editor.commit();
    }

    public void clearGeoFence(String id) {

        // Remove a flattened GeoFence object from storage by removing all of its keys
        Editor editor = mPrefs.edit();
        
        editor.remove(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_LATITUDE));
        editor.remove(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_LONGITUDE));
        editor.remove(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_RADIUS));
        editor.remove(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_EXPIRATION_DURATION));
        editor.remove(getGeoFenceFieldKey(id, GeoFenceUtils.KEY_TRANSITION_TYPE));
        
        editor.commit();
    }

    /**
     * Given a GeoFence object's ID and the name of a field
     * (for example, GeoFenceUtils.KEY_LATITUDE), return the key name of the
     * object's values in SharedPreferences.
     *
     * @param id The ID of a GeoFence object
     * @param fieldName The field represented by the key
     * @return The full key name of a value in SharedPreferences
     */
    private String getGeoFenceFieldKey(String id, String fieldName) {
        return GeoFenceUtils.KEY_PREFIX + id + "_" + fieldName;
    }

    //	present geoFenceId
	public void whichGeoFenceId(String ids) {
		
		Editor editor = mPrefs.edit();
		
		editor.putString("presentGeoFenceId",ids);
		
		editor.commit();
	}
	
	//	get geoFenceid
	public String getWhichGeoFenceId() {
		return mPrefs.getString("presentGeoFenceId","noData");
	}
}