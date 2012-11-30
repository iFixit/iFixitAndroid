package com.dozuki.ifixit.guide_create.ui;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.GalleryActivity;
import com.dozuki.ifixit.gallery.ui.MediaFragment;
import com.dozuki.ifixit.topic_view.ui.TopicsActivity;

public class GuideCreateActivity extends SherlockFragmentActivity{

	 private ActionBar mActionBar;
	 private GuidePortalFragment mGuidePortal;
	 
	 @Override
	   public void onCreate(Bundle savedInstanceState) {
		  setTheme(((MainApplication)getApplication()).getSiteTheme());
	      getSupportActionBar().setTitle(((MainApplication)getApplication())
	       .getSite().mTitle);
	      mActionBar = getSupportActionBar();
	      mActionBar.setTitle("");

	      super.onCreate(savedInstanceState);
	      
	      setContentView(R.layout.guide_create);
	      
	      mGuidePortal  = (GuidePortalFragment)getSupportFragmentManager().findFragmentById(
	    	       R.id.guide_create_view_fragment);
	      mGuidePortal.setRetainInstance(true);
	      
	      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	 }
	 
	 @Override
	   public boolean onCreateOptionsMenu(Menu menu) { 
			MenuInflater inflater = getSupportMenuInflater();
			inflater.inflate(R.menu.guide_create_menu, menu);
			SubMenu subMenu = menu.addSubMenu("");
			subMenu.setIcon(R.drawable.ic_menu_spinner);
			inflater.inflate(R.menu.menu_bar, subMenu);
			
			MenuItem subMenuItem = subMenu.getItem();
			subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
					| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

			return super.onCreateOptionsMenu(menu);
	   }
	 
	 @Override
	   public boolean onOptionsItemSelected(MenuItem item) {
		   Intent intent;
	      switch (item.getItemId()) {
	         case android.R.id.home:
	        	 finish();
	        	 return true;
	         case R.id.new_guide_button:
	        	 mGuidePortal.createGuide();
	        	 return true;
	         case R.id.gallery_button:
	            intent = new Intent(this, GalleryActivity.class);
	            startActivity(intent);
	            return true;
	         case R.id.my_guides_button:
	        	 return true;
	         case R.id.browse_button:
	        	 intent = new Intent(this, TopicsActivity.class);
		         startActivity(intent);
	        	 return true;
	         default:
	            return super.onOptionsItemSelected(item);
	      }
	 }

}
