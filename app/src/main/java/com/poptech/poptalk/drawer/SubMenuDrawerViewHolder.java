package com.poptech.poptalk.drawer;

import android.view.View;
import android.widget.TextView;

import com.poptech.poptalk.R;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

/**
 * Created by sontt on 02/05/2017.
 */

public class SubMenuDrawerViewHolder extends ChildViewHolder {
    private TextView childTextView;

    public SubMenuDrawerViewHolder(View itemView) {
        super(itemView);
        childTextView = (TextView) itemView.findViewById(R.id.list_item_artist_name);
    }

    public void setSubMenuName(String name) {
        childTextView.setText(name);
    }
}
