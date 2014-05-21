package com.entourage.app.profile;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.entourage.app.CircleTransform;
import com.entourage.app.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Fabrice on 5/12/14.
 */
public class AlbumAdapter extends ArrayAdapter<String[]>
{
    private CircleTransform mTransformationCircle;

    public AlbumAdapter(Context context, List<String[]> albums)
    {
        super(context, R.layout.albums_list_item, albums);
        mTransformationCircle = new CircleTransform();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        String[] curAlbum = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.albums_list_item, parent, false);
        }

        ImageView albumCoverPhoto = (ImageView) convertView.findViewById(R.id.album_cover_photo);
        TextView albumName = (TextView) convertView.findViewById(R.id.album_name);
        TextView albumCount = (TextView) convertView.findViewById(R.id.album_count);

        albumName.setText(curAlbum[1]);
        albumCount.setText(curAlbum[2] + " " + getContext().getString(R.string.photo));
        Picasso.with(getContext())
                .load(curAlbum[3])
                .placeholder(R.drawable.ic_action_picture)
                .resize(130, 130)
                .centerCrop()
                .transform(mTransformationCircle)
                .into(albumCoverPhoto);

        return convertView;
    }
}
