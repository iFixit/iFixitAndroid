package com.dozuki.ifixit.ui.guide.view;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APICall;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;

public class TeardownsActivity extends GuideListActivity {
   @Override
   protected int getGuideListTitle() {
      return R.string.teardowns;
   }

   @Override
   protected APICall getApiCall(int limit, int offset) {
      return APIService.getTeardowns(limit, offset);
   }

   @Subscribe
   public void onGuides(APIEvent.Guides event) {
      setGuides(event);
   }
}
