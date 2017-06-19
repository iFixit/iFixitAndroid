package com.dozuki.ifixit.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.Utils;
import com.dozuki.ifixit.util.transformations.RoundedTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;


public class GuideListRecyclerAdapter extends RecyclerView.Adapter<GuideListRecyclerAdapter.ViewHolder> {

   private ArrayList<GuideInfo> mGuides;
   private final boolean mShortTitle;

   public GuideListRecyclerAdapter(ArrayList<GuideInfo> guides, boolean shortTitle) {
      mGuides = guides;
      mShortTitle = shortTitle;
   }

   @Override
   public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.guide_grid_item, parent, false);
      ViewHolder vh = new ViewHolder(v);
      return vh;
   }

   @Override
   public void onBindViewHolder(ViewHolder holder, int position) {
      holder.setItem(mGuides.get(position));
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

   public GuideInfo getItem(int position) {
      return mGuides.get(position);
   }

   public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      private final TextView mTitleView;
      private final ImageView mThumbnail;
      private final Picasso mPicasso;

      // each data item is just a string in this case
      public View mItemView;
      private GuideInfo mGuide;

      public ViewHolder(View v) {
         super(v);
         mItemView = v;

         mTitleView = (TextView)v.findViewById(R.id.guide_grid_item_title);
         mThumbnail = (ImageView)v.findViewById(R.id.guide_grid_item_thumbnail);
         mPicasso = Picasso.with(v.getContext());

         ((RelativeLayout)v.findViewById(R.id.guide_item_target)).setOnClickListener(this);
         mItemView.setOnClickListener(this);
      }

      public void setItem(GuideInfo guide) {
         mGuide = guide;
         mTitleView.setText(mGuide.mTitle);
         Transformation transform = new RoundedTransformation(4, 0);

         if (guide.hasImage()) {
            // Clear image before setting it to make sure the old image isn't the background while the new one is loading
            Utils.safeStripImageView(mThumbnail);
            mPicasso.cancelRequest(mThumbnail);

            String url = mGuide.getImagePath(ImageSizes.guideList);
            mPicasso
             .load(url)
             .transform(transform)
             .error(R.drawable.no_image)
             .into(mThumbnail);
         } else {
            mPicasso
             .load(R.drawable.no_image)
             .fit()
             .transform(transform)
             .into(mThumbnail);
         }
      }

      @Override
      public void onClick(View view) {
         Context context = view.getContext();
         Intent intent = GuideViewActivity.viewGuideid(context, mGuide.mGuideid);
         context.startActivity(intent);
      }
   }
}
