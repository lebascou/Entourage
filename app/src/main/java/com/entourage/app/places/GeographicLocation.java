package com.entourage.app.places;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Fabrice on 5/26/2014.
 */
public class GeographicLocation
{
    private float mLatitude;
    private float mLongitude;

    public GeographicLocation(float latitude, float longitude)
    {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public GeographicLocation(String latitude, String longitude)
    {
        mLatitude = Float.valueOf(latitude);
        mLongitude = Float.valueOf(longitude);
    }

    public static GeographicLocation fromPlacesApiJson(JSONObject geometryObj)
    {
        GeographicLocation newLocation = null;

        try {
            JSONObject locationObj = geometryObj.getJSONObject("location");
            newLocation = new GeographicLocation(locationObj.getString("lat"), locationObj.getString("lng"));
        } catch (JSONException e) {
        }
        return newLocation;
    }
}
