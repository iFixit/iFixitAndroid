package com.dozuki.ifixit.ui.guide.create;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.JSONHelper;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiError;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.squareup.otto.Subscribe;

import org.json.JSONException;

import java.util.ArrayList;

public class GuideCreateActivity extends BaseMenuDrawerActivity implements SwipeRefreshLayout.OnRefreshListener, GuideListItemListener {
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
   private GuideCreateRecyclerListAdapter mGuideRecyclerListAdapter;
   private RecyclerView mRecyclerListView;
   private SwipeRefreshLayout mSwipeLayout;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      super.setDrawerContent(R.layout.guide_create);

      getSupportActionBar().setTitle(getString(R.string.my_guides));

      if (savedInstanceState != null) {
         mUserGuideList = (ArrayList<GuideInfo>) savedInstanceState.getSerializable(GUIDE_OBJECT_KEY);
         mShowingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         mGuideForDelete = (GuideInfo) savedInstanceState.getSerializable(GUIDE_FOR_DELETE);

         if (mShowingHelp) {
            createHelpDialog().show();
         }
      } else {
         showLoading(R.id.loading_container);
         Api.call(this, ApiCall.userGuides());
      }

      mGuideRecyclerListAdapter = new GuideCreateRecyclerListAdapter(mUserGuideList);
      mGuideRecyclerListAdapter.setGuideListItemListener(this);
      mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
      mSwipeLayout.setOnRefreshListener(this);

      mRecyclerListView = (RecyclerView) findViewById(R.id.guide_create_listview);
      mRecyclerListView.setLayoutManager(new LinearLayoutManager(this));
      mRecyclerListView.setAdapter(mGuideRecyclerListAdapter);

      FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_guide_fab);

      if (fab != null) {
         fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(v.getContext(), StepEditActivity.class);
               startActivity(intent);
            }
         });
      }

      App.sendScreenView("/user/guides");
   }

   @Override
   public void onRefresh() {
      Api.call(GuideCreateActivity.this, ApiCall.userGuides());
   }

   @Override
   public void onStart() {
      super.onStart();
   }

   @Override
   public void onRestart() {
      super.onRestart();

      // Don't retry the event if the Activity is finishing. This fixes a crazy
      // bug that caused authenticated Activities to not finish even though they
      // should have been. This was caused by the user being logged out, the API
      // call triggering the Unauthorized ApiEvent which opened the LoginDialog
      // so App thought that the user was logging in and decided
      // not to finish the Activity below this one on the stack.
      if (!isFinishing()) {
         // Perform the API call again because data may have changed in child Activities.
         Api.call(this, ApiCall.userGuides());
      }
   }

   @Override
   public void onContentChanged() {
      super.onContentChanged();

      //if (mRecyclerListView != null)
      //   mRecyclerListView.setEmptyView(findViewById(R.id.no_guides_text));
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
      hideLoading();
      mSwipeLayout.setRefreshing(false);
      if (!event.hasError()) {
         mGuideRecyclerListAdapter.clear();
         mUserGuideList.clear();
         mUserGuideList.addAll(event.getResult());
         Log.d("GuideCreateActivity", "Size: " + mUserGuideList.size());
         mGuideRecyclerListAdapter.addAll(mUserGuideList);
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

      mGuideRecyclerListAdapter.clear();
      mGuideRecyclerListAdapter.addAll(mUserGuideList);
   }

   @Subscribe
   public void onDeleteGuide(ApiEvent.DeleteGuide event) {
      if (!event.hasError()) {
         mUserGuideList.remove(mGuideForDelete);
         mGuideRecyclerListAdapter.clear();
         mGuideRecyclerListAdapter.addAll(mUserGuideList);
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

      return builder.create();
   }

   @Override
   public void onEditItemClicked(GuideInfo guide) {
      App.sendEvent("ui_action", "button_press", "edit_guide", (long) guide.mGuideid);

      Intent intent = new Intent(this, StepsActivity.class);
      intent.putExtra(StepsActivity.GUIDE_ID_KEY, guide.mGuideid);
      intent.putExtra(StepsActivity.GUIDE_PUBLIC_KEY, guide.mPublic);
      startActivityForResult(intent, GuideCreateActivity.GUIDE_STEP_LIST_REQUEST);
   }

   @Override
   public void onPublishItemClicked(GuideInfo guide) {
      App.sendEvent("ui_action", "button_press", "publish_guide", (long) guide.mGuideid);

      // Ignore button press if we are already (un)publishing the guide.
      if (guide.mIsPublishing) {
         return;
      }

      guide.mIsPublishing = true;

      Log.d("Api", "Guide Revisionid before: " + guide.mRevisionid);

      if (!guide.mPublic) {
         Api.call(this,
          ApiCall.publishGuide(guide.mGuideid, guide.mRevisionid));
      } else {
         Api.call(this,
          ApiCall.unpublishGuide(guide.mGuideid, guide.mRevisionid));
      }
   }

   @Override
   public void onViewItemClicked(GuideInfo guide) {
      Intent intent = new Intent(this, GuideViewActivity.class);
      intent.putExtra(GuideViewActivity.GUIDEID, guide.mGuideid);
      intent.putExtra(GuideViewActivity.CURRENT_PAGE, 0);
      startActivity(intent);
   }

   @Override
   public void onDeleteItemClicked(GuideInfo guide) {
      App.sendEvent("ui_action", "button_press", "delete_guide", (long) guide.mGuideid);

      createDeleteDialog(guide).show();
   }
}
