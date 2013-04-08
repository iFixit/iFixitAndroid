package com.dozuki.ifixit.util;

import android.text.Spannable;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Log;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.gallery.*;
import com.dozuki.ifixit.model.guide.*;
import com.dozuki.ifixit.model.login.User;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.model.topic.TopicNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JSONHelper {
   /**
    * Used to indicate an integer JSON field that is null.
    */
   public static final int NULL_INT = -1;

   public static ArrayList<Site> parseSites(String json) {
      ArrayList<Site> sites = new ArrayList<Site>();

      try {
         JSONArray jSites = new JSONArray(json);
         Site site;

         for (int i = 0; i < jSites.length(); i++) {
            site = parseSite(jSites.getJSONObject(i));

            if (site != null) {
               sites.add(site);
            }
         }
      } catch (JSONException e) {
         Log.e("iFixit", "Error parsing sites: " + e);
      }

      return sites;
   }

   private static Site parseSite(JSONObject jSite) throws JSONException {
      Site site = new Site(jSite.getInt("siteid"));

      site.mName = jSite.getString("name");
      site.mDomain = jSite.getString("domain");
      site.mTitle = jSite.getString("title");
      site.mTheme = jSite.getString("theme");
      site.mPublic = !jSite.getBoolean("private");
      site.mDescription = jSite.getString("description");
      site.mAnswers = jSite.getInt("answers") != 0;

      setAuthentication(site, jSite.getJSONObject("authentication"));

      return site;
   }

   private static void setAuthentication(Site site, JSONObject jAuth) throws JSONException {
      site.mStandardAuth = jAuth.has("standard") && jAuth.getBoolean("standard");

      site.mSsoUrl = jAuth.has("sso") ? jAuth.getString("sso") : null;

      site.mPublicRegistration = jAuth.getBoolean("public-registration");
   }

   /**
    * Guide parsing
    */
   public static Guide parseGuide(String json) throws JSONException {
      JSONObject jGuideInfo = new JSONObject(json);
      JSONObject jGuide = jGuideInfo.getJSONObject("guide");
      JSONArray jSteps = jGuide.getJSONArray("steps");
      JSONArray jTools = jGuide.getJSONArray("tools");
      JSONArray jParts = jGuide.getJSONArray("parts");
      JSONObject jAuthor = jGuide.getJSONObject("author");
      Guide guide = new Guide(jGuideInfo.getInt("guideid"));

      guide.setTitle(jGuide.getString("title"));
      guide.setTopic(jGuideInfo.getString("topic"));
      guide.setSubject(jGuide.getString("subject"));
      guide.setAuthor(jAuthor.getString("text"));
      guide.setTimeRequired(jGuide.getString("time_required"));
      guide.setDifficulty(jGuide.getString("difficulty"));
      guide.setIntroductionRaw(jGuide.getString("introduction_raw"));
      guide.setIntroductionRendered(jGuide.getString("introduction_rendered"));
      guide.setIntroImage(parseImage(jGuide, "image"));
      guide.setSummary(jGuide.getString("summary"));
      guide.setRevisionid(jGuide.getInt("revisionid"));

      for (int i = 0; i < jSteps.length(); i++) {
         guide.addStep(parseStep(jSteps.getJSONObject(i), i + 1));
      }

      for (int i = 0; i < jTools.length(); i++) {
         guide.addTool(parseTool(jTools.getJSONObject(i)));
      }

      for (int i = 0; i < jParts.length(); i++) {
         guide.addPart(parsePart(jParts.getJSONObject(i)));
      }

      return guide;
   }

   private static GuidePart parsePart(JSONObject jPart) throws JSONException {
      return new GuidePart(jPart.getString("text"), jPart.getString("url"),
       jPart.getString("thumbnail"), jPart.getString("notes"));
   }

   private static GuideTool parseTool(JSONObject jTool) throws JSONException {
      return new GuideTool(jTool.getString("text"), jTool.getString("url"),
       jTool.getString("thumbnail"), jTool.getString("notes"));
   }

   public static GuideStep parseStep(JSONObject jStep, int stepNumber) throws JSONException {
      GuideStep step = new GuideStep(stepNumber);

      step.setGuideid(jStep.getInt("guideid"));
      step.setStepid(jStep.getInt("stepid"));
      step.setRevisionid(jStep.getInt("revisionid"));
      step.setOrderby(jStep.getInt("orderby"));
      step.setTitle(jStep.getString("title"));

      try {
         JSONObject jMedia = jStep.getJSONObject("media");
         String type = jMedia.getString("type");

         if (type.compareTo("image") == 0) {
            JSONArray jImages = jMedia.getJSONArray("data");
            for (int i = 0; i < jImages.length(); i++) {
               step.addImage(parseImage(jImages.getJSONObject(i), null));
            }
         }

         if (type.compareTo("video") == 0) {
            JSONObject jVideo = jMedia.getJSONObject("data");
            step.addVideo(parseVideo(jVideo));
         }

         if (type.compareTo("embed") == 0) {
            JSONObject jEmbed = jMedia.getJSONObject("data");
            step.addEmbed(parseEmbed(jEmbed));
         }

      } catch (JSONException e) {
         APIImage image = new APIImage();
         step.addImage(image);
      }

      JSONArray jLines = jStep.getJSONArray("lines");
      for (int i = 0; i < jLines.length(); i++) {
         step.addLine(parseLine(jLines.getJSONObject(i)));
      }

      return step;
   }

   public static Map<String, String> getQueryMap(String url) {
      String query = url.substring(url.indexOf('?') + 1);
      String[] params = query.split("&");
      Map<String, String> map = new HashMap<String, String>();
      for (String param : params) {
         String name = param.split("=")[0];
         String value = param.split("=")[1];
         map.put(name, value);
      }

      return map;
   }

   private static StepVideo parseVideo(JSONObject jVideo) throws JSONException {
      StepVideo video = new StepVideo();

      try {
         JSONArray jEncodings = jVideo.getJSONArray("encoding");
         for (int i = 0; i < jEncodings.length(); i++) {
            video.addEncoding(parseVideoEncoding(jEncodings.getJSONObject(i)));
         }
      } catch (JSONException e) {
         e.printStackTrace();
         Log.e("JSONHelper parseVideo", "Error parsing video API response");
      }

      video.setThumbnail(parseVideoThumbnail(jVideo.getJSONObject("thumbnail")));
      return video;
   }

   private static StepVideoThumbnail parseVideoThumbnail(JSONObject jVideoThumb) throws JSONException {
      String guid = jVideoThumb.getString("guid");
      int imageid = jVideoThumb.getInt("imageid");
      String ratio = jVideoThumb.getString("ratio");
      int width = jVideoThumb.getInt("width");
      int height = jVideoThumb.getInt("height");

      String url = jVideoThumb.getString("medium");
      url = url.substring(0, url.lastIndexOf("."));

      return new StepVideoThumbnail(guid, imageid, url, ratio, width, height);
   }

   private static Embed parseEmbed(JSONObject jEmbed) throws JSONException {
      Embed em = new Embed(jEmbed.getInt("width"), jEmbed.getInt("height"),
       jEmbed.getString("type"), jEmbed.getString("url"));
      em.setContentURL(getQueryMap(jEmbed.getString("url")).get("url"));
      return em;
   }

   public static OEmbed parseOEmbed(String embed) throws JSONException {

      JSONObject jOEmbed = new JSONObject(embed);
      String thumbnail = null;
      if (jOEmbed.has("thumbnail_url")) {
         thumbnail = jOEmbed.getString("thumbnail_url");
      }
      Document doc = Jsoup.parse(jOEmbed.getString("html"));
      return new OEmbed(jOEmbed.getString("html"),
       doc.getElementsByAttribute("src").get(0).attr("src"), thumbnail);
   }

   private static StepLine parseLine(JSONObject jLine) throws JSONException {

      return new StepLine(jLine.getInt("lineid"), jLine.getString("bullet"),
       jLine.getInt("level"), jLine.getString("text_raw"), jLine.getString("text_rendered"));
   }

   /**
    * Topic hierarchy parsing
    */
   public static TopicNode parseTopics(String json) throws JSONException {
      JSONObject jTopics = new JSONObject(json);
      ArrayList<TopicNode> topics = parseTopicChildren(jTopics);
      TopicNode root = new TopicNode();

      root.addAllTopics(topics);

      return root;
   }

   /**
    * Reads through the given JSONObject and adds any topics to the given topic.
    */
   private static ArrayList<TopicNode> parseTopicChildren(JSONObject jTopic)
    throws JSONException {
      @SuppressWarnings("unchecked")
      Iterator<String> iterator = jTopic.keys();
      String topicName;
      ArrayList<TopicNode> topics = new ArrayList<TopicNode>();
      TopicNode currentTopic;

      while (iterator.hasNext()) {
         topicName = iterator.next();

         currentTopic = new TopicNode(topicName);
         currentTopic.addAllTopics(parseTopicChildren(
          jTopic.getJSONObject(topicName)));
         topics.add(currentTopic);
      }

      return topics;
   }

   /**
    * Topic leaf parsing
    */
   public static TopicLeaf parseTopicLeaf(String json) throws JSONException {
      JSONObject jTopic = new JSONObject(json);
      JSONArray jGuides = jTopic.getJSONArray("guides");
      JSONObject jSolutions = jTopic.getJSONObject("solutions");
      JSONObject jInfo = jTopic.getJSONObject("topic_info");
      TopicLeaf topicLeaf = new TopicLeaf(jInfo.getString("name"));

      for (int i = 0; i < jGuides.length(); i++) {
         topicLeaf.addGuide(parseGuideInfo(jGuides.getJSONObject(i)));
      }

      topicLeaf.setNumSolutions(Integer.parseInt(jSolutions.getString("count")));
      topicLeaf.setSolutionsUrl(jSolutions.getString("url"));

      return topicLeaf;
   }

   private static GuideInfo parseGuideInfo(JSONObject jGuide) {
      try {
         GuideInfo guideInfo = new GuideInfo(jGuide.getInt("guideid"));

         guideInfo.mRevisionid = jGuide.optInt("revisionid", NULL_INT);
         guideInfo.mModifiedDate = jGuide.optInt("modified_date", NULL_INT);
         guideInfo.mPrereqModifiedDate = jGuide.optInt("prereq_modified_date", NULL_INT);
         guideInfo.mTopic = jGuide.getString("topic");
         guideInfo.mSubject = jGuide.getString("subject");
         guideInfo.mType = jGuide.getString("type");
         guideInfo.mTitle = jGuide.getString("title");
         guideInfo.mPublic = jGuide.getBoolean("public");
         guideInfo.mImage = parseImage(jGuide, "image");

         return guideInfo;
      } catch (JSONException e) {
         Log.e("iFixit", "Error parsing guide info: " + e);
         return null;
      }
   }

   /**
    * Parsing list of UserImageInfo.
    */
   public static UserImageList parseUserImages(String json) throws JSONException {
      JSONArray jImages = new JSONArray(json);

      UserImageList userImageList = new UserImageList();

      for (int i = 0; i < jImages.length(); i++) {
         userImageList.addItem((parseUserImageInfo(jImages.getJSONObject(i))));
      }

      return userImageList;
   }

   public static UserImageInfo parseUserImageInfo(JSONObject jImage)
    throws JSONException {
      UserImageInfo userImageInfo = new UserImageInfo();
      // TODO: Make a function to parse the image format and return an object
      // that UserImageInfo uses. All other image parsing should use that too.
      userImageInfo.setItemId(jImage.getJSONObject("image").getString("id"));
      userImageInfo.setGuid(jImage.getJSONObject("image").getString("medium"));
      userImageInfo.setWidth(jImage.getString("width"));
      userImageInfo.setHeight(jImage.getString("height"));
      userImageInfo.setRatio(jImage.getString("ratio"));

      return userImageInfo;
   }

   public static UserVideoList parseUserVideos(String jVideo) throws JSONException {
      JSONArray jImages = new JSONArray(jVideo);

      UserVideoList userVideoList = new UserVideoList();

      for (int i = 0; i < jImages.length(); i++) {
         userVideoList.addItem((parseUserVideoInfo(jImages.getJSONObject(i))));
      }
      return userVideoList;
   }

   public static UserVideoInfo parseUserVideoInfo(JSONObject jVideo)
    throws JSONException {
      UserVideoInfo userVideoInfo = new UserVideoInfo();
      userVideoInfo.setItemId(jVideo.getString("imageid"));
      userVideoInfo.setGuid(jVideo.getString("guid"));
      userVideoInfo.setHeight(jVideo.getString("height"));
      userVideoInfo.setWidth(jVideo.getString("width"));
      userVideoInfo.setRatio(jVideo.getString("ratio"));

      return userVideoInfo;
   }

   public static UserEmbedList parseUserEmbeds(String jEmbed) throws JSONException {
      JSONArray jEmbeds = new JSONArray(jEmbed);

      UserEmbedList userEmbedList = new UserEmbedList();

      for (int i = 0; i < jEmbed.length(); i++) {
         userEmbedList.addItem((parseUserVideoInfo(jEmbeds.getJSONObject(i))));
      }
      return userEmbedList;
   }

   public static UserEmbedInfo parseUserEmbedInfo(JSONObject jEmbed)
    throws JSONException {
      UserEmbedInfo userEmbedInfo = new UserEmbedInfo();
//        userEmbedInfo.setItemId(jEmbed.getString("imageid"));
//        userEmbedInfo.setGuid(jEmbed.getString("guid"));
//        userEmbedInfo.setHeight(jEmbed.getString("height"));
//        userEmbedInfo.setWidth(jEmbed.getString("width"));
//        userEmbedInfo.setRatio(jEmbed.getString("ratio"));

      return userEmbedInfo;
   }


   public static UploadedImageInfo parseUploadedImageInfo(String image)
    throws JSONException {
      JSONObject jImage = new JSONObject(image);

      UploadedImageInfo userImageInfo = new UploadedImageInfo();
      userImageInfo.setImageid(jImage.getJSONObject("image").getString("id"));
      userImageInfo.setGuid(jImage.getJSONObject("image").getString("original"));

      return userImageInfo;
   }

   /**
    * Login parsing info
    */
   public static User parseLoginInfo(String json) throws JSONException {
      JSONObject jUser = new JSONObject(json);

      User user = new User();
      user.setUserid(jUser.getString("userid"));
      user.setUsername(jUser.getString("username"));
      //    user.setImageid(jUser.getString("imageid"));
      user.setAuthToken(jUser.getString("authToken"));

      return user;
   }

   /**
    * Returns the error message contained in the given JSON, or null if one
    * does not exist.
    * <p/>
    * e.g. Returns "Guide not found" for:
    * "{"error":true,"msg":"Guide not found"}"
    */
   public static String parseError(String json) {
      String error = null;

      try {
         JSONObject jError = new JSONObject(json);
         if (jError.getBoolean("error")) {
            error = jError.getString("msg");
         }
      } catch (JSONException e) {
      }

      return error;
   }

   /**
    * Removes relative a hrefs
    *
    * @param spantext (from Html.fromhtml())
    * @return spanned with fixed links
    */
   public static Spanned correctLinkPaths(Spanned spantext) {
      Object[] spans = spantext.getSpans(0, spantext.length(), Object.class);
      for (Object span : spans) {
         int start = spantext.getSpanStart(span);
         int end = spantext.getSpanEnd(span);
         int flags = spantext.getSpanFlags(span);
         
         Site site = MainApplication.get().getSite();
         
         if (span instanceof URLSpan) {
            URLSpan urlSpan = (URLSpan) span;
            if (!urlSpan.getURL().startsWith("http")) {
               if (urlSpan.getURL().startsWith("/")) {
                  urlSpan = new URLSpan("http://" + site.mDomain +
                   urlSpan.getURL());
               } else {
                  urlSpan = new URLSpan("http://" + site.mDomain + "/" +
                   urlSpan.getURL());
               }
            }
            ((Spannable) spantext).removeSpan(span);
            ((Spannable) spantext).setSpan(urlSpan, start, end, flags);
         }
      }

      return spantext;
   }

   public static ArrayList<UserGuide> parseUserGuides(String json) throws JSONException {
      JSONArray jGuideInfos = new JSONArray(json);
      ArrayList<UserGuide> guideList = new ArrayList<UserGuide>();

      for (int i = 0; i < jGuideInfos.length(); i++) {
         guideList.add(parseUserGuideInfo(jGuideInfos.getJSONObject(i)));
      }

      return guideList;
   }

   public static UserGuide parseUserGuideInfo(JSONObject json) throws JSONException {

      UserGuide userGuide = new UserGuide();

      userGuide.setGuideid(json.getInt("guideid"));
      userGuide.setTopic(json.getString("topic"));
      userGuide.setTitle(json.getString("title"));
      userGuide.setSubject(json.getString("subject"));
      userGuide.setType(json.getString("type"));
      userGuide.setPublished(json.getBoolean("public"));
      userGuide.setUserName(json.getString("username"));
      userGuide.setUserid(json.getInt("userid"));
      userGuide.setRevisionid(json.getInt("revisionid"));

      userGuide.setImage(parseImage(json, "image"));

      return userGuide;
   }

   public static JSONArray createLineArray(ArrayList<StepLine> lines) throws JSONException {

      JSONArray array = new JSONArray();

      for (StepLine l : lines) {
         JSONObject lineObject = createLineObject(l);
         array.put(lineObject);
      }
      return array;
   }

   public static JSONArray createImageArray(ArrayList<APIImage> lines) throws JSONException {

      JSONArray array = new JSONArray();

      for (APIImage l : lines) {
         array.put(l.mId);
      }
      return array;
   }

   public static JSONObject createStepMediaJsonObject(GuideStep step) throws JSONException {
      JSONObject jMedia = new JSONObject();

      jMedia.put("type", "image");
      jMedia.put("data", createImageArray(step.getImages()));

      return jMedia;
   }

   public static JSONObject createLineObject(StepLine l) throws JSONException {

      JSONObject lineObject = new JSONObject();

      lineObject.put("text", l.getTextRaw());
      lineObject.put("bullet", l.getColor());
      lineObject.put("level", l.getLevel());
      if (l.getLineId() != null) {
         lineObject.put("lineid", l.getLineId());
      } else {
         lineObject.put("lineid", 0);
      }

      return lineObject;
   }

   public static JSONArray createStepIdArray(ArrayList<GuideStep> steps) throws JSONException {
      JSONArray jSteps = new JSONArray();
      for (GuideStep step : steps) {
         jSteps.put(jSteps.length(), step.getStepid());
      }

      return jSteps;
   }

   public static APIImage parseImage(JSONObject image, String imageFieldName) {
      try {
         if (imageFieldName != null) {
            image = image.optJSONObject(imageFieldName);
         }

         if (image == null) {
            return new APIImage();
         }

         APIImage apiImage = new APIImage(image.getInt("id"), image.getString("original"));

         return apiImage;
      } catch (JSONException e) {
         Log.w("iFixit", "APIImage parsing", e);
         return new APIImage();
      }
   }

   private static VideoEncoding parseVideoEncoding(JSONObject jVideoEncoding) throws JSONException {
      VideoEncoding encoding =
       new VideoEncoding(jVideoEncoding.getInt("width"), jVideoEncoding.getInt("height"),
        jVideoEncoding.getString("url"), jVideoEncoding.getString("format"));
      return encoding;
   }
}
