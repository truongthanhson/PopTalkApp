package com.poptech.poptalk.drawer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.poptech.poptalk.R;
import com.poptech.poptalk.collections.AppMenuOpen;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class DrawerMenuAdapter extends ExpandableRecyclerViewAdapter<TopLevelMenuViewHolder, SubMenuDrawerViewHolder> {
  AppMenuOpen delegate;

  public DrawerMenuAdapter(List<? extends ExpandableGroup> groups, AppMenuOpen delegate) {
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
  public SubMenuDrawerViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_sub_menu_drawer, parent, false);
    return new SubMenuDrawerViewHolder(view);
  }

  @Override
  public void onBindChildViewHolder(SubMenuDrawerViewHolder holder, int flatPosition,
                                    final ExpandableGroup group, int childIndex) {

    final SubMenuDrawer subMenu = ((TopLevelMenuDrawer) group).getItems().get(childIndex);
    holder.setSubMenuName(subMenu.getName());
    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(group.getTitle().equalsIgnoreCase("view")){
          if(subMenu.getName().equalsIgnoreCase("collection")){
            delegate.onNavigateToViewCollection();
          }else if(subMenu.getName().equalsIgnoreCase("list")){
            delegate.onNavigateToViewList();
          }else if(subMenu.getName().equalsIgnoreCase("location")){
            delegate.onNavigateToViewLocation();
          }
        }else if(group.getTitle().equalsIgnoreCase("sort by")){
            if(subMenu.getName().equalsIgnoreCase("description")){
                delegate.onNavigateToSortByDescription();
            }else if(subMenu.getName().equalsIgnoreCase("language")){
                delegate.onNavigateToSortByLanguage();
            }else if(subMenu.getName().equalsIgnoreCase("recent")){
                delegate.onNavigateToSortByRecent();
            }
        }else if(group.getTitle().equalsIgnoreCase("story board")){
          if(subMenu.getName().equalsIgnoreCase("map & datetime")){
            delegate.onNavigateStoryboardMap();
          }else if(subMenu.getName().equalsIgnoreCase("frequency")){
            delegate.onNavigateStoryboardFrequency();
          }
        }
      }
    });
  }

  @Override
  public void onBindGroupViewHolder(TopLevelMenuViewHolder holder, int flatPosition,
      ExpandableGroup group) {

    holder.setTopLevelMenu(group);
  }
}
