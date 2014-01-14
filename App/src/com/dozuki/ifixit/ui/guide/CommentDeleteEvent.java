package com.dozuki.ifixit.ui.guide;

import com.dozuki.ifixit.model.Comment;

public class CommentDeleteEvent {
   public Comment comment;
   public CommentDeleteEvent(Comment comment) {
      this.comment = comment;
   }
}
