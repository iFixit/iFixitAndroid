package com.dozuki.ifixit.ui.guide;

import com.dozuki.ifixit.model.Comment;

public class CommentEditEvent {
   public Comment comment;

   public CommentEditEvent(Comment comment) {
      this.comment = comment;
   }
}
