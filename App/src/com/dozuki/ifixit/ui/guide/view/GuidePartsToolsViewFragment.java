package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockListFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Item;

import java.util.ArrayList;

public class GuidePartsToolsViewFragment extends SherlockListFragment {
   private ArrayList<Item> mItems;

   public GuidePartsToolsViewFragment(ArrayList<Item> items) {
      mItems = new ArrayList<Item>(items);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

      View view = inflater.inflate(R.layout.guide_parts_tools, container, false);

      setListAdapter(new PartsToolsAdapter(getSherlockActivity(), mItems));

      return view;
   }
}
