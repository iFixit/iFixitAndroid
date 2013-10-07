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
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.PicassoUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class GuideSearchResult implements SearchResult, Serializable {
   private static final long serialVersionUID = -2464223443335L;

   public int mGuideid;
   public String mLocale;
   public String mUrl;
   public int mRevisionid;
   public Date mModifiedDate;
   public Date mPrereqModifiedDate;
   public String mGuideType;
   public String mTopic;
   public String mSubject;
   public String mTitle;
   public boolean mPublic;
   public ArrayList<String> mFlags;
   public Image mImage;
   public int mUserid;
   public String mAuthorUsername;

   public GuideSearchResult() {
      mFlags = new ArrayList<String>();
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
            intent.putExtra(GuideViewActivity.GUIDEID, mGuideid);
            v.getContext().startActivity(intent);
         }
      });

      ((TextView)v.findViewById(R.id.guide_title)).setText(Html.fromHtml(mTitle));
      ((TextView)v.findViewById(R.id.guide_author)).setText(
       MainApplication.get().getString(R.string.by_author, mAuthorUsername));

      ImageView thumbnail = (ImageView)v.findViewById(R.id.guide_thumbnail);

      PicassoUtils.with(context)
       .load(mImage.getPath(MainApplication.get().getImageSizes().getThumb()))
       .into(thumbnail);

      return v;
   }

   @Override
   public int getLayout() {
      return R.layout.guide_search_result_row;
   }
}
