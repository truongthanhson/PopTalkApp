package com.poptech.poptalk.drawer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.poptech.poptalk.R;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class DrawerMenuAdapter extends ExpandableRecyclerViewAdapter<TopLevelMenuViewHolder, SubMenuDrawerViewHolder> {

  public DrawerMenuAdapter(List<? extends ExpandableGroup> groups) {
    super(groups);
  }

  @Override
  public TopLevelMenuViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_top_level_menu_drawer, parent, false);
    return new TopLevelMenuViewHolder(view);
  }

  @Override
  public SubMenuDrawerViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_sub_menu_drawer, parent, false);
    return new SubMenuDrawerViewHolder(view);
  }

  @Override
  public void onBindChildViewHolder(SubMenuDrawerViewHolder holder, int flatPosition,
                                    ExpandableGroup group, int childIndex) {

    final SubMenuDrawer subMenu = ((TopLevelMenuDrawer) group).getItems().get(childIndex);
    holder.setSubMenuName(subMenu.getName());
    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Toast.makeText(view.getContext(), subMenu.getName(), Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onBindGroupViewHolder(TopLevelMenuViewHolder holder, int flatPosition,
      ExpandableGroup group) {

    holder.setTopLevelMenu(group);
  }
}
