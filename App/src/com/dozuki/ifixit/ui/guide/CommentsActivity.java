package com.dozuki.ifixit.ui.guide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;

public class CommentsActivity extends BaseActivity {

   private static final String COMMENTS_KEY = "COMMENTS_KEY";
   private static final String TITLE_KEY = "TITLE_FIELD";
   private static final String GUIDEID_KEY = "GUIDEID_KEY";
   private static final String STEPID_KEY = "STEPID_KEY";

   private ArrayList<Comment> mComments;
   private int mGuideid;
   private int mStepid;
   private CommentsAdapter mAdapter;
   private ListView mCommentsList;
   private EditText mAddCommentField;
   private Comment mCommentToDelete = null;

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

      mAddCommentField = (EditText) findViewById(R.id.add_comment_field);

      ImageButton addComment = (ImageButton) findViewById(R.id.add_comment_button);
      addComment.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            mAddCommentField.setEnabled(false);
            String commentText = String.valueOf(mAddCommentField.getText());
            String commentContext = mStepid == -1 ? "guide" : "step";
            int commentContextid = mStepid == -1 ? mGuideid : mStepid;
            Object parentid = mAddCommentField.getTag(R.id.comment_parent_id);

            if (commentText.length() > 0) {
               if (parentid != null) {
                  Api.call(CommentsActivity.this, ApiCall.newComment(commentText, commentContext, commentContextid,
                   (Integer) parentid));
               } else {
                  Api.call(CommentsActivity.this, ApiCall.newComment(commentText, commentContext, commentContextid));
               }
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
            Intent data = new Intent();
            data.putExtra(GuideViewActivity.COMMENTS_TAG, mComments);
            if (getParent() == null) {
               setResult(Activity.RESULT_OK, data);
            } else {
               getParent().setResult(Activity.RESULT_OK, data);
            }
            finish();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Subscribe
   public void onCommentDelete(CommentDeleteEvent event) {
      mCommentToDelete = event.comment;
      Api.call(this, ApiCall.deleteComment(event.comment.mCommentid));
   }

   @Subscribe
   public void onCommentDeleted(ApiEvent.DeleteComment event) {
      if (!event.hasError()) {
         for (Iterator<Comment> it = mComments.iterator(); it.hasNext(); ) {
            Comment comment = it.next();
            if (comment.mCommentid == mCommentToDelete.mCommentid) {
               it.remove();
               break;
            } else {
               for (Iterator<Comment> rit = comment.mReplies.iterator(); rit.hasNext(); ) {
                  Comment reply = rit.next();
                  if (reply.mCommentid == mCommentToDelete.mCommentid) {
                     rit.remove();
                     break;
                  }
               }
            }
         }

         mAdapter.setComments(mComments);
         mAdapter.notifyDataSetChanged();
      } else {
         Toast.makeText(getBaseContext(), R.string.error_deleting_comment, Toast.LENGTH_SHORT).show();
      }

      mCommentToDelete = null;
   }

   @Subscribe
   public void onCommentEditing(final CommentEditEvent event) {
      View viewRoot = findViewById(event.comment.mCommentid);
      final View editContainer = viewRoot.findViewById(R.id.edit_comment_container);
      final EditText editCommentField = (EditText) viewRoot.findViewById(R.id.edit_comment_text);
      final ViewSwitcher switcher = (ViewSwitcher) viewRoot.findViewById(R.id.edit_comment_switcher);

      switcher.showNext();

      editContainer.setVisibility(View.VISIBLE);
      editCommentField.setText(event.comment.mTextRaw);

      viewRoot.findViewById(R.id.save_edit_comment_button).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String updatedText = editCommentField.getText().toString();
            Log.d("api", updatedText);
            // Fire off the edit request only if the comment was changed
            if (!updatedText.equals(event.comment.mTextRaw)) {
               Log.d("api", "text changed");
               Api.call(CommentsActivity.this, ApiCall.editComment(updatedText, event.comment.mCommentid));
            }
         }
      });

      viewRoot.findViewById(R.id.exit_comment_edit_button).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            switcher.showPrevious();
            editContainer.setVisibility(View.GONE);
            editCommentField.setText("");
         }
      });
   }

   @Subscribe
   public void onCommentEdited(ApiEvent.EditComment event) {
      if (!event.hasError()) {
         Comment comment = event.getResult();

         for (Comment c : mComments) {
            if (comment.isReply() && c.mCommentid == comment.mParentid) {
               for (int i = 0; i < c.mReplies.size(); i++) {
                  if (c.mReplies.get(i).mCommentid == comment.mCommentid) {
                     c.mReplies.get(i).mTextRaw = comment.mTextRaw;
                     c.mReplies.get(i).mTextRendered = comment.mTextRendered;
                     break;
                  }
               }
               break;
            } else if (comment.mCommentid == c.mCommentid) {
               c.mTextRaw = comment.mTextRaw;
               c.mTextRendered = comment.mTextRendered;
            }
         }

         mAdapter.setComments(mComments);
         mAdapter.notifyDataSetChanged();
/*
         View viewRoot = findViewById(comment.mCommentid);
         final ViewSwitcher switcher = (ViewSwitcher) viewRoot.findViewById(R.id.edit_comment_switcher);

         switcher.showNext();*/
      } else {
         Toast.makeText(getBaseContext(), event.getError().mMessage, Toast.LENGTH_SHORT).show();
      }
   }

   @Subscribe
   public void onCommentReplying(CommentReplyingEvent event) {
      mAddCommentField.setHint(R.string.add_reply);
      mAddCommentField.requestFocus();

      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.showSoftInput(mAddCommentField, InputMethodManager.SHOW_IMPLICIT);

      mAddCommentField.setTag(R.id.comment_parent_id, event.parent.mCommentid);
   }

   @Subscribe
   public void onCommentAdd(ApiEvent.AddComment event) {
      if (!event.hasError()) {
         int position = 0;
         Comment comment = event.getResult();
         if (comment.isReply()) {
            for (Comment c : mComments) {
               if (c.mCommentid == comment.mParentid) {
                  c.mReplies.add(c.mReplies.size(), comment);
                  break;
               }

               position++;
            }
         } else {
            mComments.add(mComments.size(), comment);
            position = mComments.size();
         }

         mAdapter.setComments(mComments);
         mAdapter.notifyDataSetChanged();
         scrollCommentsToPosition(position);
         mAddCommentField.setText("");
         mAddCommentField.setHint(R.string.add_comment);
         mAddCommentField.setTag(R.id.comment_parent_id, null);
      } else {
         Toast.makeText(getBaseContext(), event.getError().mMessage, Toast.LENGTH_SHORT).show();
      }

      mAddCommentField.setEnabled(true);
   }

   private void scrollCommentsToPosition(final int position) {
      new Handler().post(new Runnable() {
         @Override
         public void run() {
            mCommentsList.setSelection(position);
         }
      });
   }
}
