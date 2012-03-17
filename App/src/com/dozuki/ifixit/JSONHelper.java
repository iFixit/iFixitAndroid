package com.dozuki.ifixit;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONHelper {
   private static final String LEAF_INDICATOR = "TOPICS";

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
         guide.setTopic(jGuideInfo.getString("topic"));
         guide.setSubject(jGuide.getString("subject"));
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
      } catch (JSONException e) {
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
      JSONObject jMedia = jStep.getJSONObject("media");
      JSONArray jImages = jMedia.getJSONArray("image");
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
    * Topic hierarchy parsing
    */
   public static TopicNode parseTopics(String json) {
      try {
         JSONObject jTopics = new JSONObject(json);
         ArrayList<TopicNode> topics = parseTopicChildren(jTopics);
         TopicNode root = null;

         if (topics != null) {
            root = new TopicNode();
            root.addAllTopics(topics);
         } else {
            Log.e("iFixit", "Topics is null (response: " + json + ")");
         }

         return root;
      }
      catch (Exception e) {
         Log.w("iFixit", "Error parsing topics: " + e.getMessage());
         return null;
      }
   }

   /**
    * Reads through the given JSONObject and adds any topics to the given
    * topic
    */
   public static ArrayList<TopicNode> parseTopicChildren(JSONObject jTopic) {
      Iterator<String> iterator = jTopic.keys();
      String topicName;
      ArrayList<TopicNode> topics = new ArrayList<TopicNode>();
      TopicNode currentTopic;

      try {
         while (iterator.hasNext()) {
            topicName = iterator.next();

            if (topicName.equals(LEAF_INDICATOR)) {
               topics.addAll(parseTopicLeaves(
                jTopic.getJSONArray(LEAF_INDICATOR)));
            }
            else {
               currentTopic = new TopicNode(topicName);
               currentTopic.addAllTopics(parseTopicChildren(
                jTopic.getJSONObject(topicName)));
               topics.add(currentTopic);
            }
         }
      } catch (Exception e) {
         Log.w("iFixit", "Error parsing topic children: " + e.getMessage());
      }

      return topics;
   }

   public static ArrayList<TopicNode> parseTopicLeaves(JSONArray jLeaves) {
      ArrayList<TopicNode> topics = new ArrayList<TopicNode>();

      try {
         for (int i = 0; i < jLeaves.length(); i++) {
            topics.add(new TopicNode(jLeaves.getString(i)));
         }
      } catch (Exception e) {
         Log.w("iFixit", "Error parsing topic leaves: " + e.getMessage());
      }

      return topics;
   }

   /**
    * Topic leaf parsing
    */
   public static TopicLeaf parseTopicLeaf(String json) {
      try {
         JSONObject jTopic = new JSONObject(json);
         JSONArray jGuides = jTopic.getJSONArray("guides");
         JSONObject jSolutions = jTopic.getJSONObject("solutions");
         JSONObject jInfo = jTopic.getJSONObject("topic_info");
         TopicLeaf topicLeaf = new TopicLeaf(jInfo.getString("name"));

         for (int i = 0; i < jGuides.length(); i++) {
            topicLeaf.addGuide(parseGuideInfo(jGuides.getJSONObject(i)));
         }

         topicLeaf.setNumSolutions(Integer.parseInt(
          jSolutions.getString("count")));
         topicLeaf.setSolutionsUrl(jSolutions.getString("url"));

         return topicLeaf;
      } catch (JSONException e) {
         Log.e("iFixit", "Error parsing topic leaf: " + e);
         return null;
      }
   }

   public static GuideInfo parseGuideInfo(JSONObject jGuide) {
      try {
         GuideInfo guideInfo = new GuideInfo(jGuide.getInt("guideid"));

         guideInfo.setSubject(jGuide.getString("subject"));
         guideInfo.setImage(jGuide.getString("image_url"));
         guideInfo.setTitle(jGuide.getString("title"));
         guideInfo.setType(jGuide.getString("type"));
         guideInfo.setUrl(jGuide.getString("url"));

         return guideInfo;
      } catch (JSONException e) {
         Log.e("iFixit", "Error parsing guide info: " + e);
         return null;
      }
   }
}
