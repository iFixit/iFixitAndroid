package com.dozuki.ifixit.ui.guide.create;

import com.dozuki.ifixit.model.guide.StepLine;

import java.util.ArrayList;

public class StepLinesChangedEvent {
   public ArrayList<StepLine> lines;
   public int stepid;
   public StepLinesChangedEvent(int stepid, ArrayList<StepLine> lines) {
      this.lines = lines;
      this.stepid = stepid;
   }
}
