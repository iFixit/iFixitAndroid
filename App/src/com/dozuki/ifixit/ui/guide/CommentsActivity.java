package com.dozuki.ifixit.ui.guide;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class CommentsActivity extends BaseActivity {

   private static final String COMMENTS_KEY = "COMMENTS_KEY";
   private static final String TITLE_KEY = "TITLE_FIELD";
   private static final String GUIDEID_KEY = "GUIDEID_KEY";
   private static final String STEPID_KEY = "STEPID_KEY";

   private ArrayList<Comment> mComments;
   private int mGuideid;
   private int mStepid;
   private CommentsAdapter mAdapter;
   private CommentsActivity mActivity;
   private ListView mCommentsList;

   public static Intent viewComments(Context context, ArrayList<Comment> comments, String title, int guideid,
    int stepid) {

      Intent intent = new Intent(context, CommentsActivity.class);

      intent.putExtra(TITLE_KEY, title);
      intent.putExtra(GUIDEID_KEY, guideid);
      intent.putExtra(STEPID_KEY, stepid);
      intent.putExtra(COMMENTS_KEY, comments);

      return intent;
   }

   public CommentsActivity() {
      mComments = new ArrayList<Comment>();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.comments);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      mActivity = this;
      Bundle args = getIntent().getExtras();
      String title;

      if (savedInstanceState != null) {
         mComments = (ArrayList<Comment>) savedInstanceState.getSerializable(COMMENTS_KEY);
         mGuideid = savedInstanceState.getInt(GUIDEID_KEY);
         mStepid = savedInstanceState.getInt(STEPID_KEY);
         title = savedInstanceState.getString(TITLE_KEY);
      } else if (args != null) {
         mComments = (ArrayList<Comment>) args.getSerializable(COMMENTS_KEY);
         mGuideid = args.getInt(GUIDEID_KEY);
         mStepid = args.getInt(STEPID_KEY);
         title = args.getString(TITLE_KEY);
      } else {
         title = getString(R.string.comments);
      }

      final EditText editText = (EditText) findViewById(R.id.add_comment_field);

      ImageButton addComment = (ImageButton) findViewById(R.id.add_comment_button);
      addComment.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String commentText = String.valueOf(editText.getText());

            if (commentText.length() > 0) {
               Api.call(mActivity, ApiCall.postNewGuideComment(commentText, mGuideid, mStepid));
            }
         }
      });

      mCommentsList = (ListView) findViewById(R.id.comment_list);
      mCommentsList.setEmptyView(findViewById(android.R.id.empty));

      mAdapter = new CommentsAdapter(this, mComments);
      mCommentsList.setAdapter(mAdapter);

      setTitle(title);
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(COMMENTS_KEY, mComments);
      state.putInt(GUIDEID_KEY, mGuideid);
      state.putInt(STEPID_KEY, mStepid);
   }


   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         // Respond to the action bar's Up/Home button
         case android.R.id.home:
            finish();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Subscribe
   public void onCommentAdd(ApiEvent.AddComment event) {
      if (!event.hasError()) {
         Guide guide = event.getResult();
         mComments.clear();
         if (mStepid == -1) {
            mComments.addAll(guide.getComments());
         } else {
            mComments.addAll(guide.getStepById(mStepid).getComments());
         }

         mAdapter.setComments(mComments);
         mAdapter.notifyDataSetChanged();
         scrollCommentsToBottom();
      } else {

      }
   }

   private void scrollCommentsToBottom() {
      new Handler().post(new Runnable() {
         @Override
         public void run() {
            mCommentsList.setSelection(mAdapter.getCount() - 1);
         }
      });
   }
}
