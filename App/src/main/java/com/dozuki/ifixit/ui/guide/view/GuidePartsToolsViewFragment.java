package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Item;
import com.dozuki.ifixit.ui.BaseListFragment;

import java.util.ArrayList;

public class GuidePartsToolsViewFragment extends BaseListFragment {
   private static final String ITEMS = "ITEMS";

   private ArrayList<Item> mItems;

   public static GuidePartsToolsViewFragment newInstance(ArrayList<Item> items) {
      GuidePartsToolsViewFragment fragment = new GuidePartsToolsViewFragment();
      Bundle args = new Bundle();
      args.putSerializable(ITEMS, items);
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      mItems = (ArrayList<Item>)getArguments().getSerializable(ITEMS);
      View view = inflater.inflate(R.layout.guide_parts_tools, container, false);

      setListAdapter(new PartsToolsAdapter(getSherlockActivity(), mItems));

      return view;
   }
}
