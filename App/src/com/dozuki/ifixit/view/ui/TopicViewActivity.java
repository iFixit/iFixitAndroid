package com.dozuki.ifixit.view.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.view.model.TopicNode;
import com.ifixit.android.imagemanager.ImageManager;

public class TopicViewActivity extends SherlockFragmentActivity {
   public static final String TOPIC_KEY = "TOPIC";

   private TopicViewFragment mTopicView;
   private TopicNode mTopicNode;
   protected ImageManager mImageManager;

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      setContentView(R.layout.topic_view);

      mImageManager = ((MainApplication) getApplication()).getImageManager();
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      if (savedState == null) {
         mTopicView = new TopicViewFragment();
         FragmentTransaction ft =
            getSupportFragmentManager().beginTransaction();
         ft.replace(R.id.topic_view_fragment, mTopicView);
         ft.commit();
      } else {
         mTopicView =
            (TopicViewFragment) getSupportFragmentManager().findFragmentById(
               R.id.topic_view_fragment);
      }

      mTopicNode = (TopicNode) getIntent().getSerializableExtra(TOPIC_KEY);
   }

   @Override
   public void onAttachFragment(Fragment fragment) {
      if (fragment instanceof TopicViewFragment) {
         TopicViewFragment topicViewFragment = (TopicViewFragment) fragment;

         if (topicViewFragment.getTopicNode() == null && mTopicNode != null) {
            topicViewFragment.setTopicNode(mTopicNode);
         }
      }
   }

   @Override
   public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
      com.actionbarsherlock.view.MenuInflater inflater =
         getSupportMenuInflater();
      inflater.inflate(R.menu.menu_bar, menu);
      MenuItem galleryIcon = menu.findItem(R.id.gallery_button);
      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            finish();
            return true;
         case R.id.gallery_button:
            Intent intent = new Intent(this, GalleryActivity.class);
            startActivity(intent);
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }
}
