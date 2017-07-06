package com.poptech.poptalk.drawer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.poptech.poptalk.R;
import com.poptech.poptalk.collections.AppMenuOpen;
import com.thoughtbot.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DrawerMenuAdapter extends CheckableChildRecyclerViewAdapter<TopLevelMenuViewHolder, SubMenuDrawerViewHolder> {
    AppMenuOpen delegate;
    private final Set<Integer> mSelectedPos = Collections.synchronizedSet(new TreeSet<Integer>());

    public DrawerMenuAdapter(List<? extends CheckedExpandableGroup> groups, AppMenuOpen delegate) {
        super(groups);
        this.delegate = delegate;
    }

    @Override
    public TopLevelMenuViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_top_level_menu_drawer, parent, false);
        return new TopLevelMenuViewHolder(view);
    }

    @Override
    public SubMenuDrawerViewHolder onCreateCheckChildViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_sub_menu_drawer, viewGroup, false);
        return new SubMenuDrawerViewHolder(view);
    }

    @Override
    public void onBindCheckChildViewHolder(SubMenuDrawerViewHolder subMenuDrawerViewHolder, int i, CheckedExpandableGroup checkedExpandableGroup, int childIndex) {
        final SubMenuDrawer subMenu = (SubMenuDrawer) checkedExpandableGroup.getItems().get(childIndex);
        subMenuDrawerViewHolder.setSubMenuName(subMenu.getName());
    }

    @Override
    public void onBindGroupViewHolder(TopLevelMenuViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {

        holder.setTopLevelMenu(group);
    }

    @Override
    public void onChildCheckChanged(View view, boolean checked, int flatPos) {
        super.onChildCheckChanged(view, checked, flatPos);
    }
}
