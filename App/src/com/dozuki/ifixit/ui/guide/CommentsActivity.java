package com.dozuki.ifixit.ui.guide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.user.LoginEvent;
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
   private static final String CONTEXTID = "CONTEXTID_KEY";
   private static final String CONTEXT = "CONTEXT_KEY";
   private static final String GUIDEID_KEY = "GUIDEID_KEY";

   private ArrayList<Comment> mComments;
   private String mTitle;
   private CommentsAdapter mAdapter;
   private ListView mCommentsList;
   private EditText mAddCommentField;
   private String mCommentContext;
   private int mCommentContextId;
   private int mGuideid;
   private ImageButton mAddCommentButton;
   private ProgressBar mAddCommentProgress;

   public static Intent viewComments(Context context, ArrayList<Comment> comments, String title,
    String commentContext, int contextid) {

      Intent intent = new Intent(context, CommentsActivity.class);

      intent.putExtra(TITLE_KEY, title);
      intent.putExtra(CONTEXTID, contextid);
      intent.putExtra(CONTEXT, commentContext);
      intent.putExtra(COMMENTS_KEY, comments);

      return intent;
   }

   public static Intent viewGuideComments(Context context, ArrayList<Comment> comments, String title,
    String commentContext, int contextid, int guideid) {

      Intent intent = viewComments(context, comments, title, commentContext, contextid);
      intent.putExtra(GUIDEID_KEY, guideid);

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

      if (savedInstanceState != null) {
         mComments = (ArrayList<Comment>) savedInstanceState.getSerializable(COMMENTS_KEY);
         mCommentContext = savedInstanceState.getString(CONTEXT);
         mCommentContextId = savedInstanceState.getInt(CONTEXTID);
         mTitle = savedInstanceState.getString(TITLE_KEY);
         mGuideid = savedInstanceState.getInt(GUIDEID_KEY, 0);

      } else if (args != null) {
         mComments = (ArrayList<Comment>) args.getSerializable(COMMENTS_KEY);
         mCommentContext = args.getString(CONTEXT);
         mCommentContextId = args.getInt(CONTEXTID);
         mTitle = args.getString(TITLE_KEY);
         mGuideid = args.getInt(GUIDEID_KEY, 0);
      } else {
         mTitle = getString(R.string.comments);
      }

      mAddCommentField = (EditText) findViewById(R.id.add_comment_field);
      mAddCommentProgress = (ProgressBar) findViewById(R.id.add_comment_progress);
      mAddCommentButton = (ImageButton) findViewById(R.id.add_comment_button);
      mAddCommentButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String commentText = String.valueOf(mAddCommentField.getText());
            Object parentid = mAddCommentField.getTag(R.id.comment_parent_id);

            if (commentText.length() > 0) {
               mAddCommentField.setEnabled(false);
               mAddCommentButton.setVisibility(View.GONE);
               mAddCommentProgress.setVisibility(View.VISIBLE);

               if (parentid != null) {
                  Api.call(CommentsActivity.this, ApiCall.newComment(commentText, mCommentContext, mCommentContextId,
                   (Integer) parentid));
               } else {
                  Api.call(CommentsActivity.this, ApiCall.newComment(commentText, mCommentContext, mCommentContextId));
               }
            }
         }
      });

      mCommentsList = (ListView) findViewById(R.id.comment_list);
      mCommentsList.setEmptyView(findViewById(android.R.id.empty));

      mAdapter = new CommentsAdapter(this, mComments);
      mCommentsList.setAdapter(mAdapter);

      setTitle(mTitle);

      if (App.get().isUserLoggedIn()) {
         if (mCommentContext.equalsIgnoreCase("guide") || mCommentContext.equalsIgnoreCase("step")) {
            Api.call(this, ApiCall.guide(mGuideid));
         } else {
            // TODO: Get wiki comments once we add those endpoints.
         }
      }
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(COMMENTS_KEY, mComments);
      state.putInt(CONTEXTID, mCommentContextId);
      state.putString(CONTEXT, mCommentContext);
      state.putString(TITLE_KEY, mTitle);
      state.putInt(GUIDEID_KEY, mGuideid);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         // Respond to the action bar's Up/Home button
         case android.R.id.home:
            finishCommentsActivity();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public void onBackPressed() {
      finishCommentsActivity();
   }

   private void finishCommentsActivity() {
      Intent data = new Intent();
      data.putExtra(GuideViewActivity.COMMENTS_TAG, mComments);
      if (getParent() == null) {
         setResult(Activity.RESULT_OK, data);
      } else {
         getParent().setResult(Activity.RESULT_OK, data);
      }
      finish();
   }

   @Subscribe
   public void onGuideGet(ApiEvent.ViewGuide event) {
      if (!event.hasError()) {
         Guide guide = event.getResult();
         if (mCommentContext.equalsIgnoreCase("guide")) {
            mComments = guide.getComments();
         } else if (mCommentContext.equalsIgnoreCase("step")) {
            mComments = guide.getStepById(mCommentContextId).getComments();
         }

         mAdapter.setComments(mComments);
         mAdapter.notifyDataSetChanged();
      }
   }

   @Subscribe
   public void onCommentDelete(CommentDeleteEvent event) {
      Api.call(this, ApiCall.deleteComment(event.comment.mCommentid));
   }

   @Subscribe
   public void onCommentDeleted(ApiEvent.DeleteComment event) {
      if (!event.hasError()) {
         int commentIdToDelete = Integer.parseInt(event.getExtraInfo());
         for (Iterator<Comment> it = mComments.iterator(); it.hasNext(); ) {
            Comment comment = it.next();
            if (comment.mCommentid == commentIdToDelete) {
               it.remove();
               break;
            } else {
               for (Iterator<Comment> rit = comment.mReplies.iterator(); rit.hasNext(); ) {
                  Comment reply = rit.next();
                  if (reply.mCommentid == commentIdToDelete) {
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
   }

   @Subscribe
   public void onCommentEditing(final CommentEditEvent event) {
      final View viewRoot = findViewById(event.comment.mCommentid);
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
            // Fire off the edit request only if the comment was changed
            if (!updatedText.equals(event.comment.mTextRaw)) {
               viewRoot.findViewById(R.id.comment_progress).setVisibility(View.VISIBLE);
               viewRoot.findViewById(R.id.comment_menu).setVisibility(View.GONE);
               v.setEnabled(false);

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
               for (Comment reply : c.mReplies) {
                  if (reply.mCommentid == comment.mCommentid) {
                     reply.mTextRaw = comment.mTextRaw;
                     reply.mTextRendered = comment.mTextRendered;
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
      } else {
         Toast.makeText(getBaseContext(), event.getError().mMessage, Toast.LENGTH_SHORT).show();
      }
      
      mAdapter.notifyDataSetChanged();
   }

   @Subscribe
   public void onCommentReplying(CommentReplyingEvent event) {
      mAddCommentField.setHint(R.string.add_reply);
      mAddCommentField.requestFocus();

      showSoftKeyboard();

      mAddCommentField.setTag(R.id.comment_parent_id, event.parentid);

      Button exitReply = (Button) findViewById(R.id.exit_comment_reply_button);
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 7f);
      mAddCommentField.setLayoutParams(params);
      exitReply.setVisibility(View.VISIBLE);
      exitReply.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            resetCommentField(false);
            v.setVisibility(View.GONE);
         }
      });
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
         resetCommentField(false);
      } else {
         Toast.makeText(getBaseContext(), event.getError().mMessage, Toast.LENGTH_SHORT).show();
      }

      mAddCommentField.setEnabled(true);
      mAddCommentButton.setVisibility(View.VISIBLE);
      mAddCommentProgress.setVisibility(View.GONE);
   }

   @Subscribe
   public void onCancelLogin(LoginEvent.Cancel event) {
      resetCommentField(true);
   }

   private void resetCommentField(boolean keepText) {
      if (!keepText) {
         mAddCommentField.setText("");
         mAddCommentField.setHint(R.string.add_comment);
         mAddCommentField.setTag(R.id.comment_parent_id, null);
         findViewById(R.id.exit_comment_reply_button).setVisibility(View.GONE);
      }
      mAddCommentField.setEnabled(true);

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 8f);
      mAddCommentField.setLayoutParams(params);
      mAddCommentButton.setVisibility(View.VISIBLE);
      mAddCommentProgress.setVisibility(View.GONE);
   }

   private void scrollCommentsToPosition(final int position) {
      new Handler().post(new Runnable() {
         @Override
         public void run() {
            mCommentsList.setSelection(position);
         }
      });
   }

   private void showSoftKeyboard() {
      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.toggleSoftInputFromWindow(mAddCommentField.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
   }
}
