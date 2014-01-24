package com.dozuki.ifixit.ui.guide;

import com.dozuki.ifixit.model.Comment;

public class CommentReplyingEvent {
   public Comment parent;

   public CommentReplyingEvent(Comment parent) {
      this.parent = parent;
   }
}
