package com.dozuki.ifixit.ui.topic_view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.topic.TopicNode;
import com.dozuki.ifixit.ui.IfixitActivity;
import com.marczych.androidimagemanager.ImageManager;

public class TopicViewActivity extends IfixitActivity {
   public static final String TOPIC_KEY = "TOPIC";

   private TopicViewFragment mTopicView;
   private TopicNode mTopicNode;
   protected ImageManager mImageManager;

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      setContentView(R.layout.topic_view);

      mImageManager = ((MainApplication)getApplication()).getImageManager();
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      if (savedState == null) {
         mTopicView = new TopicViewFragment();
         FragmentTransaction ft = getSupportFragmentManager()
          .beginTransaction();
         ft.replace(R.id.topic_view_fragment, mTopicView);
         ft.commit();
      } else {
         mTopicView = (TopicViewFragment)getSupportFragmentManager()
          .findFragmentById(R.id.topic_view_fragment);
      }

      mTopicNode = (TopicNode)getIntent().getSerializableExtra(TOPIC_KEY);
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

/*   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            finish();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }*/
}
