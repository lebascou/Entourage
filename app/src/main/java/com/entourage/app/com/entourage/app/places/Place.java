package com.entourage.app.com.entourage.app.places;

/**
 * Created by Fabrice on 5/12/14.
 */
public class Place
{
    private String mName;
    private String mPicturePath;
    private int mPopulationMale;
    private int mPopulationFemale;

    public Place(String name)
    {
        this(name, null, 0, 0);
    }

    public Place(String name, String picturePath, int populationMale, int populationFemale)
    {
        this.mName = name;
        this.mPicturePath = picturePath;
        this.mPopulationMale = populationMale;
        this.mPopulationFemale = populationFemale;
    }

    public String getName()
    {
        return mName;
    }

    public String getPicturePath()
    {
        return mPicturePath;
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

}
