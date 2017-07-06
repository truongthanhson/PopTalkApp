package com.poptech.poptalk.drawer;

import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.poptech.poptalk.R;
import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

/**
 * Created by sontt on 02/05/2017.
 */

public class SubMenuDrawerViewHolder extends CheckableChildViewHolder {
    private CheckedTextView childTextView;

    public SubMenuDrawerViewHolder(View itemView) {
        super(itemView);
        childTextView = (CheckedTextView) itemView.findViewById(R.id.list_item_singlecheck_artist_name);
    }

    @Override
    public Checkable getCheckable() {
        return childTextView;
    }

    public void setSubMenuName(String name) {
        childTextView.setText(name);
    }
}
