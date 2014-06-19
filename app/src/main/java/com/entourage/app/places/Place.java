package com.entourage.app.places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fabrice on 5/12/14.
 */
public class Place
{
    private String mName;
    private String mPictureUrl = null;
    private String mId = null;
    private String mReference = null;
    private String mAddress = null;
    private Boolean mOpenNow = null;
    private GeographicLocation mLocation = null;
    private List<String> mTypes = new ArrayList<String>();
    private Float mRating = null;

    private int mPopulationMale;
    private int mPopulationFemale;

    public Place(String name)
    {
        this(name, null, 0, 0);
    }

    public Place(String name, String pictureUrl, int populationMale, int populationFemale)
    {
        this.mName = name;
        this.mPictureUrl = pictureUrl;
        this.mPopulationMale = populationMale;
        this.mPopulationFemale = populationFemale;
    }

    public static Place fromPlacesApiJson(JSONObject jsonObj) {
        Place newPlace = null;
        try {
            newPlace = new Place(jsonObj.getString("name"));
            newPlace.mPictureUrl = jsonObj.getString("icon"); // basic map api icon

            newPlace.setId(jsonObj.getString("id")); // ID -> use it as primary key
            newPlace.setReference(jsonObj.getString("reference")); // Reference to this location -> used for other api calls
            if (jsonObj.has("geometry"))
                newPlace.setLocation(GeographicLocation.fromPlacesApiJson(jsonObj.getJSONObject("geometry"))); // {location: {lat: '', lng: ''}} Location -> use it for distance
            if (jsonObj.has("rating"))
                newPlace.setRating(Float.valueOf(jsonObj.getString("rating"))); //Place rate -> [1.0, 5.0]
            if (jsonObj.has("vicinity"))
                newPlace.setAddress(jsonObj.getString("vicinity")); //Address
            if (jsonObj.has("types"))
            {
                JSONArray jsonTypes = jsonObj.getJSONArray("types"); //Array of types (bar, night_club...)-> use it to classify
                for (int i = 0; i < jsonTypes.length(); i++)
                    newPlace.getTypes().add(jsonTypes.getString(i));
            }
            if (jsonObj.has("opening_hours"))
            {
                JSONObject openings = jsonObj.getJSONObject("opening_hours"); // {open_now: true|false} Used to check if the place is opened
                if (openings.has("open_now"))
                {
                    newPlace.setOpenNow(openings.getBoolean("open_now"));
                }
            }
        } catch (JSONException e) {
        }
        return newPlace;
    }

    public String getName()
    {
        return mName;
    }

    public String getPictureUrl()
    {
        return mPictureUrl;
    }

    public int getPopulationMale()
    {
        return mPopulationMale;
    }

    public int getPopulationFemale()
    {
        return mPopulationFemale;
    }

    public void setPopulation(int male, int female)
    {
        this.mPopulationMale = male;
        this.mPopulationFemale = female;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getReference() {
        return mReference;
    }

    public void setReference(String mReference) {
        this.mReference = mReference;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public Boolean getOpenNow() {
        return mOpenNow;
    }

    public void setOpenNow(Boolean mOpenNow) {
        this.mOpenNow = mOpenNow;
    }

    public GeographicLocation getLocation() {
        return mLocation;
    }

    public void setLocation(GeographicLocation mLocation) {
        this.mLocation = mLocation;
    }

    public List<String> getTypes() {
        return mTypes;
    }

    public void setTypes(List<String> mTypes) {
        this.mTypes = mTypes;
    }

    public Float getRating() {
        return mRating;
    }

    public void setRating(Float mRating) {
        this.mRating = mRating;
    }
}
