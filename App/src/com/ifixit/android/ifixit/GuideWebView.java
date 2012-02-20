package com.ifixit.android.ifixit;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class GuideWebView extends WebViewClient {
   private static final int GUIDE_POSITION = 3;
   private static final int GUIDEID_POSITION = 5;
   private static final String GUIDE_URL = "Guide";
   private static final String TEARDOWN_URL = "Teardown";


   protected MainActivity mGuideActivity;

   public GuideWebView(MainActivity guideActivity) {
      mGuideActivity = guideActivity;
   }

   @Override
   public boolean shouldOverrideUrlLoading(WebView view, String url) {
      String[] pieces = url.split("/");
      int guideid;

      try {
         if (pieces[GUIDE_POSITION].equals(GUIDE_URL) ||
          pieces[GUIDE_POSITION].equals(TEARDOWN_URL)) {
            guideid = Integer.parseInt(pieces[GUIDEID_POSITION]);
            mGuideActivity.viewGuide(guideid);
            return true;
         }
      }
      catch (ArrayIndexOutOfBoundsException e) {}
      catch (NumberFormatException e) {}

      view.loadUrl(url);

      return true;
   }
}
