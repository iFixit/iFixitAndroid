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
import com.dozuki.ifixit.ui.guide.create.wizard.TopicNameFragment;

public class TopicNamePage extends EditTextPage {
   public static final String TOPIC_DATA_KEY = "name";

   public TopicNamePage(ModelCallbacks callbacks) {
      super(callbacks);
   }

   @Override
   public Fragment createFragment() {
      return TopicNameFragment.create(getKey());
   }
}
