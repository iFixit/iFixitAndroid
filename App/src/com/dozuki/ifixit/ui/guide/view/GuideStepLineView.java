package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.StepLine;
import com.dozuki.ifixit.util.JSONHelper;

public class GuideStepLineView extends LinearLayout {
   private static final int LINE_INDENT = 50;
   private static final int MARGIN = 10;

   private TextView mStepText;
   private ImageView mBulletView;
   private ImageView mIconView;

   public GuideStepLineView(Context context) {
      super(context);

      setOrientation(HORIZONTAL);
      setWeightSum(100);

      LayoutInflater.from(context).inflate(R.layout.guide_step_row, this, true);
   }

   public void setLine(StepLine line) {
      int iconRes, bulletRes;

      setPadding(LINE_INDENT * line.getLevel(), MARGIN, 0, MARGIN);

      mStepText = (TextView) findViewById(R.id.step_text);
      mStepText.setText(JSONHelper.correctLinkPaths(Html.fromHtml(
       line.getTextRendered())));
      mStepText.setMovementMethod(LinkMovementMethod.getInstance());

      mBulletView = (ImageView)findViewById(R.id.bullet);
      bulletRes = getBulletResource(line.getColor());
      mBulletView.setImageResource(bulletRes);
      mBulletView.setLayoutParams(new LinearLayout.LayoutParams(
       LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));

      mIconView = (ImageView) findViewById(R.id.bullet_icon);

      if (line.hasIcon()) {
         if (line.getColor().equals("icon_reminder")) {
            iconRes = R.drawable.icon_reminder;
         } else if (line.getColor().equals("icon_caution")) {
            iconRes = R.drawable.icon_caution;
         } else if (line.getColor().equals("icon_note")) {
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

      if (color.equals("black")) {
         iconRes = R.drawable.bullet_black;
      } else if (color.equals("orange")) {
         iconRes = R.drawable.bullet_orange;
      } else if (color.equals("blue")) {
         iconRes = R.drawable.bullet_blue;
      } else if (color.equals("violet")) {
         iconRes = R.drawable.bullet_purple;
      } else if (color.equals("green")) {
         iconRes = R.drawable.bullet_green;
      } else if (color.equals("red")) {
         iconRes = R.drawable.bullet_red;
      } else if (color.equals("white")) {
         iconRes = R.drawable.bullet_white;
      } else if (color.equals("yellow")) {
         iconRes = R.drawable.bullet_yellow;
      } else {
         iconRes = R.drawable.bullet_black;
      }

      return iconRes;
   }
}
