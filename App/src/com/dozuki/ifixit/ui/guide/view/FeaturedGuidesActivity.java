package com.dozuki.ifixit.ui.guide.view;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APICall;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;

public class FeaturedGuidesActivity extends GuideListActivity {
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
