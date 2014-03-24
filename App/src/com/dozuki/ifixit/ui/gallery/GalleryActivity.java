package com.dozuki.ifixit.ui.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.util.Utils;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.HashMap;

public class GalleryActivity extends BaseMenuDrawerActivity {

   public static final String MEDIA_FRAGMENT_PHOTOS = "MEDIA_FRAGMENT_PHOTOS";
   public static final String ACTIVITY_RETURN_MODE = "ACTIVITY_RETURN_ID";

   private static final String SHOWING_DELETE = "SHOWING_DELETE";
   public static final String MEDIA_RETURN_KEY = "MEDIA_RETURN_KEY";
   public static final String ATTACHED_MEDIA_IDS = "ATTACHED_MEDIA_IDS";

   public static boolean showingLogout = false;
   public static boolean showingHelp = false;
   public static boolean showingDelete = false;

   private HashMap<String, MediaFragment> mMediaCategoryFragments;
   private MediaFragment mCurrentMediaFragment;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.gallery_root);

      mMediaCategoryFragments = new HashMap<String, MediaFragment>();
      mMediaCategoryFragments.put(MEDIA_FRAGMENT_PHOTOS, new PhotoMediaFragment());

      /*
       * mMediaCategoryFragments.put(MEDIA_FRAGMENT_VIDEOS,
       * new VideoMediaFragment());
       * mMediaCategoryFragments.put(MEDIA_FRAGMENT_EMBEDS,
       * new EmbedMediaFragment());
       */
      mCurrentMediaFragment = mMediaCategoryFragments.get(MEDIA_FRAGMENT_PHOTOS);

      showingHelp = false;
      showingLogout = false;
      showingDelete = false;

      boolean getMediaItemForReturn = false;

      Bundle bundle = getIntent().getExtras();

      if (bundle != null) {
         int returnValue = bundle.getInt(ACTIVITY_RETURN_MODE, -1);
         ArrayList<Image> alreadyAttachedImages = (ArrayList<Image>)bundle.getSerializable(ATTACHED_MEDIA_IDS);
         mCurrentMediaFragment.setAlreadyAttachedImages(alreadyAttachedImages);
         if (returnValue != -1) {
            getMediaItemForReturn = true;
         }
         startActionMode(new ContextualMediaSelect());
      }

      mCurrentMediaFragment.setForReturn(getMediaItemForReturn);
      StepAdapter stepAdapter = new StepAdapter(this.getSupportFragmentManager());
      ViewPager pager = (ViewPager) findViewById(R.id.gallery_view_body_pager);
      pager.setAdapter(stepAdapter);
      TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.gallery_view_top_bar);
      titleIndicator.setViewPager(pager);
      pager.setCurrentItem(1);

      App.getGaTracker().set(Fields.SCREEN_NAME, "/user/media/images");
      App.getGaTracker().send(MapBuilder.createAppView().build());
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putBoolean(SHOWING_DELETE, showingDelete);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      boolean isLoggedIn = ((App) getApplication()).isUserLoggedIn();
      switch (item.getItemId()) {
         case R.id.top_camera_button:
            if (!isLoggedIn) {
               return false;
            }
            mCurrentMediaFragment.launchCamera();
            return true;
         case R.id.top_gallery_button:
            if (!isLoggedIn) {
               return false;
            }
            mCurrentMediaFragment.launchImageChooser();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public void onLogin(LoginEvent.Login event) {
      super.onLogin(event);

      if (App.get().isFirstTimeGalleryUser()) {
         createHelpDialog().show();
         App.get().setFirstTimeGalleryUser(false);
      }
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getSupportMenuInflater().inflate(R.menu.gallery_menu, menu);

      return super.onCreateOptionsMenu(menu);
   }

   /**
    * Why in the world is this class called StepAdapter?
    */
   public class StepAdapter extends FixedFragmentStatePagerAdapter {

      public StepAdapter(FragmentManager fm) {
         super(fm);
      }

      @Override
      public int getCount() {
         return mMediaCategoryFragments.size();
      }

      @Override
      public CharSequence getPageTitle(int position) {
         return Utils.capitalize(getString(R.string.images));
         /*
          * switch (position) {
          * case 0:
          * return "Videos";
          * case 1:
          * return "Photos";
          * case 2:
          * return "Embeds";
          * default:
          * return "Photos";
          * }
          */
      }

      @Override
      public Fragment getItem(int position) {
         return mMediaCategoryFragments.get(MEDIA_FRAGMENT_PHOTOS);
      }

      @Override
      public void setPrimaryItem(ViewGroup container, int position, Object object) {
         super.setPrimaryItem(container, position, object);
         mCurrentMediaFragment = (MediaFragment) object;
      }
   }

   private AlertDialog createHelpDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);

      builder.setTitle(getString(R.string.media_help_title)).setMessage(getString(R.string.media_help_message,
       App.get().getSite().mTitle))
         .setPositiveButton(getString(R.string.media_help_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               dialog.cancel();
            }
         });

      return builder.create();
   }

   public final class ContextualMediaSelect implements ActionMode.Callback {
      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
         // Create the menu from the xml file
         getSupportMenuInflater().inflate(R.menu.gallery_menu, menu);
         return true;
      }

      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
         menu.findItem(R.id.top_camera_button).setVisible(false);
         return true;
      }

      @Override
      public void onDestroyActionMode(ActionMode mode) {
         finish();
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
         boolean isLoggedIn = ((App) getApplication()).isUserLoggedIn();

         if (!isLoggedIn) return false;

         switch (item.getItemId()) {
            case R.id.top_camera_button:
               mCurrentMediaFragment.launchCamera();
               break;
            case R.id.top_gallery_button:
               mCurrentMediaFragment.launchImageChooser();
               break;
         }
         return true;
      }
   }
}
