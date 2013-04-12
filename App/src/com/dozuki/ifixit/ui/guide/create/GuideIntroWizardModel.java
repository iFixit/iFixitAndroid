package com.dozuki.ifixit.ui.guide.create;
/*
 * Based on the page wizard example by Roman Nurik
 * https://code.google.com/p/romannurik-code/source/browse/misc/wizardpager/
 */


import android.content.Context;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.model.guide.wizard.AbstractWizardModel;
import com.dozuki.ifixit.model.guide.wizard.PageList;
import com.dozuki.ifixit.model.guide.wizard.SingleFixedChoicePage;
import com.dozuki.ifixit.model.guide.wizard.TopicNamePage;

import java.util.ArrayList;

public class GuideIntroWizardModel extends AbstractWizardModel {
   Context mContext;

   public GuideIntroWizardModel(Context context) {
      super(context);
      mContext = context;
   }

   @Override
   protected PageList onNewRootPageList() {
      ArrayList<String> types = MainApplication.get().getSite().getGuideTypes();
      String[] typesArr = new String[types.size()];

      return new PageList(
       new SingleFixedChoicePage(this, "Guide Type")
        .setChoices(types.toArray(typesArr))
        .setRequired(true),
       new TopicNamePage(this)
        .setRequired(true)
      );
   }
}
