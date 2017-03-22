package com.dozuki.ifixit.model.search;

import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.ui.topic.TopicViewActivity;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.transformations.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.io.Serializable;

public class TopicSearchResult implements SearchResult, Serializable, View.OnClickListener {
   private static final long serialVersionUID = -24643222443335L;

   public String mTitle;
   public String mDisplayTitle;
   public String mNamespace;
   public String mSummary;
   public String mUrl;
   public String mText;
   public Image mImage = new Image();


   @Override
   public View buildView(View v, LayoutInflater inflater, ViewGroup container) {
      if (v == null) {
         v = inflater.inflate(getLayout(), container, false);
      }

      ((TextView)v.findViewById(R.id.search_result_title)).setText(Html.fromHtml(mDisplayTitle));
      ImageView thumbnail = (ImageView)v.findViewById(R.id.search_result_thumbnail);
      ((RelativeLayout)v.findViewById(R.id.search_result_target)).setOnClickListener(this);
      v.setOnClickListener(this);

      Picasso.with(container.getContext())
       .load(mImage.getPath(ImageSizes.stepThumb))
       .transform(new RoundedTransformation(4, 0))
       .error(R.drawable.no_image)
       .into(thumbnail);

      return v;
   }

   @Override
   public int getLayout() {
      return R.layout.search_row;
   }

   @Override
   public String getType() {
      return "device";
   }

   @Override
   public void onClick(View view) {
      Intent intent = TopicViewActivity.viewTopic(view.getContext(), mTitle);
      view.getContext().startActivity(intent);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      TopicSearchResult that = (TopicSearchResult) o;

      if (mTitle != null ? !mTitle.equals(that.mTitle) : that.mTitle != null) return false;
      if (mDisplayTitle != null ? !mDisplayTitle.equals(that.mDisplayTitle) : that.mDisplayTitle != null)
         return false;
      if (mNamespace != null ? !mNamespace.equals(that.mNamespace) : that.mNamespace != null)
         return false;
      if (mSummary != null ? !mSummary.equals(that.mSummary) : that.mSummary != null) return false;
      if (mUrl != null ? !mUrl.equals(that.mUrl) : that.mUrl != null) return false;
      if (mText != null ? !mText.equals(that.mText) : that.mText != null) return false;
      return mImage != null ? mImage.equals(that.mImage) : that.mImage == null;

   }

   @Override
   public int hashCode() {
      int result = mTitle != null ? mTitle.hashCode() : 0;
      result = 31 * result + (mDisplayTitle != null ? mDisplayTitle.hashCode() : 0);
      result = 31 * result + (mNamespace != null ? mNamespace.hashCode() : 0);
      result = 31 * result + (mSummary != null ? mSummary.hashCode() : 0);
      result = 31 * result + (mUrl != null ? mUrl.hashCode() : 0);
      result = 31 * result + (mText != null ? mText.hashCode() : 0);
      result = 31 * result + (mImage != null ? mImage.hashCode() : 0);
      return result;
   }
}
