package com.dozuki.ifixit.dozuki.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.dozuki.model.Site;

public class SiteRowView extends RelativeLayout {
   protected Site mSite;
   protected TextView mSiteName;
   protected TextView mSiteDescription;
   protected ImageView mSitePrivateIcon;
   public SiteRowView(Context context) {
      super(context);

      LayoutInflater inflater = (LayoutInflater)context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.site_row, this, true);

      mSiteName = (TextView)findViewById(R.id.site_name);
      mSiteDescription = (TextView)findViewById(R.id.site_description);
      mSitePrivateIcon = (ImageView)findViewById(R.id.site_private);
   }

   public void setSite(Site site) {
      mSite = site;

      String siteTitle = (mSite.mTitle.compareTo("") == 0) ? mSite.mName : mSite.mTitle;
      mSiteName.setText(siteTitle);
      mSiteDescription.setText(mSite.mDescription);
      
      if (!site.mPublic) {
         mSitePrivateIcon.setVisibility(VISIBLE);
      } else {
         mSitePrivateIcon.setVisibility(INVISIBLE);
      }
   }
}
