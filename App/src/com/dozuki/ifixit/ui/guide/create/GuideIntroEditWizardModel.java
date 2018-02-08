package com.dozuki.ifixit.ui.guide.create;

/**
 * Based on the page wizard example by Roman Nurik
 * https://code.google.com/p/romannurik-code/source/browse/misc/wizardpager/
 */

import android.content.Context;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.wizard.EditTextPage;
import com.dozuki.ifixit.model.guide.wizard.Page;
import com.dozuki.ifixit.model.guide.wizard.PageList;

public class GuideIntroEditWizardModel extends GuideIntroWizardModel {

   public GuideIntroEditWizardModel(Context context) {
      super(context);
   }

   @Override
   protected PageList onNewRootPageList() {
      App app = App.get();

      String topicName = app.getTopicName();

      Page summaryPage = new EditTextPage(this)
       .setDescription(app.getString(R.string.guide_intro_wizard_guide_summary_description))
       .setHint(app.getString(R.string.optional))
       .setTitle(app.getString(R.string.guide_intro_wizard_guide_summary_title));

      Page introductionPage = new EditTextPage(this)
       .setDescription(app.getString(R.string.guide_intro_wizard_guide_introduction_description,
        topicName.toLowerCase()))
       .setHint(app.getString(R.string.optional))
       .stripNewlines(false)
       .setTitle(app.getString(R.string.guide_intro_wizard_guide_introduction_title));

      PageList pages = super.onNewRootPageList();

      pages.add(summaryPage);
      pages.add(introductionPage);

      return pages;
   }
}
