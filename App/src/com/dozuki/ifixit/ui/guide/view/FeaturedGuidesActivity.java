package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APICall;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.otto.Subscribe;

public class FeaturedGuidesActivity extends GuideListActivity {

   @Override
   public void onCreate(Bundle state) {
      super.onCreate(state);

      MainApplication.getGaTracker().set(Fields.SCREEN_NAME, "/guides/featured");
      MainApplication.getGaTracker().send(MapBuilder.createAppView().build());
   }

   @Override
   protected int getGuideListTitle() {
      return R.string.featured_guides;
   }

   @Override
   protected APICall getApiCall(int limit, int offset) {
      return APIService.getFeaturedGuides(limit, offset);
   }

   @Subscribe
   public void onGuides(APIEvent.Guides event) {
      setGuides(event);
   }
}
