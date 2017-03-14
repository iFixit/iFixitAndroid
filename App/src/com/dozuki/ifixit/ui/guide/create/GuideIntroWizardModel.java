package com.dozuki.ifixit.ui.guide.create;

/**
 * Based on the page wizard example by Roman Nurik
 * https://code.google.com/p/romannurik-code/source/browse/misc/wizardpager/
 */

import android.content.Context;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.guide.wizard.AbstractWizardModel;
import com.dozuki.ifixit.model.guide.wizard.BranchPage;
import com.dozuki.ifixit.model.guide.wizard.EditTextPage;
import com.dozuki.ifixit.model.guide.wizard.GuideTitlePage;
import com.dozuki.ifixit.model.guide.wizard.Page;
import com.dozuki.ifixit.model.guide.wizard.PageList;
import com.dozuki.ifixit.model.guide.wizard.TopicNamePage;

import java.util.ArrayList;

public class GuideIntroWizardModel extends AbstractWizardModel {
   public static String HAS_SUBJECT_KEY = "hasSubject";
   public static String NO_SUBJECT_KEY = "noSubject";

   public GuideIntroWizardModel(Context context) {
      super(context);
   }

   @Override
   protected PageList onNewRootPageList() {
      App app = App.get();

      String[] typesArr = new String[app.getSite().getGuideTypes().size()];

      String topicName = app.getTopicName();

      Page topicPage = new TopicNamePage(this)
       .setDescription(app.getString(R.string.guide_intro_wizard_guide_topic_description,
        topicName.toLowerCase(), topicName.toLowerCase()))
       .setHint(app.getString(R.string.guide_intro_wizard_guide_topic_hint, topicName))
       .setTitle(app.getString(R.string.guide_intro_wizard_guide_topic_title, topicName))
       .setRequired(true);

      Page subjectPage = new EditTextPage(this)
       .setDescription(app.getString(R.string.guide_intro_wizard_guide_subject_description,
        topicName.toLowerCase()))
       .setHint(app.getString(R.string.guide_intro_wizard_guide_subject_hint))
       .setTitle(app.getString(R.string.guide_intro_wizard_guide_subject_title))
       .setRequired(true);

      Page titlePage = new GuideTitlePage(this)
       .setDescription(app.getString(R.string.guide_intro_wizard_guide_title_description,
        topicName.toLowerCase()))
       .setTitle(app.getString(R.string.guide_intro_wizard_guide_title_title));

      typesArr = app.getSite().getGuideTypes().toArray(typesArr);

      String[] hasSubject = filterTypesForBranch(typesArr, true);
      String[] noSubject = filterTypesForBranch(typesArr, false);

      Page typePage = new BranchPage(this, app.getString(R.string.guide_intro_wizard_guide_type_title));

      if (hasSubject.length != 0) {
         ((BranchPage)typePage).addBranch(hasSubject, HAS_SUBJECT_KEY, subjectPage);
      }

      if (noSubject.length != 0) {
         ((BranchPage)typePage).addBranch(noSubject, NO_SUBJECT_KEY);
      }

      ((BranchPage)typePage)
       .setChoices(typesArr)
       .setRequired(true);

      return new PageList(topicPage, typePage, titlePage);
   }

   private String[] filterTypesForBranch(String[] types, boolean hasSubjectBranch) {
      Site site = App.get().getSite();
      ArrayList<String> result = new ArrayList<String>();

      for (int i = 0; i < types.length; i++) {
         if (hasSubjectBranch == site.hasSubject(types[i])) {
            result.add(types[i]);
         }
      }

      return result.toArray(new String[result.size()]);
   }
}
