package com.dozuki.ifixit.ui.topic_view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import com.dozuki.ifixit.ui.BaseSearchMenuDrawerActivity;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.topic.TopicNode;
import com.google.analytics.tracking.android.Fields;

public class TopicViewActivity extends BaseSearchMenuDrawerActivity {
   public static final String TOPIC_KEY = "TOPIC";

   private TopicViewFragment mTopicView;
   private TopicNode mTopicNode;

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      setContentView(R.layout.topic_view);

      showLoading(R.id.loading_container);

      if (savedState == null) {
         mTopicView = new TopicViewFragment();
         Bundle extras = getIntent().getExtras();

         if (extras != null) {
            mTopicView.setArguments(extras);
         }

         FragmentTransaction ft = getSupportFragmentManager()
          .beginTransaction();
         ft.replace(R.id.topic_view_fragment, mTopicView);
         ft.commit();
      } else {
         mTopicView = (TopicViewFragment)getSupportFragmentManager()
          .findFragmentById(R.id.topic_view_fragment);
      }

      mTopicNode = (TopicNode)getIntent().getSerializableExtra(TOPIC_KEY);

      if (mTopicNode != null) {
         setTitle(mTopicNode.getName());

         MainApplication.getGaTracker().set(Fields.SCREEN_NAME, "/category/" + mTopicNode.getName());
      }
   }

   @Override
   public void onAttachFragment(Fragment fragment) {
      if (fragment instanceof TopicViewFragment) {
         TopicViewFragment topicViewFragment = (TopicViewFragment)fragment;

         if (topicViewFragment.getTopicNode() == null && mTopicNode != null) {
            topicViewFragment.setTopicNode(mTopicNode);
         }
      }
   }

   @Override
   public void showLoading(int container) {
      findViewById(container).setVisibility(View.VISIBLE);
      super.showLoading(container);
   }

   @Override
   public void hideLoading() {
      super.hideLoading();
      findViewById(R.id.loading_container).setVisibility(View.GONE);
   }
}
