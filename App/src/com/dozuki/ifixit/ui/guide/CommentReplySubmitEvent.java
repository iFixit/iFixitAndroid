package com.dozuki.ifixit.ui.guide;

import com.dozuki.ifixit.model.Comment;

public class CommentReplySubmitEvent {
   public Comment parent;
   public String text;

   public CommentReplySubmitEvent(String text, Comment parent) {
      this.text = text;
      this.parent = parent;
   }
}
