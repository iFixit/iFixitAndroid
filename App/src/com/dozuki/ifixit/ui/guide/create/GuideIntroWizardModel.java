package com.dozuki.ifixit.ui.guide.create;
/*
 * Based on the page wizard example by Roman Nurik
 * https://code.google.com/p/romannurik-code/source/browse/misc/wizardpager/
 */


import android.content.Context;
import com.dozuki.ifixit.MainApplication;
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

      ArrayList<String> types = MainApplication.get().getSite().getGuideTypes();
      ArrayList<String> topics = MainApplication.get().getTopics();
      String[] typesArr = new String[types.size()];

      String topicName = "Topic";

      if (app.getSite().mName.compareTo("ifixit") == 0) {
         topicName = "Device";
      }

      return new PageList(
       new SingleFixedChoicePage(this, "Guide Type")
        .setChoices(types.toArray(typesArr))
        .setRequired(true),
       new TopicNamePage(this)
        .setTopicAutocompleteList(topics)
        .setDescription("What " + topicName.toLowerCase() + " are you working on? For example, " +
         "is it a \"iPhone 4\" or a \"Kenmore " +
         "Washing Machine\"?  If the " + topicName.toLowerCase() + " already exists, " +
         "be sure to select it from the prepopulated " +
         "list")
        .setTitle(topicName + " Name")
        .setRequired(true),
       new EditTextPage(this)
        .setDescription("What is this guide specifically about?  Describe the component, " +
         "thing or idea that you are describing about this " + topicName.toLowerCase())
        .setTitle("Part"),
       new EditTextPage(this)
        .setDescription("Generated title from the type, " + topicName.toLowerCase() + " and focus.  Only edit if you " +
         "aren't happy with what's there.")
        .setTitle("Guide Title"),
       new EditTextPage(this)
        .setDescription("In a sentence or two, describe what this guide is about.")
        .setTitle("Guide Summary"),
       new EditTextPage(this)
        .setDescription("What is this guide specifically about?  Describe the component, " +
         "thing or idea that you are describing about this " + topicName.toLowerCase())
        .setTitle("Introduction")
      );
   }
}
