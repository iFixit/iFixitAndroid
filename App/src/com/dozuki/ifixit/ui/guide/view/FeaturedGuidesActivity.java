package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.squareup.otto.Subscribe;

public class FeaturedGuidesActivity extends GuideListActivity {
   @Override
   public void onCreate(Bundle state) {
      super.onCreate(state);

      App.sendScreenView("/guides/featured");
   }

   @Override
   protected int getGuideListTitle() {
      return R.string.featured_guides;
   }

   @Override
   protected ApiCall getApiCall(int limit, int offset) {
      return ApiCall.featuredGuides(limit, offset);
   }

   @Subscribe
   public void onGuides(ApiEvent.Guides event) {
      setGuides(event);
   }
}
