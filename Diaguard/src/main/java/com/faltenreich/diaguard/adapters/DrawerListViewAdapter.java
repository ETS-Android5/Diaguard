package com.faltenreich.diaguard.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.faltenreich.diaguard.R;
import com.faltenreich.diaguard.helpers.PreferenceHelper;

/**
 * Created by Filip on 06.01.2015.
 */
public class DrawerListViewAdapter extends BaseAdapter {

    private Context context;
    private String[] titles;
    private int[] icons;
    public int fragmentCount;

    PreferenceHelper preferenceHelper;

    public DrawerListViewAdapter(Context context, String[] titles, int[] fragmentIcons) {
        this.context = context;
        this.titles = titles;
        this.icons = fragmentIcons;
        this.fragmentCount = fragmentIcons.length;
        this.preferenceHelper = new PreferenceHelper(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView;
        if(position < icons.length) {
            itemView = inflater.inflate(R.layout.drawer_list_item_fragment, parent, false);
            ImageView imgIcon = (ImageView) itemView.findViewById(R.id.icon);
            imgIcon.setImageResource(icons[position]);
        }
        else {
            itemView = inflater.inflate(R.layout.drawer_list_item_activity, parent, false);
        }

        TextView txtTitle = (TextView) itemView.findViewById(R.id.title);
        txtTitle.setText(titles[position]);
        if(position == 0) {
            // Initialization
            txtTitle.setTypeface(null, Typeface.BOLD);
            txtTitle.setTextColor(context.getResources().getColor(R.color.green));
            ((ImageView) itemView.findViewById(R.id.icon)).
                    setImageDrawable(context.getResources().getDrawable(R.drawable.ic_home_active));
        }

        return itemView;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Object getItem(int position) {
        return titles[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}