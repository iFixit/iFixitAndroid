package com.ifixit.android.ifixit;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GuideJSONHelper {
   private static final String LEAF_INDICATOR = "DEVICES";

   /**
    * Guide parsing
    */
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
      return new StepLine(jLine.getString("bullet"), jLine.getInt("level"),
       jLine.getString("text"));
   }

   /**
    * Device hierarchy parsing
    */
   public static ArrayList<Device> parseDevices(String json) {
      try {
         JSONObject jDevices = new JSONObject(json);

         return parseDeviceChildren(jDevices);
      }
      catch (Exception e) {
         Log.w("iFixit", "Error parsing devices: " + e.getMessage());
         return null;
      }
   }

   /**
    * Reads through the given JSONObject and adds any devices to the given
    * device
    */
   public static ArrayList<Device> parseDeviceChildren(JSONObject jDevice) {
      Iterator<String> iterator = jDevice.keys();
      String deviceName;
      ArrayList<Device> devices = new ArrayList<Device>();
      Device currentDevice;

      try {
         while (iterator.hasNext()) {
            deviceName = iterator.next();

            if (deviceName.equals(LEAF_INDICATOR)) {
               devices.addAll(parseDeviceLeaves(
                jDevice.getJSONArray(LEAF_INDICATOR)));
            }
            else {
               currentDevice = new Device(deviceName);
               currentDevice.addAllDevices(parseDeviceChildren(
                jDevice.getJSONObject(deviceName)));
               devices.add(currentDevice);
            }
         }
      } catch (Exception e) {
         Log.w("iFixit", "Error parsing device children: " + e.getMessage());
      }

      return devices;
   }

   public static ArrayList<Device> parseDeviceLeaves(JSONArray jLeaves) {
      ArrayList<Device> devices = new ArrayList<Device>();

      try {
         for (int i = 0; i < jLeaves.length(); i++) {
            devices.add(new Device(jLeaves.getString(i)));
         }
      } catch (Exception e) {
         Log.w("iFixit", "Error parsing device leaves: " + e.getMessage());
      }

      return devices;
   }
}
