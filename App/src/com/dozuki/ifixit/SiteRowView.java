package com.dozuki.ifixit;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SiteRowView extends RelativeLayout {
   protected Site mSite;
   protected TextView mSiteName;

   public SiteRowView(Context context) {
      super(context);

      LayoutInflater inflater = (LayoutInflater)context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.site_row, this, true);

      mSiteName = (TextView)findViewById(R.id.site_name);
   }

   public void setSite(Site site) {
      mSite = site;

      mSiteName.setText(mSite.toString());
   }
}
