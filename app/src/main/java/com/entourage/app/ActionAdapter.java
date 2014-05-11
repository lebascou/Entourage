package com.entourage.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Fabrice on 5/9/14.
 */
public class ActionAdapter extends ArrayAdapter<String>
{
    public ActionAdapter(Context context, String[] actions)
    {
        super(context, R.layout.action_list_item, actions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String action = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.action_list_item, parent, false);
        }
        ImageView view_action_icon = (ImageView) convertView.findViewById(R.id.action_icon);
        if (action == getContext().getString(R.string.title_settings))
            view_action_icon.setImageResource(R.drawable.ic_settings);
        TextView view_action_title = (TextView) convertView.findViewById(R.id.action_title);
        view_action_title.setText(action);
        return convertView;
    }
}
