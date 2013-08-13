package com.dozuki.ifixit.ui.guide.create;

public class StepLineValidationEvent {
   public int index;
   public int stepid;

   public StepLineValidationEvent(int stepid, int index) {
      this.index = index;
      this.stepid = stepid;
   }
}
