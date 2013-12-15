package com.dozuki.ifixit.ui.guide.create;

import java.util.ArrayList;

public class StepMicCompleteEvent {
   public ArrayList<String> results;
   public int stepid;

   public StepMicCompleteEvent(ArrayList<String> results, int stepid) {
      this.results = new ArrayList<String>(results);
      this.stepid = stepid;
   }
}
