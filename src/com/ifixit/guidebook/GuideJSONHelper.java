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
         JSONArray jTools = jGuide.getJSONArray("tools");
         JSONArray jParts = jGuide.getJSONArray("parts");
         JSONObject jAuthor = jGuide.getJSONObject("author");
         JSONObject jImage = jGuide.getJSONObject("image");
         Guide guide = new Guide(jGuideInfo.getInt("guideid"));

         guide.setTitle(jGuide.getString("title"));
         guide.setDevice(jGuideInfo.getString("device"));
         guide.setAuthor(jAuthor.getString("text"));
         guide.setTimeRequired(jGuide.getString("time_required"));
         guide.setDifficulty(jGuide.getString("difficulty"));
         guide.setIntroduction(jGuide.getString("introduction"));
         guide.setIntroImage(jImage.getString("text"));
         guide.setSummary(jGuide.getString("summary"));

         for (int i = 0; i < jSteps.length(); i++) {
            guide.addStep(parseStep(jSteps.getJSONObject(i)));
         }
         
         for (int i = 0; i < jTools.length(); i++) {
            guide.addTool(parseTool(jTools.getJSONObject(i)));
         }
         
         for (int i = 0; i < jParts.length(); i++) {
            guide.addPart(parsePart(jParts.getJSONObject(i)));
         }

         return guide;
      }
      catch (JSONException e) {
         Log.e("iFixit", "Error parsing guide: " + e);
         return null;
      }
   }
   
   public static GuidePart parsePart(JSONObject jPart) throws JSONException {
      return new GuidePart(jPart.getString("text"), jPart.getString("url"),
       jPart.getString("thumbnail"), jPart.getString("notes"));
   }

   public static GuideTool parseTool(JSONObject jTool) throws JSONException {
      return new GuideTool(jTool.getString("text"), jTool.getString("url"),
       jTool.getString("thumbnail"), jTool.getString("notes"));
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

      // last image doesn't have orderby so this is necessary. API bug?
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
      return new StepLine(jLine.getString("bullet"), jLine.getInt("level"), jLine.getString("text"));
   }
}
