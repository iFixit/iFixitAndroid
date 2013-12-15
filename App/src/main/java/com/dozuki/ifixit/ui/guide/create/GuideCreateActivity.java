package com.dozuki.ifixit.ui.guide.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiError;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.JSONHelper;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.squareup.otto.Subscribe;
import org.json.JSONException;

import java.util.ArrayList;

public class GuideCreateActivity extends BaseMenuDrawerActivity {
   static final int GUIDE_STEP_LIST_REQUEST = 0;
   static int GUIDE_STEP_EDIT_REQUEST = 1;
   private static final int MENU_CREATE_GUIDE = 3;
   private static final int NO_ID = -1;


   private static final String SHOWING_HELP = "SHOWING_HELP";
   private static final String GUIDE_FOR_DELETE = "GUIDE_FOR_DELETE";
   private static String GUIDE_OBJECT_KEY = "GUIDE_OBJECT_KEY";
   public static String GUIDE_KEY = "GUIDE_KEY";

   private ArrayList<GuideInfo> mUserGuideList = new ArrayList<GuideInfo>();
   private boolean mShowingHelp;
   private GuideInfo mGuideForDelete;
   private PullToRefreshListView mGuideListView;
   private GuideCreateListAdapter mGuideListAdapter;
   private Activity mActivity;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setTitle(getString(R.string.my_guides));

      setContentView(R.layout.guide_create);

      mActivity = this;

      if (savedInstanceState != null) {
         mUserGuideList = (ArrayList<GuideInfo>)savedInstanceState.getSerializable(GUIDE_OBJECT_KEY);
         mShowingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         mGuideForDelete = (GuideInfo) savedInstanceState.getSerializable(GUIDE_FOR_DELETE);

         if (mShowingHelp) {
            createHelpDialog().show();
         }
      } else {
         showLoading(R.id.loading_container);
         Api.call(this, ApiCall.userGuides());
      }

      mGuideListAdapter = new GuideCreateListAdapter();

      mGuideListView = (PullToRefreshListView) findViewById(R.id.guide_create_listview);
      mGuideListView.setAdapter(mGuideListAdapter);

      mGuideListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
         @Override
         public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            Api.call(mActivity, ApiCall.userGuides());
         }
      });

      MainApplication.getGaTracker().set(Fields.SCREEN_NAME, "/user/guides");
   }

   @Override
   public void onStart() {
      super.onStart();

      MainApplication.getGaTracker().send(MapBuilder.createAppView().build());
   }

   @Override
   public void onRestart() {
      super.onRestart();

      // Don't retry the event if the Activity is finishing. This fixes a crazy
      // bug that caused authenticated Activities to not finish even though they
      // should have been. This was caused by the user being logged out, the API
      // call triggering the Unauthorized ApiEvent which opened the LoginDialog
      // so MainApplication thought that the user was logging in and decided
      // not to finish the Activity below this one on the stack.
      if (!isFinishing()) {
         // Perform the API call again because data may have changed in child Activities.
         Api.call(this, ApiCall.userGuides());
      }
   }

   @Override
   public void onContentChanged() {
      super.onContentChanged();

      if (mGuideListView != null)
         mGuideListView.setEmptyView(findViewById(R.id.no_guides_text));
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case MENU_CREATE_GUIDE:
            Intent intent = new Intent(this, StepEditActivity.class);
            startActivity(intent);
            finish();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuItem createGuideItem = menu.add(0, MENU_CREATE_GUIDE, 0, R.string.add_guide);
      createGuideItem.setIcon(R.drawable.ic_menu_add_guide);
      createGuideItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);

      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);

      savedInstanceState.putSerializable(GUIDE_OBJECT_KEY, mUserGuideList);
      savedInstanceState.putSerializable(GUIDE_FOR_DELETE, mGuideForDelete);
      savedInstanceState.putBoolean(SHOWING_HELP, mShowingHelp);
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   @Subscribe
   public void onUserGuides(ApiEvent.UserGuides event) {
      if (!event.hasError()) {
         mUserGuideList.clear();
         mUserGuideList.addAll(event.getResult());

         mGuideListAdapter.notifyDataSetChanged();

         mGuideListView.onRefreshComplete();
         mGuideListView.setEmptyView(findViewById(R.id.no_guides_text));

         hideLoading();
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   @Subscribe
   public void onPublishStatus(ApiEvent.PublishStatus event) {
      // Update guide even if there is a conflict.
      if (!event.hasError() || event.getError().mType == ApiError.Type.CONFLICT) {
         Guide guide = event.getResult();
         for (GuideInfo userGuide : mUserGuideList) {
            if (userGuide.mGuideid == guide.getGuideid()) {
               userGuide.mRevisionid = guide.getRevisionid();
               userGuide.mPublic = guide.isPublic();
               userGuide.mIsPublishing = false;
               break;
            }
         }
      }

      if (event.hasError()) {
         Api.getErrorDialog(this, event).show();

         // Reset the guide state
         for (GuideInfo guide : mUserGuideList) {
            guide.mIsPublishing = false;
         }
      }

      mGuideListAdapter.notifyDataSetChanged();
   }

   @Subscribe
   public void onDeleteGuide(ApiEvent.DeleteGuide event) {
      if (!event.hasError()) {
         mUserGuideList.remove(mGuideForDelete);

         mGuideListAdapter.notifyDataSetChanged();
      } else {
         // Try to update the guide's revisionid on a conflict.
         if (event.getError().mType == ApiError.Type.CONFLICT) {
            try {
               Guide updatedGuide = JSONHelper.parseGuide(event.getResponse());
               GuideInfo guideToUpdate = mUserGuideList.get(mUserGuideList.indexOf(mGuideForDelete));

               guideToUpdate.mRevisionid = updatedGuide.getRevisionid();
            } catch (JSONException e) {
               Log.w("GuideCreateActivity", "Error parsing guide delete conflict", e);
            }
         }
         Api.getErrorDialog(this, event).show();
      }
   }

   private AlertDialog createHelpDialog() {
      mShowingHelp = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.media_help_title)).setMessage(getString(R.string.guide_create_help))
       .setPositiveButton(getString(R.string.media_help_confirm), new DialogInterface.OnClickListener() {

          public void onClick(DialogInterface dialog, int id) {
             mShowingHelp = false;
             dialog.cancel();
          }
       });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            mShowingHelp = false;
         }
      });

      return dialog;
   }

   public AlertDialog createDeleteDialog(GuideInfo item) {
      mGuideForDelete = item;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.confirm_delete_title))
       .setMessage(getString(R.string.confirm_delete_body, item.mTitle))
       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
             Api.call(GuideCreateActivity.this, ApiCall.deleteGuide(mGuideForDelete));
             dialog.cancel();
          }
       }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int id) {
            mGuideForDelete = null;
         }
      });

      AlertDialog dialog = builder.create();

      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
         }
      });

      return dialog;
   }

   public class GuideCreateListAdapter extends BaseAdapter {

      @Override
      public int getCount() {
         return mUserGuideList.size();
      }

      @Override
      public Object getItem(int position) {
         return mUserGuideList.get(position);
      }

      @Override
      public long getItemId(int position) {
         return position;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         GuideListItem itemView;
         GuideInfo currItem = (GuideInfo) getItem(position);

         if (convertView != null) {
            itemView = (GuideListItem) convertView;
         } else {
            itemView = new GuideListItem(parent.getContext(), mActivity);
         }

         itemView.setRowData(currItem);

         return itemView;
      }
   }
}
