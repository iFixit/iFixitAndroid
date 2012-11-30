package com.dozuki.ifixit.topic_view.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.GalleryActivity;
import com.dozuki.ifixit.guide_create.ui.GuideCreateActivity;
import com.dozuki.ifixit.topic_view.model.TopicNode;
import com.ifixit.android.imagemanager.ImageManager;

public class TopicViewActivity extends SherlockFragmentActivity {
   public static final String TOPIC_KEY = "TOPIC";

   private TopicViewFragment mTopicView;
   private TopicNode mTopicNode;
   protected ImageManager mImageManager;

   @Override
   public void onCreate(Bundle savedState) {
      setTheme(((MainApplication)getApplication()).getSiteTheme());
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

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
	   SubMenu subMenu = menu.addSubMenu("");
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu_bar, subMenu);
		subMenu.setIcon(R.drawable.ic_menu_spinner);
		MenuItem subMenuItem = subMenu.getItem();
		subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
	   Intent intent ;
      switch (item.getItemId()) {
         case android.R.id.home:
            finish();
            return true;
         case R.id.gallery_button:
            intent = new Intent(this, GalleryActivity.class);
            startActivity(intent);
            return true;
         case R.id.my_guides_button:
        	 intent = new Intent(this, GuideCreateActivity.class);
        	 startActivity(intent);
        	 return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }
}
