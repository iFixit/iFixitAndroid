package com.dozuki.ifixit.ui.guide;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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

   public static Intent viewComments(Context context, ArrayList<Comment> comments, String title, int guideid,
    int stepid) {

      Bundle args = new Bundle();
      args.putSerializable(COMMENTS_KEY, comments);
      args.putString(TITLE_KEY, title);
      args.putInt(GUIDEID_KEY, guideid);
      args.putInt(STEPID_KEY, stepid);
      Intent intent = new Intent(context, CommentsActivity.class);

      return intent;
   }

   public CommentsActivity() {
      mComments = new ArrayList<Comment>();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.guide_step_comments);

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

      ListView list = (ListView) findViewById(android.R.id.list);
      list.setEmptyView(findViewById(android.R.id.empty));

      mAdapter = new CommentsAdapter(this, mComments);
      list.setAdapter(mAdapter);

      setTitle(title);
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(COMMENTS_KEY, mComments);
      state.putInt(GUIDEID_KEY, mGuideid);
      state.putInt(STEPID_KEY, mStepid);
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
      } else {

      }
   }
}
