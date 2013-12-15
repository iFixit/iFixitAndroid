package com.dozuki.ifixit.ui.guide.create;

public class StepTitleChangedEvent {
   public int stepid;
   public String title;

   public StepTitleChangedEvent(int stepId, String title) {
      this.stepid = stepId;
      this.title = title;
   }
}
