package com.dozuki.ifixit.util;

import android.util.Log;
import com.dozuki.ifixit.model.*;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.gallery.GalleryEmbedList;
import com.dozuki.ifixit.model.gallery.GalleryVideoList;
import com.dozuki.ifixit.model.guide.*;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.model.topic.TopicNode;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.model.user.UserImage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.lang.reflect.Type;
import java.util.*;

public class JSONHelper {
   private static final String TAG = "JSONHelper";

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
         Log.e(TAG, "Error parsing sites: " + e);
      }

      return sites;
   }

   public static Site parseSiteInfo(String json) {
      Site site = null;

      try {
         JSONObject siteInfoObject = new JSONObject(json);
         site = parseSite(siteInfoObject);

         JSONObject types = (JSONObject) siteInfoObject.get("guide-types");
         site.mGuideTypes = new ArrayList<GuideType>();

         Iterator<?> keys = types.keys();
         while (keys.hasNext()) {
            String key = (String) keys.next();
            if (types.get(key) instanceof JSONObject) {
               site.mGuideTypes.add(parseGuideType(key, (JSONObject) types.get(key)));
            }
         }
      } catch (JSONException e) {
         Log.e(TAG, "Error parsing site info", e);
      }

      return site;
   }

   private static Site parseSite(JSONObject jSite) throws JSONException {
      Site site = new Site(jSite.getInt("siteid"));

      site.mName = jSite.getString("name");
      site.mDomain = jSite.getString("domain");
      site.mCustomDomain = jSite.has("custom_domain") ? jSite.getString("custom_domain") : "";
      site.mTitle = jSite.getString("title");
      site.mTheme = jSite.getString("theme");
      site.mPublic = !jSite.getBoolean("private");
      site.mDescription = jSite.getString("description");
      site.mAnswers = jSite.getBoolean("answers");
      site.mStoreUrl = jSite.has("store") ? jSite.getString("store") : "";

      setAuthentication(site, jSite.getJSONObject("authentication"));

      return site;
   }

   private static void setAuthentication(Site site, JSONObject jAuth) throws JSONException {
      site.mStandardAuth = jAuth.has("standard") && jAuth.getBoolean("standard");

      site.mSsoUrl = jAuth.has("sso") ? jAuth.getString("sso") : null;

      site.mPublicRegistration = jAuth.getBoolean("public-registration");
   }

   public static ArrayList<String> parseAllTopics(String json) {
      ArrayList<String> topics = new ArrayList<String>();

      try {
         JSONArray topicsJson = new JSONArray(json);

         for (int i = 0; i < topicsJson.length(); i++)
            topics.add(topicsJson.getString(i));

      } catch (JSONException e) {
         Log.e(TAG, "Error parsing all topics list: ", e);
      }

      return topics;
   }

   private static GuideType parseGuideType(String type, JSONObject object) {
      try {
         return new GuideType(type, object.getString("title"), object.getString("prompt"));
      } catch (JSONException e) {
         return new GuideType(type, "", "");
      }
   }

   /**
    * Guide parsing
    */
   public static Guide parseGuide(String json) throws JSONException {
      JSONObject jGuide = new JSONObject(json);
      JSONArray jSteps = jGuide.getJSONArray("steps");
      JSONArray jTools = jGuide.getJSONArray("tools");
      JSONArray jParts = jGuide.getJSONArray("parts");
      JSONObject jAuthor = jGuide.getJSONObject("author");
      Guide guide = new Guide(jGuide.getInt("guideid"));

      guide.setTitle(jGuide.getString("title"));
      guide.setTopic(jGuide.getString("category"));
      guide.setSubject(jGuide.getString("subject"));
      guide.setAuthor(jAuthor.getString("username"));
      guide.setTimeRequired(jGuide.getString("time_required"));
      guide.setDifficulty(jGuide.getString("difficulty"));
      guide.setIntroductionRaw(jGuide.getString("introduction_raw"));
      guide.setIntroductionRendered(jGuide.getString("introduction_rendered"));
      guide.setIntroImage(parseImage(jGuide, "image"));
      guide.setSummary(jGuide.isNull("summary") ? "" : jGuide.getString("summary"));
      guide.setRevisionid(jGuide.getInt("revisionid"));
      guide.setPublic(jGuide.getBoolean("public"));
      guide.setType(jGuide.getString("type"));
      guide.setPatrolThreshold(jGuide.getInt("patrol_threshold"));

      if (jGuide.has("can_edit")) {
         guide.setCanEdit(jGuide.getBoolean("can_edit"));
      }

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

   private static Item parsePart(JSONObject jPart) throws JSONException {
      return new Item(
       Item.ItemType.PART,
       jPart.getString("text"),
       jPart.getInt("quantity"),
       jPart.getString("url"),
       jPart.getString("thumbnail"),
       jPart.getString("notes"));
   }

   private static Item parseTool(JSONObject jTool) throws JSONException {
      return new Item(
       Item.ItemType.TOOL,
       jTool.getString("text"),
       jTool.getInt("quantity"),
       jTool.getString("url"),
       jTool.getString("thumbnail"),
       jTool.getString("notes"));
   }

   public static GuideStep parseStep(JSONObject jStep, int stepNumber) throws JSONException {
      GuideStep step = new GuideStep(stepNumber);

      step.setGuideid(jStep.getInt("guideid"));
      step.setStepid(jStep.getInt("stepid"));
      step.setRevisionid(jStep.getInt("revisionid"));
      step.setOrderby(jStep.isNull("orderby") ? stepNumber : jStep.getInt("orderby"));
      step.setTitle(jStep.getString("title"));

      try {
         JSONObject jMedia = jStep.getJSONObject("media");
         String type = jMedia.getString("type");

         if (type.equals("image")) {
            JSONArray jImages = jMedia.getJSONArray("data");
            for (int i = 0; i < jImages.length(); i++) {
               step.addImage(parseImage(jImages.getJSONObject(i), null));
            }
         } else if (type.equals("video")) {
            JSONObject jVideo = jMedia.getJSONObject("data");
            step.addVideo(parseVideo(jVideo));
         } else if (type.equals("embed")) {
            JSONObject jEmbed = jMedia.getJSONObject("data");
            step.addEmbed(parseEmbed(jEmbed));
         }

      } catch (JSONException e) {
         Image image = new Image();
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

   private static Video parseVideo(JSONObject jVideo) throws JSONException {
      Video video = new Video();

      try {
         JSONArray jEncodings = jVideo.getJSONArray("encodings");

         int numEncodings = jEncodings.length();
         for (int i = 0; i < numEncodings; i++) {
            video.addEncoding(parseVideoEncoding(jEncodings.getJSONObject(i)));
         }

         video.setHeight(jVideo.getInt("width"));
         video.setWidth(jVideo.getInt("height"));
         video.setDuration(jVideo.getInt("duration"));
         video.setFilename(jVideo.getString("filename"));
         video.setThumbnail(parseVideoThumbnail(jVideo.getJSONObject("image")));

      } catch (JSONException e) {
         e.printStackTrace();
         Log.e("JSONHelper parseVideo", "Error parsing video API response");
      }

      return video;
   }

   private static VideoThumbnail parseVideoThumbnail(JSONObject jVideoThumb) throws JSONException {

      Image image = parseImage(jVideoThumb.getJSONObject("image"), null);

      String ratio = jVideoThumb.getString("ratio");
      int width = jVideoThumb.getInt("width");
      int height = jVideoThumb.getInt("height");

      return new VideoThumbnail(image.getId(), image.getPath(), ratio, width, height);
   }

   private static VideoEncoding parseVideoEncoding(JSONObject jVideoEncoding) throws JSONException {
      return new VideoEncoding(jVideoEncoding.getInt("width"), jVideoEncoding.getInt("height"),
        jVideoEncoding.getString("url"), jVideoEncoding.getString("format"));
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
      int lineid = jLine.isNull("lineid") ? 0 : jLine.getInt("lineid");

      return new StepLine(lineid, jLine.getString("bullet"),
       jLine.getInt("level"), jLine.getString("text_raw"), jLine.getString("text_rendered"));
   }

   /**
    * Topic hierarchy parsing
    */
   public static TopicNode parseTopics(String json) throws JSONException {
      JSONObject jTopics = new JSONObject(json);
      ArrayList<TopicNode> topics = parseTopicChildren(jTopics);
      TopicNode root = new TopicNode();

      root.setChildren(topics);

      return root;
   }

   /**
    * Reads through the given JSONObject and adds any topics to the given topic.
    */
   private static ArrayList<TopicNode> parseTopicChildren(JSONObject jTopic)
    throws JSONException {
      // Don't allocate any lists if it's empty.
      if (jTopic.length() == 0) {
         return null;
      }

      @SuppressWarnings("unchecked")
      Iterator<String> iterator = jTopic.keys();
      String topicName;
      ArrayList<TopicNode> topics = new ArrayList<TopicNode>();
      TopicNode currentTopic;

      while (iterator.hasNext()) {
         topicName = iterator.next();

         currentTopic = new TopicNode(topicName);
         currentTopic.setChildren(parseTopicChildren(jTopic.getJSONObject(topicName)));

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
         String guideJson = jGuides.getJSONObject(i).toString();
         GuideInfo guide = new Gson().fromJson(guideJson, GuideInfo.class);
         topicLeaf.addGuide(guide);
      }

      topicLeaf.setNumSolutions(Integer.parseInt(jSolutions.getString("count")));
      topicLeaf.setSolutionsUrl(jSolutions.getString("url"));
      topicLeaf.setDescription(jTopic.getString("description"));
      topicLeaf.setImage(parseImage(jTopic.getJSONObject("image"), null));
      topicLeaf.setLocale(jTopic.getString("locale"));
      topicLeaf.setContentsRaw(jTopic.getString("contents_raw"));
      topicLeaf.setContentsRendered(jTopic.getString("contents_rendered"));
      topicLeaf.setTitle(jTopic.getString("display_title"));

      return topicLeaf;
   }

   /**
    * Parsing list of UserImageInfo.
    */
   public static ArrayList<UserImage> parseUserImages(String json) throws JSONException {
      JSONArray jImages = new JSONArray(json);

      ArrayList<UserImage> userImageList = new ArrayList<UserImage>();

      for (int i = 0; i < jImages.length(); i++) {
         userImageList.add(parseUserImage(jImages.getJSONObject(i)));
      }

      return userImageList;
   }

   private static UserImage parseUserImage(JSONObject jImage) throws JSONException {
      JSONObject img = jImage.getJSONObject("image");

      int id = img.getInt("id");
      int width = jImage.getInt("width");
      int height = jImage.getInt("height");
      String path = img.getString("original");
      String ratio = jImage.getString("ratio");
      String markup = jImage.isNull("markup") ? "" : jImage.getString("markup");
      // TODO: add exif
      String exif = "";

      return new UserImage(id, path, width, height, ratio, markup, exif);
   }

   public static GalleryVideoList parseUserVideos(String jVideo) throws JSONException {
      JSONArray jImages = new JSONArray(jVideo);

      GalleryVideoList userVideoList = new GalleryVideoList();

      for (int i = 0; i < jImages.length(); i++) {
         //userVideoList.addItem((parseUserVideoInfo(jImages.getJSONObject(i))));
      }
      return userVideoList;
   }

   public static Video parseUserVideoInfo(JSONObject jVideo) throws JSONException {
      /*
      UserVideoInfo userVideoInfo = new UserVideoInfo();
      userVideoInfo.setItemId(jVideo.getString("imageid"));
      userVideoInfo.setGuid(jVideo.getString("guid"));
      userVideoInfo.setHeight(jVideo.getString("height"));
      userVideoInfo.setWidth(jVideo.getString("width"));
      userVideoInfo.setRatio(jVideo.getString("ratio"));

      return userVideoInfo; */
      return new Video();
   }

   public static GalleryEmbedList parseUserEmbeds(String jEmbed) throws JSONException {
      JSONArray jEmbeds = new JSONArray(jEmbed);

      GalleryEmbedList userEmbedList = new GalleryEmbedList();

      for (int i = 0; i < jEmbed.length(); i++) {
         //userEmbedList.addItem((parseUserVideoInfo(jEmbeds.getJSONObject(i))));
      }
      return userEmbedList;
   }
/*
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
*/

   public static Image parseUploadedImage(String image) throws JSONException {
      return parseImage(new JSONObject(image), "image");
   }

   /**
    * Login parsing info
    */
   public static User parseLoginInfo(String json) throws JSONException {
      JSONObject jUser = new JSONObject(json);

      User user = new User();
      user.setUserid(jUser.getInt("userid"));
      user.setUsername(jUser.getString("username"));

      if (!jUser.isNull("image"))
         user.setAvatar(parseImage(jUser.getJSONObject("image"), null));

      user.setAboutRaw(jUser.getString("about_raw"));
      user.setAboutRendered(jUser.getString("about_rendered"));

      if (!jUser.isNull("summary"))
         user.setSummary(jUser.getString("summary"));
      if (!jUser.isNull("location"))
         user.setLocation(jUser.getString("location"));
      if (!jUser.isNull("join_date"))
         user.setJoinDate(jUser.getInt("join_date"));

      user.setBadges(parseBadges(jUser.getJSONObject("badge_counts")));
      user.setReputation(jUser.getInt("reputation"));
      user.setCertificationCount(jUser.getInt("certification_count"));
      user.setAuthToken(jUser.getString("authToken"));

      return user;
   }

   public static Badges parseBadges(JSONObject json) throws JSONException {

      int gold = json.getInt("gold");
      int silver = json.getInt("silver");
      int bronze = json.getInt("bronze");

      return new Badges(bronze, silver, gold);
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

         if (jError.has("message")) {
            error = jError.getString("message");
         }
      } catch (JSONException e) {
         Log.e("JSONHelper", "Unable to parse error message");
      }

      return error;
   }

   public static ArrayList<GuideInfo> parseUserFavorites(String json) {
      ArrayList<GuideInfo> result = new ArrayList<GuideInfo>();

      try {
         JSONArray jFavorites = new JSONArray(json);

         int length = jFavorites.length();
         for (int i = 0; i < length; i++) {
            JSONObject jGuide = jFavorites.getJSONObject(i).getJSONObject("guide");

            GuideInfo guide = new Gson().fromJson(jGuide.toString(), GuideInfo.class);
            result.add(guide);
         }
      } catch (JSONException e) {
         e.printStackTrace();
      }

      return result;
   }

   public static ArrayList<GuideInfo> parseUserGuides(String json) {
      return parseGuides(json);
   }

   public static ArrayList<GuideInfo> parseGuides(String json) {
      Type guidesType = new TypeToken<Collection<GuideInfo>>() {}.getType();
      Collection<GuideInfo> guideList = new Gson().fromJson(json, guidesType);

      return new ArrayList<GuideInfo>(guideList);
   }

   /*
   public static UserGuide parseUserGuideInfo(JSONObject json) throws JSONException {

      GuideInfo guideInfo = new GuideInfo(json.getInt("guideid"));

      guideInfo.mTopic = json.getString("category");
      guideInfo.mTitle = json.getString("title");
      guideInfo.setSubject(json.getString("subject"));
      guideInfo.setType(json.getString("type"));
      guideInfo.setPublished(json.getBoolean("public"));
      guideInfo.setUserName(json.getString("username"));
      guideInfo.setUserid(json.getInt("userid"));
      guideInfo.setRevisionid(json.getInt("revisionid"));

      guideInfo.setImage(parseImage(json, "image"));

      return guideInfo;
   }
*/
   public static JSONArray createLineArray(ArrayList<StepLine> lines) throws JSONException {

      JSONArray array = new JSONArray();

      for (StepLine l : lines) {
         JSONObject lineObject = createLineObject(l);
         array.put(lineObject);
      }
      return array;
   }

   public static JSONArray createImageArray(ArrayList<Image> lines) throws JSONException {

      JSONArray array = new JSONArray();

      for (Image l : lines) {
         array.put(l.getId());
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
      lineObject.put("lineid", (l.getLineId() != null ? l.getLineId() : 0));

      return lineObject;
   }

   public static JSONArray createStepIdArray(ArrayList<GuideStep> steps) throws JSONException {
      JSONArray jSteps = new JSONArray();
      for (GuideStep step : steps) {
         jSteps.put(jSteps.length(), step.getStepid());
      }

      return jSteps;
   }

   public static Image parseImage(JSONObject image, String imageFieldName) {
      try {
         if (imageFieldName != null) {
            image = image.optJSONObject(imageFieldName);
         }

         if (image == null) {
            return new Image();
         }

         return new Image(image.getInt("id"), image.getString("original"));
      } catch (JSONException e) {
         Log.w(TAG, "Image parsing", e);
         return new Image();
      }
   }
}
