package com.entourage.app.profile;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Fabrice on 5/14/2014.
 */
public class Profile implements Serializable
{
    public static final int DEFAULT_PICTURE_SIZE = 768;
    public static final int DEFAULT_MIN_PICTURE_SIZE = 256;
    public static final int DEFAULT_MAX_PICTURE = 4;
    public static final String PREFS_USER_PROFILE = "preferences_user_profile";
    private static final String PREFS_USER_PROFILE_USER_ID = "user_id";
    private static final String PREFS_USER_PROFILE_USER_FIRST_NAME = "user_first_name";
    private static final String PREFS_USER_PROFILE_USER_LAST_NAME = "user_last_name";
    private static final String PREFS_USER_PROFILE_USER_AGE = "user_age";
    private static final String PREFS_USER_PROFILE_USER_EMAIL = "user_email";
    private static final String PREFS_USER_PROFILE_USER_GENDER = "user_gender";
    private static final String PREFS_USER_PROFILE_USER_PICTURE = "user_picture";
    private static final String PREFS_USER_PROFILE_USER_BIO = "user_bio";

    private Session mFbSession;
    private String mUserId;
    private String mEmail;
    private String mFirstName;
    private String mLastName;
    private String mBirthDay;
    private String mGender;
    private String mBio;
    private ArrayList<String> mProfilePicture;
    private String mProfileAlbumId;

    /**
     * Constructor from a facebook session
     * @param fbSession
     */
    public Profile(Session fbSession)
    {
        this(fbSession, null, null, null, null, null, null, "");
    }

    /**
     * Constructor for saved instances
     * @param fbSession
     * @param userId
     * @param firstName
     */
    public Profile(Session fbSession, String userId, String email, String firstName, String lastName,
                   String birthday, String gender, String bio)
    {
        this.mFbSession = fbSession;
        this.mUserId = userId;
        this.mEmail = email;
        this.mFirstName = firstName;
        this.mLastName = lastName;
        this.mBirthDay = birthday;
        this.mGender = gender;
        this.mBio = bio;
        this.mProfilePicture = new ArrayList<String>();
    }

