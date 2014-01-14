package com.dozuki.ifixit.ui.guide;

import com.dozuki.ifixit.model.Comment;

public class CommentReplyEvent {
   public Comment parent;
   public String text;

   public CommentReplyEvent(String text, Comment parent) {
      this.text = text;
      this.parent = parent;
   }
}
