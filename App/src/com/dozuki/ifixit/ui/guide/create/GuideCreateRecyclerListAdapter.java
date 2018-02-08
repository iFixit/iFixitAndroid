package com.dozuki.ifixit.ui.guide.create;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;

import java.util.ArrayList;

class GuideCreateRecyclerListAdapter extends RecyclerView.Adapter<GuideListItemHolder> {
   private ArrayList<GuideInfo> mGuides = new ArrayList<>();
   private GuideListItemListener mItemListener;

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

   public void updateItem(Guide guide) {
      for (int i = mGuides.size() - 1; i >= 0; i--) {
         final GuideInfo userGuide = mGuides.get(i);
         if (userGuide.mGuideid == guide.getGuideid()) {
            userGuide.mTitle = guide.getTitle();
            userGuide.mRevisionid = guide.getRevisionid();
            userGuide.mPublic = guide.isPublic();
            userGuide.mIsPublishing = false;
            mGuides.set(i, userGuide);
            break;
         }
      }

      notifyDataSetChanged();
   }

   public void markAllAsFinished() {
      for (GuideInfo guide : mGuides) {
         guide.mIsPublishing = false;
      }

      notifyDataSetChanged();
   }

   public ArrayList<GuideInfo> getAll() {
      return mGuides;
   }

   public void remove(GuideInfo guide) {
      mGuides.remove(guide);
      notifyDataSetChanged();
   }

   public void removeAll() {
      mGuides.clear();
      notifyDataSetChanged();
   }

   public void clear() {
      mGuides.clear();
      notifyDataSetChanged();
   }

   public void addAll(ArrayList<GuideInfo> list) {
      mGuides.addAll(list);
      notifyDataSetChanged();
   }

   public void replaceAll(ArrayList<GuideInfo> list) {
      mGuides.clear();
      mGuides.addAll(list);
      notifyDataSetChanged();
   }

   @Override
   public int getItemCount() {
      return mGuides.size();
   }

}
