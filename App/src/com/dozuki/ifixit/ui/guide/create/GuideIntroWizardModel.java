package com.dozuki.ifixit.ui.guide.create;

/**
 * Based on the page wizard example by Roman Nurik
 * https://code.google.com/p/romannurik-code/source/browse/misc/wizardpager/
 */


import android.content.Context;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.wizard.*;

public class GuideIntroWizardModel extends AbstractWizardModel {
   public static String HAS_SUBJECT_KEY = "hasSubject";
   public static String NO_SUBJECT_KEY = "noSubject";


   public GuideIntroWizardModel(Context context) {
      super(context);
   }

   @Override
   protected PageList onNewRootPageList() {
      MainApplication app = MainApplication.get();

      String[] typesArr = new String[app.getSite().getGuideTypes().size()];

      String topicName = app.getTopicName();

      String[] hasSubject = {"Repair", "Installation", "Disassembly"};
      String[] noSubject = {"Technique", "Maintenance", "Teardown"};

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

      Page summaryPage = new EditTextPage(this)
       .setDescription(app.getString(R.string.guide_intro_wizard_guide_summary_description))
       .setHint(app.getString(R.string.optional))
       .setTitle(app.getString(R.string.guide_intro_wizard_guide_summary_title));

      Page introductionPage = new EditTextPage(this)
       .setDescription(app.getString(R.string.guide_intro_wizard_guide_introduction_description,
        topicName.toLowerCase()))
       .setHint(app.getString(R.string.optional))
       .setTitle(app.getString(R.string.guide_intro_wizard_guide_introduction_title));

      Page typePage = new BranchPage(this, app.getString(R.string.guide_intro_wizard_guide_type_title))
       .addBranch(hasSubject, HAS_SUBJECT_KEY, subjectPage)
       .addBranch(noSubject, NO_SUBJECT_KEY)
       .setChoices(app.getSite().getGuideTypes().toArray(typesArr))
       .setRequired(true);

      return new PageList(
       topicPage,
       typePage,
       titlePage,
       summaryPage,
       introductionPage
      );
   }
}
