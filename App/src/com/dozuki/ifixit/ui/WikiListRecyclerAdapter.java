package com.dozuki.ifixit.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Wiki;
import com.dozuki.ifixit.ui.wiki.WikiViewActivity;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.Utils;
import com.dozuki.ifixit.util.transformations.RoundedTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;


public class WikiListRecyclerAdapter extends RecyclerView.Adapter<WikiListRecyclerAdapter.ViewHolder> {

   private Context mContext;
   private ArrayList<Wiki> mItems;
   private final boolean mShortTitle;

   public WikiListRecyclerAdapter(Context context, ArrayList<Wiki> items, boolean shortTitle) {
      mItems = items;
      mShortTitle = shortTitle;
      mContext = context;
   }

   @Override
   public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_grid_item, parent, false);
      ViewHolder vh = new ViewHolder(v);
      return vh;
   }

   @Override
   public void onBindViewHolder(ViewHolder holder, int position) {
      holder.setItem(mItems.get(position));
   }

   @Override
   public int getItemCount() {
      return mItems.size();
   }

   public void setItems(ArrayList<Wiki> items) {
      mItems = items;
   }

   public void addItems(ArrayList<Wiki> items) {
      mItems.addAll(items);
   }

   public Wiki getItem(int position) {
      return mItems.get(position);
   }

   public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      private final TextView mTitleView;
      private final ImageView mThumbnail;
      private final Picasso mPicasso;

      // each data item is just a string in this case
      public View mItemView;
      private Wiki Wiki;

      public ViewHolder(View v) {
         super(v);
         mItemView = v;

         mTitleView = (TextView)v.findViewById(R.id.simple_grid_item_title);
         mThumbnail = (ImageView)v.findViewById(R.id.simple_grid_item_thumbnail);
         mPicasso = Picasso.with(v.getContext());

         ((RelativeLayout)v.findViewById(R.id.simple_item_target)).setOnClickListener(this);
         mItemView.setOnClickListener(this);
      }

      public void setItem(Wiki item) {
         Wiki = item;
         mTitleView.setText(Wiki.displayTitle);
         Transformation transform = new RoundedTransformation(4, 0);

         if (item.hasImage()) {
            // Clear image before setting it to make sure the old image isn't the background while the new one is loading
            Utils.safeStripImageView(mThumbnail);
            mPicasso.cancelRequest(mThumbnail);

            String url = Wiki.getImagePath(ImageSizes.guideList);
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
         Intent intent = WikiViewActivity.viewByTitle(context, Wiki.title);
         context.startActivity(intent);
      }
   }
}
