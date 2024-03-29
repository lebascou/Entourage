package com.entourage.app.places;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.entourage.app.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Fabrice on 5/12/14.
 */
public class PlacesAdapter extends ArrayAdapter<Place>
{
    public PlacesAdapter(Context context, List<Place> places)
    {
        super(context, R.layout.places_list_item, places);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Place curPlace = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.places_list_item, parent, false);
        }
        ImageView viewPicture = (ImageView) convertView.findViewById(R.id.place_picture);
        if (curPlace.getPictureUrl() == null)
            viewPicture.setImageResource(R.drawable.ic_building);
        else
            Picasso.with(getContext()).load(curPlace.getPictureUrl())
                    .placeholder(R.drawable.ic_building)
                    .resize(128, 128)
                    .centerCrop()
                    .into(viewPicture);

        TextView viewName = (TextView) convertView.findViewById(R.id.place_name);
        viewName.setText(curPlace.getName());

        TextView viewPopulationMale = (TextView) convertView.findViewById(R.id.place_population_male);
        viewPopulationMale.setText(String.valueOf(curPlace.getPopulationMale()));

        TextView viewPopulationFemale = (TextView) convertView.findViewById(R.id.place_population_female);
        viewPopulationFemale.setText(String.valueOf(curPlace.getPopulationFemale()));


        if (curPlace.getAddress() != null)
            ((TextView) convertView.findViewById(R.id.place_info_1)).setText(curPlace.getAddress());
        if (curPlace.getTypes() != null)
            ((TextView) convertView.findViewById(R.id.place_info_2)).setText(String.valueOf(curPlace.getTypes()));
        if (curPlace.getRating() != null)
            ((TextView) convertView.findViewById(R.id.place_info_3)).setText(String.valueOf(curPlace.getRating()));

        return convertView;
    }
}
