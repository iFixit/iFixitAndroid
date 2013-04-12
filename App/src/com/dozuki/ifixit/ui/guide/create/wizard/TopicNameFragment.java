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

package com.dozuki.ifixit.ui.guide.create.wizard;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.wizard.TopicNamePage;
import org.holoeverywhere.widget.EditText;

public class TopicNameFragment extends Fragment {
   private static final String ARG_KEY = "key";

   private PageFragmentCallbacks mCallbacks;
   private String mKey;
   private TopicNamePage mPage;
   private EditText mTopicNameView;

   public static TopicNameFragment create(String key) {
      Bundle args = new Bundle();
      args.putString(ARG_KEY, key);

      TopicNameFragment fragment = new TopicNameFragment();
      fragment.setArguments(args);
      return fragment;
   }

   public TopicNameFragment() {
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      Bundle args = getArguments();
      mKey = args.getString(ARG_KEY);
      mPage = (TopicNamePage) mCallbacks.onGetPage(mKey);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.guide_create_intro_topic_name, container, false);
      ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

      mTopicNameView = ((EditText) rootView.findViewById(R.id.topic_name));

      return rootView;
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);

      if (!(activity instanceof PageFragmentCallbacks)) {
         throw new ClassCastException("Activity must implement PageFragmentCallbacks");
      }

      mCallbacks = (PageFragmentCallbacks) activity;
   }

   @Override
   public void onDetach() {
      super.onDetach();
      mCallbacks = null;
   }

   @Override
   public void onViewCreated(View view, Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

   }

   @Override
   public void setMenuVisibility(boolean menuVisible) {
      super.setMenuVisibility(menuVisible);

   }
}
