package com.ifixit.guidebook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GuideJSONHelper {
   public static Guide parseGuide(String json) {
      try {
         JSONObject jGuideInfo = new JSONObject(json);
         JSONObject jGuide = jGuideInfo.getJSONObject("guide");
         JSONArray jSteps = jGuide.getJSONArray("steps");
         Guide guide = new Guide(jGuideInfo.getInt("guideid"));

         guide.setTitle(jGuide.getString("title"));
         guide.setDevice(jGuideInfo.getString("device"));
         guide.setAuthor(jGuide.getString("author"));
         guide.setTimeRequired(jGuide.getString("time_required"));
         guide.setDifficulty(jGuide.getString("difficulty"));
         guide.setIntroduction(jGuide.getString("introduction"));
         guide.setSummary(jGuide.getString("summary"));

         for (int i = 0; i < jSteps.length(); i++) {
            guide.addStep(parseStep(jSteps.getJSONObject(i)));
         }

         return guide;
      }
      catch (JSONException e) {
         Log.e("iFixit", "Error parsing guide: " + e);
         return null;
      }
   }

   public static GuideStep parseStep(JSONObject jStep) throws JSONException {
      JSONArray jImages = jStep.getJSONArray("images");
      JSONArray jLines = jStep.getJSONArray("lines");
      GuideStep step = new GuideStep(jStep.getInt("number"));

      step.setTitle(jStep.getString("title"));

      for (int i = 0; i < jImages.length(); i++) {
         step.addImage(parseImage(jImages.getJSONObject(i)));
      }

      for (int i = 0; i < jLines.length(); i++) {
         step.addLine(parseLine(jLines.getJSONObject(i)));
      }

      return step;
   }

   public static StepImage parseImage(JSONObject jImage) throws JSONException {
      StepImage image = new StepImage(jImage.getInt("imageid"));

      //last image doesn't have orderby so this is necessary. Could be a bug?
      try {
         image.setOrderby(jImage.getInt("orderby"));
      }
      catch (JSONException e) {
         image.setOrderby(1);
      }

      image.setText(jImage.getString("text"));

      return image;
   }

   public static StepLine parseLine(JSONObject jLine) throws JSONException {
      StepLine line = new StepLine();

      line.setColor(jLine.getString("bullet"));
      line.setLevel(jLine.getInt("level"));
      line.setText(jLine.getString("text"));

      return line;
   }
}
