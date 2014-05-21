package com.entourage.app.profile;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.entourage.app.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

/**
 * Created by Fabrice on 5/12/14.
 */
public class PhotoAdapter extends ArrayAdapter<String[]>
{

    public PhotoAdapter(Context context, List<String[]> photos)
    {
        super(context, R.layout.photos_list_item, photos); // photos_list_item not used
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView view = (ImageView) convertView;
        if (view == null) {
            view = new ImageView(getContext());
            view.setAdjustViewBounds(true);
            view.setScaleType(CENTER_CROP);
        }

        String[] photo = getItem(position);

        Picasso.with(getContext())
                .load(photo[1])
                .resize(Profile.DEFAULT_PICTURE_SIZE, Profile.DEFAULT_PICTURE_SIZE)
                .centerCrop()
                .into(view);
        return view;
    }
}
