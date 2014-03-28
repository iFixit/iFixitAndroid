package com.dozuki.ifixit.ui.guide;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.PicassoUtils;
import com.dozuki.ifixit.util.Utils;
import com.dozuki.ifixit.util.transformations.CircleTransformation;

import java.text.SimpleDateFormat;

public class CommentView extends RelativeLayout {
   private static final int NO_PARENT_ID = -1;
   private static final int REPLY_OPTION = 0;
   private static final int EDIT_OPTION = 1;
   private static final int DELETE_OPTION = 2;
   private RelativeLayout mContainer;
   private Context mContext;

   public CommentView(Context context) {
      super(context);

      LayoutInflater.from(context).inflate(R.layout.comment_row, this, true);

      mContext = context;
      mContainer = (RelativeLayout) findViewById(R.id.comment_row_wrap);
   }

   public void buildView(final Comment comment) {
      // Set the root view id as the commentid so we can easily reference the correct comment when editing a comment.
      setId(comment.mCommentid);

      final boolean reply = comment.mParentid != NO_PARENT_ID;

      User currentUser = App.get().getUser();

      final boolean commentOwner = currentUser != null &&
       comment.mUser.getUserid() == currentUser.getUserid();

      if (reply) {
         mContainer.setBackgroundResource(R.color.subtle_gray);
      }

      SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");
      String commentDetails =
       App.get().getString(R.string.by_on_comment_details, "<b>" + comment.mUser.getUsername() +
        "</b>", df.format(comment.mDate));

      TextView commentText = (TextView) findViewById(R.id.comment_text);
      Spanned commentHtml = Html.fromHtml(comment.mTextRendered);
      commentText.setText(Utils.trim(commentHtml, 0, commentHtml.length()));

      ((TextView) findViewById(R.id.comment_details)).setText(Html.fromHtml(commentDetails));

      ImageView avatar = (ImageView) findViewById(R.id.comment_author);

      Image avatarImage = comment.mUser.getAvatar();

      if (avatarImage != null) {
         PicassoUtils
          .with(mContext)
          .load(avatarImage.getPath("thumbnail"))
          .error(R.drawable.no_image)
          .fit()
          .centerInside()
          .transform(new CircleTransformation())
          .into(avatar);
      }

      final View menuButton = findViewById(R.id.comment_menu);

      // if there are options, show the menu button
      if (reply && !commentOwner) {
         menuButton.setVisibility(View.GONE);
      }

      menuButton.setOnClickListener(new View.OnClickListener() {
         @Override
         @SuppressWarnings("NewApi") // Suppress the warning because we already do an API check
         public void onClick(View v) {
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
                           editComment(comment);
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

               // If the comment is areply, hide reply option
               menu.findItem(R.id.comment_item_reply).setVisible(!reply);
               menu.findItem(R.id.comment_item_delete).setVisible(commentOwner);
               menu.findItem(R.id.comment_item_edit).setVisible(commentOwner);

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
                           editComment(comment);
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
   }

   private void editComment(Comment comment) {
      App.getBus().post(new CommentEditEvent(comment));
   }

   private void deleteComment(Comment comment) {
      findViewById(R.id.comment_menu).setVisibility(View.GONE);
      ProgressBar progress = (ProgressBar)findViewById(R.id.comment_progress);
      progress.setVisibility(View.VISIBLE);
      App.getBus().post(new CommentDeleteEvent(comment));
   }

   private void replyToComment(Comment parent) {
      App.getBus().post(new CommentReplyingEvent(parent));
   }
}
