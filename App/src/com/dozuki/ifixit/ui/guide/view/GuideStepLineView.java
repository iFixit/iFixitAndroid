package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.StepLine;
import com.dozuki.ifixit.util.Utils;
import com.dozuki.ifixit.util.WikiHtmlTagHandler;

public class GuideStepLineView extends LinearLayout {
   private static final int LINE_INDENT = 50;
   private static final int MARGIN = 10;

   public GuideStepLineView(Context context) {
      super(context);

      setOrientation(HORIZONTAL);
      setWeightSum(100);

      LayoutInflater.from(context).inflate(R.layout.guide_step_row, this, true);
   }

   public void setLine(StepLine line) {
      int iconRes, bulletRes;

      setPadding(LINE_INDENT * line.getLevel(), MARGIN, 0, MARGIN);

      TextView stepText = (TextView) findViewById(R.id.step_text);
      stepText.setText(Utils.correctLinkPaths(
       Html.fromHtml(line.getTextRendered(), null, new WikiHtmlTagHandler())));
      stepText.setMovementMethod(LinkMovementMethod.getInstance());

      ImageView bullet = (ImageView)findViewById(R.id.bullet);

      bulletRes = getBulletResource(line.getColor());
      bullet.setImageResource(bulletRes);
      if (!line.hasIcon()) {
         int bulletSize = (int)Utils.pxFromDp(getContext(), 16);
         bullet.getLayoutParams().width = bulletSize;
         bullet.getLayoutParams().height = bulletSize;
      }
   }

   public int getBulletResource(String color) {
      int iconRes;

      if (color.equals("icon_reminder")) {
         iconRes = R.drawable.icon_reminder;
      } else if (color.equals("icon_caution")) {
         iconRes = R.drawable.icon_caution;
      } else if (color.equals("icon_note")) {
         iconRes = R.drawable.icon_note;
      } else if (color.equals("black")) {
         iconRes = R.drawable.bullet_black;
      } else if (color.equals("orange")) {
         iconRes = R.drawable.bullet_orange;
      } else if (color.equals("light_blue")) {
         iconRes = R.drawable.bullet_light_blue;
      } else if (color.equals("blue")) {
         iconRes = R.drawable.bullet_blue;
      } else if (color.equals("violet")) {
         iconRes = R.drawable.bullet_violet;
      } else if (color.equals("green")) {
         iconRes = R.drawable.bullet_green;
      } else if (color.equals("red")) {
         iconRes = R.drawable.bullet_red;
      } else if (color.equals("yellow")) {
         iconRes = R.drawable.bullet_yellow;
      } else {
         iconRes = R.drawable.bullet_black;
      }

      return iconRes;
   }
}
