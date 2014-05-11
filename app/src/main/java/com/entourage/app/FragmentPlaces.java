package com.entourage.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

/**
 * Created by Fabrice on 5/10/14.
 */
public class FragmentPlaces extends Fragment
{
    AutoCompleteTextView mPlacesAutoComplete;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_places, container, false);

        mPlacesAutoComplete = (AutoCompleteTextView) rootView.findViewById(R.id.searchField);
        String[] countries = {"The Beers", "Les Berthoms", "Le Logo", "La Marquise", "Ninkasi Gerland"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_list_item_1, countries);
        mPlacesAutoComplete.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }
}
