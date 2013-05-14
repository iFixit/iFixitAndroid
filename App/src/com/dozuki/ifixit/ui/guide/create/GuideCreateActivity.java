package com.dozuki.ifixit.ui.guide.create;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import com.dozuki.ifixit.ui.IfixitActivity;
import com.dozuki.ifixit.util.APIError;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.squareup.otto.Subscribe;
import org.holoeverywhere.app.Activity;

import java.util.ArrayList;

public class GuideCreateActivity extends IfixitActivity implements GuideIntroFragment.GuideCreateIntroListener {
   static final int GUIDE_STEP_LIST_REQUEST = 0;
   static int GUIDE_STEP_EDIT_REQUEST = 1;
   private static final int MENU_CREATE_GUIDE = 3;
   private static final int NO_ID = -1;


   private static final String SHOWING_HELP = "SHOWING_HELP";
   private static final String SHOWING_DELETE = "SHOWING_DELETE";
   private static final String GUIDE_FOR_DELETE = "GUIDE_FOR_DELETE";
   private static String GUIDE_OBJECT_KEY = "GUIDE_OBJECT_KEY";
   public static String GUIDE_KEY = "GUIDE_KEY";
   private static final String LOADING = "LOADING";
   private int mCurOpenGuideObjectID;

   private ArrayList<GuideInfo> mUserGuideList = new ArrayList<GuideInfo>();
   private boolean mShowingHelp;
   private GuideInfo mGuideForDelete;
   private PullToRefreshListView mGuideListView;
   private GuideCreateListAdapter mGuideListAdapter;
   private Activity mActivity;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setTheme(MainApplication.get().getSiteTheme());
      getSupportActionBar().setTitle(MainApplication.get().getSite().mTitle);

      setContentView(R.layout.guide_create);

      mActivity = this;

      if (savedInstanceState != null) {
         mUserGuideList = (ArrayList<GuideInfo>) savedInstanceState.getSerializable(GUIDE_OBJECT_KEY);
         mShowingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         mGuideForDelete = (GuideInfo) savedInstanceState.getSerializable(GUIDE_FOR_DELETE);

         if (mShowingHelp) {
            createHelpDialog().show();
         }
      } else {
         APIService.call(this, APIService.getUserGuidesAPICall());
      }

      mGuideListAdapter = new GuideCreateListAdapter();

      mGuideListView = (PullToRefreshListView) findViewById(R.id.guide_create_listview);
      mGuideListView.setAdapter(mGuideListAdapter);

      mGuideListView.setEmptyView(findViewById(R.layout.guide_create_empty_guides_view));

      mGuideListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
         @Override
         public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            APIService.call(mActivity, APIService.getUserGuidesAPICall());
         }
      });
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case MENU_CREATE_GUIDE:
            createGuide();
            break;
      }

      return super.onOptionsItemSelected(item);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuItem createGuideItem = menu.add(0, MENU_CREATE_GUIDE, 0, R.string.add_guide);
      createGuideItem.setIcon(R.drawable.ic_menu_add_guide);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
         createGuideItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
      }

      return super.onCreateOptionsMenu(menu);
   }


   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putSerializable(GUIDE_OBJECT_KEY, mUserGuideList);
      savedInstanceState.putSerializable(GUIDE_FOR_DELETE, mGuideForDelete);
      savedInstanceState.putBoolean(SHOWING_HELP, mShowingHelp);
      super.onSaveInstanceState(savedInstanceState);
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }
/*
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == GUIDE_STEP_LIST_REQUEST || requestCode == GUIDE_STEP_EDIT_REQUEST) {
         if (resultCode == RESULT_OK) {
            Guide guide = (Guide) data.getSerializableExtra(GUIDE_KEY);
            if (guide == null) {
               return;
            }
            for (GuideInfo g : mUserGuideList) {
               if (g.getGuideid() == guide.getGuideid()) {
                  g.setRevisionid(guide.getRevisionid());
                  g.setTitle(guide.getTitle());
                  break;
               }
            }
         }
      }
   }*/

   @Subscribe
   public void onUserGuides(APIEvent.UserGuides event) {
      if (!event.hasError()) {
         mUserGuideList.clear();
         mUserGuideList.addAll(event.getResult());

         mGuideListAdapter.notifyDataSetChanged();

         mGuideListView.onRefreshComplete();
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(this, event.getError(), null).show();
      }
   }

   @Subscribe
   public void onPublishStatus(APIEvent.PublishStatus event) {
      if (!event.hasError()) {
         Guide guide = event.getResult();
         for (GuideInfo userGuide : mUserGuideList) {
            if (userGuide.mGuideid == guide.getGuideid()) {
               userGuide.mRevisionid = guide.getRevisionid();
               userGuide.mPublic = guide.getPublic();
               break;
            }
         }
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(this, event.getError(), null).show();
      }
   }

   @Subscribe
   public void onDeleteGuide(APIEvent.DeleteGuide event) {
      if (!event.hasError()) {
         getGuideList().remove(mGuideForDelete);

         mGuideListAdapter.notifyDataSetChanged();
      } else {
         event.setError(APIError.getFatalError(this));
         APIService.getErrorDialog(this, event.getError(), null).show();
      }
   }

   public ArrayList<GuideInfo> getGuideList() {
      return mUserGuideList;
   }

   public void addGuide(GuideInfo guide) {
      mUserGuideList.add(guide);
   }

   public void createGuide() {
      if (mUserGuideList == null) {
         return;
      }
      launchGuideCreateIntro();
   }

   private void launchGuideCreateIntro() {
      Intent intent = new Intent(this, GuideIntroActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
      startActivityForResult(intent, GUIDE_STEP_EDIT_REQUEST);
   }

   @Override
   public void onFinishIntroInput(String device, String title, String summary,
    String intro, String guideType, String subject) {
/*      showLoading();
      Guide guide = new Guide();//(GuideItemID++);
      guide.mTitle = (title);
      guide.(device);
      guide.setType(guideType);
      guide.setDevice(device);
      //guideObject.setSummary(summary);
      guide.setSubject(subject);
      guide.setIntroduction(intro);
      APIService.call(this, APIService.getCreateGuideAPICall(guideObject)); */
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
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.confirm_delete_title))
       .setMessage(getString(R.string.confirm_delete_body) + " \"" + item.mTitle + "\"?")
       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
             APIService.call(GuideCreateActivity.this, APIService.getRemoveGuideAPICall(mGuideForDelete));
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
         GuideInfo currItem = (GuideInfo)getItem(position);

         if (convertView != null) {
            itemView =  (GuideListItem) convertView;
         } else {
            itemView = new GuideListItem(parent.getContext(), mActivity);
         }

         itemView.setRowData(currItem);

         return itemView;
      }
   }

   public void onItemSelected(int id, boolean sel) {
      if (!sel) {
         mCurOpenGuideObjectID = NO_ID;
         return;
      }
      if (mCurOpenGuideObjectID != NO_ID) {
         GuideListItem view = ((GuideListItem) mGuideListView.findViewWithTag(mCurOpenGuideObjectID));
         if (view != null) {
            view.setChecked(false);
         }
         for (int i = 0; i < mUserGuideList.size(); i++) {

            if (mUserGuideList.get(i).mGuideid == mCurOpenGuideObjectID) {
               mUserGuideList.get(i).mEditMode = false;
            }
         }
      }
      mCurOpenGuideObjectID = id;
   }
}
