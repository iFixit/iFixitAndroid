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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.PicassoUtils;
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
       mContainer = (RelativeLayout)findViewById(R.id.comment_row_wrap);
    }

    public void buildView(final Comment comment) {
       final boolean reply = comment.mParentid != NO_PARENT_ID;

       User currentUser = MainApplication.get().getUser();

       final boolean commentOwner = currentUser != null &&
        comment.mUser.getUserid() == currentUser.getUserid();

       if (reply) {
          mContainer.setBackgroundResource(R.color.subtle_gray);

          // Can't reply to replies, so remove the form
          mContainer.removeView(findViewById(R.id.add_reply_container));
       }

       if (comment.isReplying()) {
          toggleReplyForm(true);
       }

       SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");
       String commmentDetails =
        MainApplication.get().getString(R.string.by_on_comment_details, "<b>" + comment.mUser.getUsername() +
         "</b>", df.format(comment.mDate));

       TextView commentText = (TextView) findViewById(R.id.comment_text);
       commentText.setText(Html.fromHtml(comment.mTextRendered));
       ((TextView) findViewById(R.id.comment_details)).setText(Html.fromHtml(commmentDetails));

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
       ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.edit_comment_switcher);
       switcher.showNext();
       ((EditText) findViewById(R.id.edit_comment_text)).setText(comment.mTextRaw);

       // MainApplication.getBus().post(new CommentEditEvent(comment));
    }

    private void deleteComment(Comment comment) {
       MainApplication.getBus().post(new CommentDeleteEvent(comment));
    }

    private void replyToComment(final Comment parent) {
       toggleReplyForm(true);

       findViewById(R.id.add_comment_button).setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
             String text = ((TextView) findViewById(R.id.add_comment_field)).getText().toString();
             MainApplication.getBus().post(new CommentReplyEvent(text, parent));
          }
       });
    }

   private void toggleReplyForm(boolean show) {
      findViewById(R.id.add_reply_container).setVisibility(show ? View.VISIBLE : View.GONE);
   }
 }
