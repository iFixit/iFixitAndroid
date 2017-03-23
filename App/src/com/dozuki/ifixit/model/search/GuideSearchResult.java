package com.dozuki.ifixit.model.search;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.transformations.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.io.Serializable;

public class GuideSearchResult implements SearchResult, Serializable, View.OnClickListener {
   private static final long serialVersionUID = -2464223423335L;

   private GuideInfo mGuideInfo;

   public GuideSearchResult(GuideInfo guideInfo) {
      mGuideInfo = guideInfo;
   }

   @Override
   public View buildView(View v, LayoutInflater inflater, ViewGroup container) {
      final Context context = container.getContext();

      if (v == null) {
         v = inflater.inflate(getLayout(), container, false);
      }

      ((RelativeLayout)v.findViewById(R.id.search_result_target)).setOnClickListener(this);
      v.setOnClickListener(this);

      ((TextView)v.findViewById(R.id.guide_title)).setText(mGuideInfo.mTitle);
      ((TextView)v.findViewById(R.id.guide_author)).setText(
       App.get().getString(R.string.by_author, mGuideInfo.mAuthorName));

      ImageView thumbnail = (ImageView)v.findViewById(R.id.guide_thumbnail);

      if (mGuideInfo.hasImage()) {
         String imagePath = mGuideInfo.getImagePath(ImageSizes.stepThumb);

         Picasso.with(context)
          .load(imagePath)
          .transform(new RoundedTransformation(4, 0))
          .error(R.drawable.no_image)
          .into(thumbnail);
      } else {
         Picasso.with(context).load(R.drawable.no_image).into(thumbnail);
      }

      return v;
   }
   @Override
   public void onClick(View v) {
      Intent intent = new Intent(v.getContext(), GuideViewActivity.class);
      intent.putExtra(GuideViewActivity.GUIDEID, mGuideInfo.mGuideid);
      v.getContext().startActivity(intent);
   }

   @Override
   public int getLayout() {
      return R.layout.guide_search_result_row;
   }

   @Override
   public String getType() {
      return "guide";
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      GuideSearchResult that = (GuideSearchResult) o;

      return mGuideInfo != null ? mGuideInfo.equals(that.mGuideInfo) : that.mGuideInfo == null;

   }

   @Override
   public int hashCode() {
      return mGuideInfo != null ? mGuideInfo.hashCode() : 0;
   }
}
