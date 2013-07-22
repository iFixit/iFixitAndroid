package com.dozuki.ifixit.ui.guide.create;

import com.dozuki.ifixit.model.Image;

public class StepImageDeleteEvent {
   public final Image image;

   public StepImageDeleteEvent(Image image) {
      this.image = image;
   }
}
