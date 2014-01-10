package com.dozuki.ifixit.ui.guide;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.transformations.CircleTransformation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CommentsAdapter extends BaseAdapter {
   private static final int NO_PARENT_ID = -1;
   private static final int REPLY_OPTION = 0;
   private static final int EDIT_OPTION = 1;
   private static final int DELETE_OPTION = 2;

   private Context mContext;
   private ArrayList<Comment> mComments;
   private final LayoutInflater mInflater;

   public CommentsAdapter(Context context, ArrayList<Comment> comments) {
      mComments = comments;
      mContext = context;
      mInflater = LayoutInflater.from(context);
   }

   @Override
   public int getCount() {
      return mComments.size();
   }

   @Override
   public Object getItem(int position) {
      return mComments.get(position);
   }

   @Override
   public long getItemId(int position) {
      return position;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      View v;

      if (convertView == null) {
         v = mInflater.inflate(R.layout.comment_row, parent, false);
      } else {
         v = convertView;
      }

      Comment comment = (Comment) getItem(position);

      v = buildComment(comment, v, false);

      LinearLayout replyContainer = (LinearLayout) v.findViewById(R.id.comment_replies);
      replyContainer.removeAllViews();

      for (Comment reply : comment.mReplies) {
         if (reply.mParentid == comment.mCommentid) {
            View replyView = mInflater.inflate(R.layout.comment_row, replyContainer, false);

            if (replyView != null) {
               replyView = buildComment(reply, replyView, true);
               replyView.setPadding(0, 0, 0, 0);
               replyContainer.addView(replyView);
            }
         }
      }

      replyContainer.setVisibility(replyContainer.getChildCount() != 0 ? View.VISIBLE : View.GONE);

      return v;
   }

   private View buildComment(final Comment comment, final View view, boolean isReply) {
      if (isReply) {
         view.setBackgroundResource(android.R.color.transparent);
      }

      SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");
      String commmentDetails =
       MainApplication.get().getString(R.string.by_on_comment_details, "<b>" + comment.mUser.getUsername() + "</b>",
        df.format(comment.mDate));

      TextView commentText = (TextView) view.findViewById(R.id.comment_text);

      commentText.setText(Html.fromHtml(comment.mTextRendered));
      ((TextView) view.findViewById(R.id.comment_details)).setText(Html.fromHtml(commmentDetails));

      ImageView avatar = (ImageView) view.findViewById(R.id.comment_author);

      PicassoUtils
       .with(mContext)
       .load(comment.mUser.getAvatar().getPath(".thumbnail"))
       .error(R.drawable.no_image)
       .fit()
       .centerInside()
       .transform(new CircleTransformation())
       .into(avatar);

      final View menuButton = view.findViewById(R.id.comment_menu);

      menuButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            User currentUser = MainApplication.get().getUser();

            boolean commentOwner = currentUser != null &&
             comment.mUser.getUserid() == currentUser.getUserid();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
               PopupMenu itemMenu = new PopupMenu(mContext, menuButton);

               itemMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                  @Override
                  public boolean onMenuItemClick(MenuItem item) {
                     switch (item.getItemId()) {
                        case R.id.comment_item_reply:
                           replyToComment(comment);
                           break;
                        case R.id.comment_item_edit:
                           editComment(comment, view);
                           break;
                        case R.id.comment_item_delete:
                           deleteComment(comment);
                           break;
                     }

                     return true;
                  }
               });

               Menu menu = itemMenu.getMenu();
               MenuInflater menuInflater = itemMenu.getMenuInflater();
               menuInflater.inflate(R.menu.comment_item_popup, menu);

               boolean reply = comment.mParentid != NO_PARENT_ID;
               // If the comment is a reply, hide reply option
               menu.findItem(R.id.comment_item_reply).setVisible(!reply);
               menu.findItem(R.id.comment_item_delete).setVisible(commentOwner);
               menu.findItem(R.id.comment_item_edit).setVisible(commentOwner);

               // if there are no options, hide the menu
               if (reply && !commentOwner) {
                  menuButton.setVisibility(View.GONE);
               }

               itemMenu.show();
            } else {
               // PopupMenu was added in API 11, so let's use an AlertDialog instead.
               AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
               builder.setItems(commentOwner ? R.array.comment_owner_options
                : R.array.comment_options, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                     switch (which) {
                        case REPLY_OPTION:
                           replyToComment(comment);
                           break;
                        case EDIT_OPTION:
                           editComment(comment, view);
                           break;
                        case DELETE_OPTION:
                           deleteComment(comment);
                           break;
                     }
                  }
               });
               builder.create();
               builder.show();
            }
         }
      });

      return view;
   }

   private void editComment(Comment comment, View view) {
      ViewSwitcher switcher = (ViewSwitcher) view.findViewById(R.id.edit_comment_switcher);
      switcher.showNext();
      ((EditText) view.findViewById(R.id.edit_comment_text)).setText(comment.mTextRaw);
      //Api.call(mContext, ApiCall.editComment(comment.mCommentid));
   }

   private void deleteComment(Comment comment) {

   }

   private void replyToComment(Comment comment) {

   }

   public void setComments(ArrayList<Comment> comments) {
      mComments = comments;
   }
}
