package com.dozuki.ifixit.ui.guide.create;

import com.dozuki.ifixit.model.guide.Guide;

public class GuideDetailsChangedEvent {

   public Guide guide;

   public GuideDetailsChangedEvent(Guide guide) {
      this.guide = guide;
   }
}
