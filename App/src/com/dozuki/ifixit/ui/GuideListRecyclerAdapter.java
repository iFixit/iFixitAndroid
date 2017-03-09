package com.dozuki.ifixit.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.model.guide.GuideInfo;

import java.util.ArrayList;


public class GuideListRecyclerAdapter extends RecyclerView.Adapter<GuideListRecyclerAdapter.ViewHolder> {

   private ArrayList<GuideInfo> mGuides;
   private final boolean mShortTitle;
   private ItemClickListener mClickListener;

   public GuideListRecyclerAdapter(ArrayList<GuideInfo> guides, boolean shortTitle) {
      mGuides = guides;
      mShortTitle = shortTitle;
   }

   @Override
   public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      GuideItemView view = new GuideItemView(parent.getContext(), mShortTitle);
      ViewHolder vh = new ViewHolder(view);
      return vh;
   }

   @Override
   public void onBindViewHolder(ViewHolder holder, int position) {
      holder.mItemView.setGuideItem(mGuides.get(position));
   }

   @Override
   public int getItemCount() {
      return mGuides.size();
   }

   public void setGuides(ArrayList<GuideInfo> guides) {
      mGuides = guides;
   }

   public void addGuides(ArrayList<GuideInfo> guides) {
      mGuides.addAll(guides);
   }

   public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      // each data item is just a string in this case
      public GuideItemView mItemView;
      public ViewHolder(GuideItemView v) {
         super(v);
         mItemView = v;
      }

      @Override
      public void onClick(View view) {
         if (mClickListener != null) {
            mClickListener.onItemClick(view, getAdapterPosition());
         }
      }
   }

   // allows clicks events to be caught
   public void setClickListener(ItemClickListener itemClickListener) {
      this.mClickListener = itemClickListener;
   }

   // parent activity will implement this method to respond to click events
   public interface ItemClickListener {
      void onItemClick(View view, int position);
   }
}
