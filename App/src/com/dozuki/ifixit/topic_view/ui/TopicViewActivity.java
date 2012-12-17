package com.dozuki.ifixit.topic_view.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.dozuki.ui.SiteListActivity;
import com.dozuki.ifixit.gallery.ui.GalleryActivity;
import com.dozuki.ifixit.gallery.ui.MediaFragment;
import com.dozuki.ifixit.login.model.LoginListener;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.login.ui.LoginFragment;
import com.dozuki.ifixit.topic_view.model.TopicNode;
import com.ifixit.android.imagemanager.ImageManager;

import org.holoeverywhere.app.Activity;

public class TopicViewActivity extends Activity implements LoginListener {
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
   public boolean onPrepareOptionsMenu(Menu menu) {
      MenuItem logout = menu.findItem(R.id.logout_button);
      logout.setVisible(!((MainApplication)getApplication()).getSite().mPublic);
      
      return super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getSupportMenuInflater();
      inflater.inflate(R.menu.menu_bar, menu);

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
         case R.id.logout_button:
            LoginFragment.getLogoutDialog(this).show();
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public void onLogout() {
      MainApplication app = (MainApplication)getApplication();
      app.logout();
      app.setSite(null);
      Intent intent = new Intent(this, SiteListActivity.class);
      startActivity(intent);
      
      finish();
   }

   @Override
   public void onLogin(User user) {
      // TODO Auto-generated method stub
   }

   @Override
   public void onCancel() {
      finish();
   }
}
