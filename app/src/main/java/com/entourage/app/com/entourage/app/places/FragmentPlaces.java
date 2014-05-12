package com.entourage.app.com.entourage.app.places;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import com.entourage.app.R;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Fabrice on 5/10/14.
 */
public class FragmentPlaces extends Fragment implements ContextualUndoAdapter.DeleteItemCallback
{
    private AutoCompleteTextView mPlacesAutoComplete;
    private ListView mPlacesListView;
    private PlacesAdapter mPlacesListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_places, container, false);

        mPlacesAutoComplete = (AutoCompleteTextView) rootView.findViewById(R.id.searchField);
        mPlacesListView = (ListView) rootView.findViewById(R.id.placesList);

        mPlacesAutoComplete.setAdapter(new PlacesAutoCompleteAdapter(inflater.getContext(), R.layout.autocomplete_list_item));

        Place[] places_favorite = {
                new Place("Le Loft club", null, 64, 180),
                new Place("The Wallace", null, 1, 32),
                new Place("La cours des miracles", null, 19, 27),
                new Place("Le Bloc", null, 126, 185),
                new Place("The Wallace", null, 1, 32),
                new Place("St James pub", null, 484, 563)};
        ArrayList<Place> places_favorite_list = new ArrayList<Place>();
        places_favorite_list.addAll(Arrays.asList(places_favorite));
        mPlacesListAdapter = new PlacesAdapter(inflater.getContext(), places_favorite_list);
        ContextualUndoAdapter undo_adapter = new ContextualUndoAdapter(mPlacesListAdapter, R.layout.place_undo_row, R.id.place_undo_row_undobutton, 5000, this);
        undo_adapter.setAbsListView(mPlacesListView);
        mPlacesListView.setAdapter(undo_adapter);

        return rootView;
    }

    @Override
    public void deleteItem(int position)
    {
        Place item = mPlacesListAdapter.getItem(position);
        if (item != null)
        {
            mPlacesListAdapter.remove(item);
            mPlacesListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }
}
