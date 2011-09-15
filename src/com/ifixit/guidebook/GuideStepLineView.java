package com.ifixit.guidebook;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuideStepLineView extends LinearLayout {
   private static final int LINE_INDENT = 50;
   private static final int MARGIN = 10;
   
   private TextView mStepText;
   private BulletView mBulletView;
   private LinearLayout mRow;
   
   public GuideStepLineView(Context context) {
      super(context);
      
      LayoutInflater inflater = (LayoutInflater)context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.step_row, this, true);  
      
      mRow = (LinearLayout)findViewById(R.id.step_row);
   }   

   public void setLine(StepLine line) {
      mStepText = (TextView)findViewById(R.id.step_text);
      mStepText.setText(Html.fromHtml(line.getText()));

      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
       LinearLayout.LayoutParams.FILL_PARENT, 
       LinearLayout.LayoutParams.WRAP_CONTENT);
      
      layoutParams.setMargins(LINE_INDENT*line.getLevel(), MARGIN, 0, MARGIN);    
      mRow.setLayoutParams(layoutParams);
      
      mBulletView = (BulletView)findViewById(R.id.bullet);
      mBulletView.setBullet(line.getColor());
   }
}