    public void loadBasicInformations()
    {
        Request.newMeRequest(mFbSession,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        // If the response is successful
                        if (user != null) {
                            mUserId = user.getId();
                            mEmail = (String) user.getProperty("email");
                            mGender = (String) user.getProperty("gender");
                            mFirstName = user.getFirstName();
                            mLastName = user.getLastName();
                            mBirthDay = user.getBirthday();
                        }
                        if (response.getError() != null) {
                            // TODO: Handle errors
                        }
                    }
                }).executeAndWait();
    }

    public String getProfilePictureAlbumId()
    {
        Bundle bundle = new Bundle();
        bundle.putString("fields", "type,id");

        new Request(
                mFbSession,
                "/me/albums",
                bundle,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response)
                    {
                        GraphObject graphObj = response.getGraphObject();
                        try {
                            JSONArray dataObj = graphObj.getInnerJSONObject().getJSONArray("data");
                            for (int i = 0; i < dataObj.length(); i++)
                            {
                                JSONObject curAlbum = dataObj.getJSONObject(i);
                                if (curAlbum.getString("type").equals("profile"))
                                {
                                    mProfileAlbumId = curAlbum.getString("id");
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                        }
                    }
                }
        ).executeAndWait();
        return mProfileAlbumId;
    }

    public void loadProfilePictures()
    {
        String profilePictureAlbumId = getProfilePictureAlbumId();

        Bundle bundle = new Bundle();
        bundle.putString("fields", "images,width,height");

        new Request(
                mFbSession,
                "/" + profilePictureAlbumId + "/photos",
                bundle,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response)
                    {
                        GraphObject graphObj = response.getGraphObject();
                        try {
                            JSONArray dataObj = graphObj.getInnerJSONObject().getJSONArray("data");
                            for (int i = 0; i < dataObj.length() && i < DEFAULT_MAX_PICTURE; i++)
                            {
                                JSONObject curPhotoObj = dataObj.getJSONObject(i);
                                String bestFitted = getBestFittedImage(curPhotoObj.getJSONArray("images"),
                                        curPhotoObj.getInt("width"),
                                        curPhotoObj.getInt("height"));
                                if (bestFitted != null)
                                    mProfilePicture.add(bestFitted);
                            }
                        } catch (JSONException e) {
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAndWait();
    }

    public static String getBestFittedImage(JSONArray curPhotoImages, int width, int height) throws JSONException, IOException
    {
        boolean ratioOnWidth = true;
        int closestImageAbsDiff = 0;
        int closestImageIdx = -1;
        int size = DEFAULT_PICTURE_SIZE;

        if (width < height)
            ratioOnWidth = false;

        for (int p = 0; p < curPhotoImages.length(); p++)
        {
            JSONObject curImageObj = curPhotoImages.getJSONObject(p);
            int curSize = curImageObj.getInt(ratioOnWidth ? "width" : "height");
            int curDiff = Math.abs(curSize - size);
            if (closestImageIdx < 0 || curDiff < closestImageAbsDiff)
            {
                closestImageAbsDiff = curDiff;
                closestImageIdx = p;
            }
        }

        if (closestImageIdx >= 0) {
            return curPhotoImages.getJSONObject(closestImageIdx).getString("source");
        }
        return null;
    }

    public void addToPictures(Bitmap bmp)
    {
        Bitmap dstBmp = bmp;

        if (bmp.getWidth() > bmp.getHeight())
        {
            dstBmp = Bitmap.createBitmap(
                    bmp,
                    bmp.getWidth()/2 - bmp.getHeight()/2,
                    0,
                    bmp.getHeight(),
                    bmp.getHeight()
            );

        }
        else if (bmp.getWidth() < bmp.getHeight())
        {
            dstBmp = Bitmap.createBitmap(
                    bmp,
                    0,
                    bmp.getHeight()/2 - bmp.getWidth()/2,
                    bmp.getWidth(),
                    bmp.getWidth()
            );
        }
        //mProfilePicture.add(dstBmp);
    }

    public void loadChanges()
    {
        loadBasicInformations();
        loadProfilePictures();
    }

    public String getUserId()
    {
        return mUserId;
    }

    public String getFirstName()
    {
        return mFirstName;
    }

    public String getBio()
    {
        return mBio;
    }

    public void setBio(String bio)
    {
        if (bio.length() > 500)
            bio = bio.substring(0, 500);
        this.mBio = bio;
    }

    public String getAge()
    {
        String age = "20";

        if (mBirthDay != null && !mBirthDay.isEmpty())
        {
            GregorianCalendar curDate = new  GregorianCalendar();
            String[] dateTab = mBirthDay.split("/");
            GregorianCalendar birthDayDate = new GregorianCalendar(Integer.parseInt(dateTab[2]),
                                                                   Integer.parseInt(dateTab[0]),
                                                                   Integer.parseInt(dateTab[1]));
            int ageInt = curDate.get(GregorianCalendar.YEAR) - birthDayDate.get(GregorianCalendar.YEAR);
            if (curDate.get(GregorianCalendar.MONTH) < birthDayDate.get(GregorianCalendar.MONTH)
               && curDate.get(GregorianCalendar.DAY_OF_MONTH) < birthDayDate.get(GregorianCalendar.DAY_OF_MONTH))
                ageInt -= 1;
            age = String.valueOf(ageInt);
        }
        return age;
    }

    public void addPicture(String url)
    {
        this.mProfilePicture.add(url);
    }

    public void removePicture(int idx)
    {
        if (idx >= 0 && idx < mProfilePicture.size())
            mProfilePicture.remove(idx);
    }

    public void swapProfilePicture(int firstIdx, int secondIdx)
    {
        Collections.swap(mProfilePicture, firstIdx, secondIdx);
    }

    public String getProfilePicture(int index)
    {
        if (index >= 0 && mProfilePicture.size() > index)
            return mProfilePicture.get(index);
        return null;
    }

    /**
     * Get the profile saved in shared preferences storage
     * @return Profile or null if none
     */
    public static Profile get(SharedPreferences settings, Session fbSession)
    {
        if (settings.contains(PREFS_USER_PROFILE_USER_ID))
        {
           Profile profile = new Profile(fbSession,
                   settings.getString(PREFS_USER_PROFILE_USER_ID, ""),
                   settings.getString(PREFS_USER_PROFILE_USER_EMAIL, ""),
                   settings.getString(PREFS_USER_PROFILE_USER_FIRST_NAME, ""),
                   settings.getString(PREFS_USER_PROFILE_USER_LAST_NAME, ""),
                   settings.getString(PREFS_USER_PROFILE_USER_AGE, ""),
                    settings.getString(PREFS_USER_PROFILE_USER_GENDER, ""),
                    settings.getString(PREFS_USER_PROFILE_USER_BIO, ""));
            for (int i = 0; i < DEFAULT_MAX_PICTURE; i++)
            {
                String imgKey = PREFS_USER_PROFILE_USER_PICTURE + String.valueOf(i);
                if (settings.contains(imgKey)) {
                    String url = settings.getString(imgKey, "");
                    if (!url.isEmpty())
                        profile.addPicture(url);
                }
            }
            return profile;
        }
        else
            return null;
    }

    /**
     * First time initialization for a new user
     *
     * @param settings
     * @param fbSession
     * @return The loaded Profile (TODO: handle network problems)
     */
    public static Profile initFromFbApi(SharedPreferences settings, Session fbSession)
    {
        Profile new_profile = new Profile(fbSession);
        new_profile.loadChanges();
        new_profile.saveLocal(settings);
        return new_profile;
    }

    public void saveLocal(SharedPreferences settings)
    {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_USER_PROFILE_USER_ID, mUserId);
        editor.putString(PREFS_USER_PROFILE_USER_EMAIL, mEmail);
        editor.putString(PREFS_USER_PROFILE_USER_GENDER, mGender);
        editor.putString(PREFS_USER_PROFILE_USER_FIRST_NAME, mFirstName);
        editor.putString(PREFS_USER_PROFILE_USER_LAST_NAME, mLastName);
        editor.putString(PREFS_USER_PROFILE_USER_AGE, mBirthDay);
        editor.putString(PREFS_USER_PROFILE_USER_BIO, mBio);
        for (int i = 0; i < Profile.DEFAULT_MAX_PICTURE; i++)
            editor.putString(PREFS_USER_PROFILE_USER_PICTURE + String.valueOf(i), getProfilePicture(i));
        editor.commit();
    }
}
