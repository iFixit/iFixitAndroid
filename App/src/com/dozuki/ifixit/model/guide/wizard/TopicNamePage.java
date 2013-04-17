/*
 * Copyright 2012 Roman Nurik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dozuki.ifixit.model.guide.wizard;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.ui.guide.create.wizard.TopicNameFragment;

import java.util.ArrayList;

/**
 * A page asking for a name and an email.
 */
public class TopicNamePage extends Page {
   public static final String TOPIC_DATA_KEY = "name";

   protected String mTopicName = "Topic";
   protected String mDescription;
   protected ArrayList<String> mTopics;

   public TopicNamePage(ModelCallbacks callbacks) {
      super(callbacks);

      MainApplication app = MainApplication.get();

      if (app.getSite().mName.compareTo("ifixit") == 0) {
         mTopicName = "Device";
      }

      setTitle(mTopicName + " Name");

      setDescription("What " + mTopicName.toLowerCase() + " are you working on? For example, " +
       "is it a \"iPhone 4\" or a \"Kenmore " +
       "Washing Machine\"?  If the " + mTopicName.toLowerCase() + " already exists, be sure to select it from the prepopulated " +
       "list");
   }

   @Override
   public Fragment createFragment() {
      return TopicNameFragment.create(getKey());
   }

   @Override
   public void getReviewItems(ArrayList<ReviewItem> dest) {
      dest.add(new ReviewItem(super.getTitle(), mData.getString(TOPIC_DATA_KEY), getKey(), -1));
   }

   @Override
   public boolean isCompleted() {
      return !TextUtils.isEmpty(mData.getString(TOPIC_DATA_KEY));
   }

   public ArrayList<String> getTopicAutocompleteList() {
      return mTopics;
   }

   public TopicNamePage setTopicAutocompleteList(ArrayList<String> topics) {
      mTopics = new ArrayList<String>(topics);

      return this;
   }

   public void setDescription(String description) {
      mDescription = description;
   }

   public String getDescription() {
      return mDescription;
   }
}
