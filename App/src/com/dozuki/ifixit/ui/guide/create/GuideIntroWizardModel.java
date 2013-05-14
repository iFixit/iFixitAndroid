package com.dozuki.ifixit.ui.guide.create;
/*
 * Based on the page wizard example by Roman Nurik
 * https://code.google.com/p/romannurik-code/source/browse/misc/wizardpager/
 */


import android.content.Context;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.wizard.*;

import java.util.ArrayList;

public class GuideIntroWizardModel extends AbstractWizardModel {
   Context mContext;

   public GuideIntroWizardModel(Context context) {
      super(context);
      mContext = context;
   }

   @Override
   protected PageList onNewRootPageList() {
      MainApplication app = MainApplication.get();

      ArrayList<String> types = app.getSite().getGuideTypes();
      ArrayList<String> topics = app.getTopics();
      String[] typesArr = new String[types.size()];

      String topicName = app.getString(R.string.topic);

      if (app.getSite().mName.compareTo("ifixit") == 0) {
         topicName = app.getString(R.string.device);
      }

      return new PageList(
       new SingleFixedChoicePage(this, app.getString(R.string.guide_intro_wizard_guide_type_title))
        .setChoices(types.toArray(typesArr))
        .setRequired(true),
       new TopicNamePage(this)
        .setTopicAutocompleteList(topics)
        .setDescription(app.getString(R.string.guide_intro_wizard_guide_topic_description,
         topicName.toLowerCase(), topicName.toLowerCase()))
        .setTitle(app.getString(R.string.guide_intro_wizard_guide_topic_title, topicName))
        .setRequired(true),
       new EditTextPage(this)
        .setDescription(app.getString(R.string.guide_intro_wizard_guide_subject_description,
         topicName.toLowerCase()))
        .setTitle(app.getString(R.string.guide_intro_wizard_guide_subject_title)),
       new EditTextPage(this)
        .setDescription(app.getString(R.string.guide_intro_wizard_guide_title_description,
          topicName.toLowerCase()))
        .setTitle(app.getString(R.string.guide_intro_wizard_guide_title_title)),
       new EditTextPage(this)
        .setDescription(app.getString(R.string.guide_intro_wizard_guide_summary_description))
        .setTitle(app.getString(R.string.guide_intro_wizard_guide_summary_title)),
       new EditTextPage(this)
        .setDescription(app.getString(R.string.guide_intro_wizard_guide_introduction_description,
         topicName.toLowerCase()))
        .setTitle(app.getString(R.string.guide_intro_wizard_guide_introduction_title))
      );
   }
}
