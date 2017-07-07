package com.poptech.poptalk.drawer;

import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.poptech.poptalk.R;
import com.poptech.poptalk.view.CheckableLinearLayout;
import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;

/**
 * Created by sontt on 02/05/2017.
 */

public class SubMenuDrawerViewHolder extends CheckableChildViewHolder {
    private TextView childTextView;
    public CheckableLinearLayout itemView;

    public SubMenuDrawerViewHolder(View itemView) {
        super(itemView);
        this.itemView = (CheckableLinearLayout) itemView;
        childTextView = (TextView) itemView.findViewById(R.id.list_item_singlecheck_artist_name);
    }

    @Override
    public Checkable getCheckable() {
        return itemView;
    }

    public void setSubMenuName(String name) {
        childTextView.setText(name);
    }
}
