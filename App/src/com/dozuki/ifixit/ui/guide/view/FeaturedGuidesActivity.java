package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.dozuki.ifixit.util.api.Api;
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
   protected ApiCall getApiCall(int limit, int offset) {
      return Api.getFeaturedGuides(limit, offset);
   }

   @Subscribe
   public void onGuides(ApiEvent.Guides event) {
      setGuides(event);
   }
}
