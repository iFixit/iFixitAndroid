package com.dozuki.ifixit.ui.guide.create;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;

import java.util.ArrayList;

class GuideCreateRecyclerListAdapter extends RecyclerView.Adapter<GuideListItemHolder> {
   private ArrayList<GuideInfo> mGuides;
   private GuideListItemListener mItemListener;

   public GuideCreateRecyclerListAdapter(ArrayList<GuideInfo> guides) {
      mGuides = guides;
   }

   public void setGuideListItemListener(GuideListItemListener l) {
      mItemListener = l;
   }

   @Override
   public GuideListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.guide_create_item, parent, false);
      GuideListItemHolder vh = new GuideListItemHolder(v, mItemListener);
      return vh;
   }

   @Override
   public void onBindViewHolder(GuideListItemHolder holder, int position) {
      Log.d("GuideCreateActivity", "Setting item " + position);
      holder.setItem(mGuides.get(position));
   }

   public ArrayList<GuideInfo> getAll() {
      Log.d("GuideCreateActivity", "Getall");
      return mGuides;
   }

   public void clear() {

      Log.d("GuideCreateActivity", "Clearing");
      mGuides.clear();
      notifyDataSetChanged();
   }

   public void addAll(ArrayList<GuideInfo> list) {
      Log.d("GuideCreateActivity", "addall");
      mGuides.addAll(list);
      notifyDataSetChanged();
   }

   @Override
   public int getItemCount() {
      return mGuides.size();
   }

}
