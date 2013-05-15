package com.dozuki.ifixit.model.guide.wizard;


import android.support.v4.app.Fragment;
import android.text.TextUtils;
import com.dozuki.ifixit.ui.guide.create.wizard.GuideTitleFragment;

import java.util.ArrayList;

public class GuideTitlePage extends EditTextPage {
   public static final String TITLE_DATA_KEY = "name";

   public GuideTitlePage(ModelCallbacks callbacks) {
      super(callbacks);
   }

   @Override
   public Fragment createFragment() {
      return GuideTitleFragment.create(getKey());
   }

   @Override
   public void getReviewItems(ArrayList<ReviewItem> dest) {
      dest.add(new ReviewItem(super.getTitle(), mData.getString(TITLE_DATA_KEY), getKey(), -1));
   }

   @Override
   public boolean isCompleted() {
      return !TextUtils.isEmpty(mData.getString(TITLE_DATA_KEY));
   }
}
