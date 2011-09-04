package com.ifixit.guidebook;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuideIntroView extends LinearLayout {
   private Context mContext;
   private TextView mTitle;

   public GuideIntroView(Context context, Guide guide) {
      super(context);      
      this.mContext = context;

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.guide_intro, this, true);        

      mTitle = (TextView) findViewById(R.id.guide_title);
      mTitle.setText(guide.getTitle());

   }

}
