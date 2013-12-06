package com.dozuki.ifixit.model.search;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.ui.RoundedTransformation;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.PicassoUtils;

import java.io.Serializable;

public class GuideSearchResult implements SearchResult, Serializable {
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

      v.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), GuideViewActivity.class);
            intent.putExtra(GuideViewActivity.GUIDEID, mGuideInfo.mGuideid);
            v.getContext().startActivity(intent);
         }
      });

      ((TextView)v.findViewById(R.id.guide_title)).setText(Html.fromHtml(mGuideInfo.mTitle));
      ((TextView)v.findViewById(R.id.guide_author)).setText(
       MainApplication.get().getString(R.string.by_author, mGuideInfo.mAuthorName));

      ImageView thumbnail = (ImageView)v.findViewById(R.id.guide_thumbnail);

      if (mGuideInfo.hasImage()) {
         String imagePath = mGuideInfo.getImagePath(MainApplication.get().getImageSizes().getThumb());

         PicassoUtils.with(context)
          .load(imagePath)
          .transform(new RoundedTransformation(4, 0))
          .error(R.drawable.no_image)
          .into(thumbnail);
      } else {
         PicassoUtils.with(context).load(R.drawable.no_image).into(thumbnail);
      }

      return v;
   }

   @Override
   public int getLayout() {
      return R.layout.guide_search_result_row;
   }
}
