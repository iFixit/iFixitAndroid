package com.dozuki.ifixit;

import android.content.Context;
import android.text.Html;

import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuideStepLineView extends LinearLayout {
   private static final int LINE_INDENT = 50;
   private static final int MARGIN = 10;

   private TextView mStepText;
   private ImageView mBulletView;
   private ImageView mIconView;
   private LinearLayout mRow;

   public GuideStepLineView(Context context) {
      super(context);

      LayoutInflater inflater = (LayoutInflater) context
       .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.step_row, this, true);

      mRow = (LinearLayout) findViewById(R.id.step_row);
   }

   public void setLine(StepLine line) {
      int iconRes, bulletRes;

      mStepText = (TextView) findViewById(R.id.step_text);
      mStepText.setText(Html.fromHtml(line.getText()));
      mStepText.setMovementMethod(LinkMovementMethod.getInstance());

      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
       LinearLayout.LayoutParams.FILL_PARENT,
       LinearLayout.LayoutParams.WRAP_CONTENT);

      layoutParams.setMargins(LINE_INDENT * line.getLevel(), MARGIN, 0,
       MARGIN);
      mRow.setLayoutParams(layoutParams);

      mBulletView = (ImageView) findViewById(R.id.bullet);
      bulletRes = this.getBulletResource(line.getColor());
      mBulletView.setImageResource(bulletRes);
      mBulletView.setLayoutParams(new LinearLayout.LayoutParams(
       LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));

      mIconView = (ImageView) findViewById(R.id.bullet_icon);

      if (line.hasIcon) {
         if (line.mColor.compareTo("icon_reminder") == 0) {
            iconRes = R.drawable.icon_reminder;
         } else if (line.mColor.compareTo("icon_caution") == 0) {
            iconRes = R.drawable.icon_caution;
         } else if (line.mColor.compareTo("icon_note") == 0) {
            iconRes = R.drawable.icon_note;
         } else {
            Log.e("setLine", "Step icon resource not there");
            iconRes = 0;
         }

         mIconView.setImageResource(iconRes);
         mIconView.setVisibility(VISIBLE);
         mIconView.setLayoutParams(new LinearLayout.LayoutParams(
          LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
      } else {
         mIconView.setVisibility(INVISIBLE);
      }
   }

   public int getBulletResource(String color) {
      int iconRes;

      if (color.equals("black"))
         iconRes = R.drawable.bullet_white_dark;
      else if (color.equals("orange"))
         iconRes = R.drawable.bullet_orange_dark;
      else if (color.equals("blue"))
         iconRes = R.drawable.bullet_blue_dark;
      else if (color.equals("purple"))
         iconRes = R.drawable.bullet_purple_dark;
      else if (color.equals("red"))
         iconRes = R.drawable.bullet_red_dark;
      else if (color.equals("teal"))
         iconRes = R.drawable.bullet_teal_dark;
      else if (color.equals("white"))
         iconRes = R.drawable.bullet_white_dark;
      else if (color.equals("yellow"))
         iconRes = R.drawable.bullet_yellow_dark;
      else
         iconRes = R.drawable.bullet_white_dark;

      return iconRes;
   }
}
