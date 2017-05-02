package com.poptech.poptalk.drawer;

import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.poptech.poptalk.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

/**
 * Created by sontt on 02/05/2017.
 */

public class TopLevelMenuViewHolder extends GroupViewHolder {
    private TextView topLevelMenuName;
    private ImageView arrow;
    private ImageView icon;

    public TopLevelMenuViewHolder(View itemView) {
        super(itemView);
        topLevelMenuName = (TextView) itemView.findViewById(R.id.list_item_genre_name);
        arrow = (ImageView) itemView.findViewById(R.id.list_item_genre_arrow);
        icon = (ImageView) itemView.findViewById(R.id.list_item_genre_icon);
    }

    public void setTopLevelMenu(ExpandableGroup topLevelMenu) {
        topLevelMenuName.setText(topLevelMenu.getTitle());
        if(topLevelMenu instanceof TopLevelMenuDrawer){
            icon.setBackgroundResource(((TopLevelMenuDrawer)topLevelMenu).getIconResId());
        }
    }

    @Override
    public void expand() {
        animateExpand();
    }

    @Override
    public void collapse() {
        animateCollapse();
    }

    private void animateExpand() {
        RotateAnimation rotate =
                new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.setAnimation(rotate);
    }

    private void animateCollapse() {
        RotateAnimation rotate =
                new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.setAnimation(rotate);
    }
}
