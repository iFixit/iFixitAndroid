package com.dozuki.ifixit.util.api;

import android.os.Bundle;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.guide.wizard.EditTextPage;
import com.dozuki.ifixit.model.guide.wizard.GuideTitlePage;
import com.dozuki.ifixit.model.guide.wizard.Page;
import com.dozuki.ifixit.model.guide.wizard.TopicNamePage;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.ui.guide.create.GuideIntroWizardModel;
import com.dozuki.ifixit.util.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Defines an ApiCall that can be performed using Api.call().
 */
public class ApiCall {
   private static final String NO_QUERY = "";

   protected ApiEndpoint mEndpoint;
   protected String mQuery;
   protected String mRequestBody;
   protected String mExtraInfo;
   protected String mFilePath;
   protected String mAuthToken;
   protected Site mSite;
   protected User mUser;
   protected int mActivityid = -1;

   public ApiCall(ApiEndpoint endpoint, String query) {
      this(endpoint, query, null);
   }

   public ApiCall(ApiEndpoint endpoint, String query, String requestBody) {
      this(endpoint, query, requestBody, null);
   }

   public ApiCall(ApiEndpoint endpoint, String query, String requestBody,
    String extraInfo) {
      this(endpoint, query, requestBody, extraInfo, null);
   }

   public ApiCall(ApiEndpoint endpoint, String query, String requestBody,
    String extraInfo, String filePath) {
      mEndpoint = endpoint;
      mQuery = query;
      mRequestBody = requestBody;
      mExtraInfo = extraInfo;
      mFilePath = filePath;
   }

   public void updateUser(User user) {
      mUser = user;

      if (mUser != null) {
         mAuthToken = mUser.getAuthToken();
      } else {
         mAuthToken = null;
      }
   }

   /**
    * ApiCall Factory methods.
    */

   public static ApiCall search(String query) {
      return new ApiCall(ApiEndpoint.SEARCH, query);
   }

   public static ApiCall teardowns(int limit, int offset) {
      return new ApiCall(ApiEndpoint.GUIDES,
       "?filter=teardown&order=DESC&limit=" + limit + "&offset=" + offset);
   }

   public static ApiCall featuredGuides(int limit, int offset) {
      return new ApiCall(ApiEndpoint.GUIDES,
       "/featured?limit=" + limit + "&offset=" + offset);
   }

   public static ApiCall categories() {
      return new ApiCall(ApiEndpoint.CATEGORIES, NO_QUERY);
   }

   public static ApiCall guide(int guideid) {
      return new ApiCall(ApiEndpoint.GUIDE, "" + guideid);
   }

   public static ApiCall userInfo(String authToken) {
      ApiCall apiCall = new ApiCall(ApiEndpoint.USER_INFO, NO_QUERY);

      apiCall.mAuthToken = authToken;

      return apiCall;
   }

   public static ApiCall topic(String topicName) {
      return new ApiCall(ApiEndpoint.TOPIC, topicName);
   }

   public static ApiCall register(String email, String password, String username) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("email", email);
         requestBody.put("password", password);
         requestBody.put("username", username);
      } catch (JSONException e) {
         return null;
      }

      return new ApiCall(ApiEndpoint.REGISTER, NO_QUERY, requestBody.toString());
   }

   public static ApiCall login(String email, String password) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("email", email);
         requestBody.put("password", password);
      } catch (JSONException e) {
         return null;
      }

      return new ApiCall(ApiEndpoint.LOGIN, NO_QUERY, requestBody.toString());
   }

   public static ApiCall logout(User user) {
      ApiCall apiCall = new ApiCall(ApiEndpoint.LOGOUT, NO_QUERY);

      // Override the authToken because the user won't be logged in by the time
      // the request is performed.
      apiCall.mAuthToken = user.getAuthToken();

      return apiCall;
   }

   public static ApiCall userFavorites(int limit, int offset) {
      return new ApiCall(ApiEndpoint.USER_FAVORITES, "?limit=" + limit + "&offset=" + offset);
   }

   public static ApiCall createGuideFromBundle(Bundle introWizardModel) {
      JSONObject requestBody = guideBundleToRequestBody(introWizardModel);

      try {
         requestBody.put("public", false);
      } catch (JSONException e) {
         return null;
      }

      return new ApiCall(ApiEndpoint.CREATE_GUIDE, NO_QUERY, requestBody.toString());
   }

   public static ApiCall createGuide(Guide guide) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("type", guide.getType());
         requestBody.put("category", guide.getTopic());
         requestBody.put("title", guide.getTitle());

         if (guide.getSubject() != null) {
            requestBody.put("subject", guide.getSubject());
         }
      } catch (JSONException e) {
         return null;
      }

      return new ApiCall(ApiEndpoint.CREATE_GUIDE, NO_QUERY, requestBody.toString());
   }

   public static ApiCall deleteGuide(GuideInfo guide) {
      return new ApiCall(ApiEndpoint.DELETE_GUIDE, guide.mGuideid + "?revisionid=" + guide.mRevisionid, "");
   }

   public static ApiCall editGuide(Bundle bundle, int guideid, int revisionid) {
      JSONObject requestBody = guideBundleToRequestBody(bundle);

      return new ApiCall(ApiEndpoint.EDIT_GUIDE, "" + guideid + "?revisionid="
       + revisionid, requestBody.toString());
   }

   public static ApiCall favoriteGuide(int guideid, boolean favorite) {
      ApiEndpoint endpoint = favorite ? ApiEndpoint.FAVORITE_GUIDE : ApiEndpoint.UNFAVORITE_GUIDE;
      return new ApiCall(endpoint, "" + guideid);
   }

   private static JSONObject guideBundleToRequestBody(Bundle bundle) {
      JSONObject requestBody = new JSONObject();
      App app = App.get();
      try {
         requestBody.put("type", bundle.getBundle(app.getString(R.string
          .guide_intro_wizard_guide_type_title)).getString(Page.SIMPLE_DATA_KEY).toLowerCase());
         requestBody.put("category", bundle.getBundle(app.getString(R.string
          .guide_intro_wizard_guide_topic_title, app.getTopicName())).getString(TopicNamePage.TOPIC_DATA_KEY));
         requestBody.put("title", bundle.getBundle(app.getString(R.string
          .guide_intro_wizard_guide_title_title)).getString(GuideTitlePage.TITLE_DATA_KEY));

         String subjectKey = GuideIntroWizardModel.HAS_SUBJECT_KEY + ":"
          + app.getString(R.string.guide_intro_wizard_guide_subject_title);
         if (bundle.containsKey(subjectKey)) {
            requestBody.put("subject", bundle.getBundle(subjectKey).getString(EditTextPage.TEXT_DATA_KEY));
         }

         String introductionKey = app.getString(R.string.guide_intro_wizard_guide_introduction_title);
         if (bundle.containsKey(introductionKey)) {
            requestBody.put("introduction", bundle.getBundle(introductionKey).getString(EditTextPage.TEXT_DATA_KEY));
         }

         String summaryKey = app.getString(R.string.guide_intro_wizard_guide_summary_title);
         if (bundle.containsKey(summaryKey)) {
            requestBody.put("summary", bundle.getBundle(summaryKey).getString(EditTextPage.TEXT_DATA_KEY));
         }

      } catch (JSONException e) {
         return null;
      }

      return requestBody;
   }

   public static ApiCall publishGuide(int guideid, int revisionid) {
      return new ApiCall(ApiEndpoint.PUBLISH_GUIDE,
       guideid + "/public" + "?revisionid=" + revisionid, "");
   }

   public static ApiCall unpublishGuide(int guideid, int revisionid) {
      return new ApiCall(ApiEndpoint.UNPUBLISH_GUIDE,
       guideid + "/public" + "?revisionid=" + revisionid, "");
   }

   public static ApiCall editStep(GuideStep step, int guideid) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("title", step.getTitle());
         requestBody.put("lines", JSONHelper.createLineArray(step.getLines()));
         requestBody.put("media", JSONHelper.createStepMediaJsonObject(step));
      } catch (JSONException e) {
         return null;
      }

      return new ApiCall(ApiEndpoint.UPDATE_GUIDE_STEP, "" + guideid + "/steps/" + step.getStepid() + "?revisionid="
       + step.getRevisionid(), requestBody.toString());
   }

   public static ApiCall createStep(GuideStep step, int guideid, int stepPosition, int revisionid) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("title", step.getTitle());
         requestBody.put("lines", JSONHelper.createLineArray(step.getLines()));
         requestBody.put("orderby", stepPosition);
         requestBody.put("media", JSONHelper.createStepMediaJsonObject(step));
      } catch (JSONException e) {
         return null;
      }

      return new ApiCall(ApiEndpoint.ADD_GUIDE_STEP, "" + guideid + "/steps" + "?revisionid=" + revisionid,
       requestBody.toString());
   }

   public static ApiCall deleteStep(int guideid, GuideStep step) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("revisionid", step.getRevisionid());
      } catch (JSONException e) {
         return null;
      }

      return new ApiCall(ApiEndpoint.DELETE_GUIDE_STEP, "" + guideid + "/steps/" + step.getStepid() + "?revisionid="
       + step.getRevisionid(), requestBody.toString());
   }

   public static ApiCall reorderSteps(Guide guide) {
      JSONObject requestBody = new JSONObject();

      try {
         requestBody.put("stepids", JSONHelper.createStepIdArray(guide.getSteps()));
      } catch (JSONException e) {
         return null;
      }

      return new ApiCall(ApiEndpoint.REORDER_GUIDE_STEPS, "" + guide.getGuideid() + "/steporder" + "?revisionid="
       + guide.getRevisionid(), requestBody.toString());
   }

   /**
    * TODO: Paginate.
    */
   public static ApiCall userGuides() {
      return new ApiCall(ApiEndpoint.USER_GUIDES, NO_QUERY);
   }

   public static ApiCall unpatrolledGuide(int guideid) {
      return new ApiCall(ApiEndpoint.GUIDE_FOR_EDIT, "" + guideid);
   }

   public static ApiCall copyImage(String query) {
      return new ApiCall(ApiEndpoint.COPY_IMAGE, query);
   }

   public static ApiCall userImages(String query) {
      return new ApiCall(ApiEndpoint.USER_IMAGES, query);
   }

   public static ApiCall userVideos(String query) {
      return new ApiCall(ApiEndpoint.USER_VIDEOS, query);
   }

   public static ApiCall userEmbeds(String query) {
      return new ApiCall(ApiEndpoint.USER_EMBEDS, query);
   }

   public static ApiCall uploadImage(String filePath, String extraInformation) {
      return new ApiCall(ApiEndpoint.UPLOAD_IMAGE, filePath, null, extraInformation,
       filePath);
   }

   public static ApiCall uploadImageToStep(String filePath) {
      return new ApiCall(ApiEndpoint.UPLOAD_STEP_IMAGE, filePath, null, null,
       filePath);
   }

   public static ApiCall deleteImage(List<Integer> deleteList) {
      StringBuilder stringBuilder = new StringBuilder();
      String separator = "";

      stringBuilder.append("?imageids=");

      /**
       * Construct a string of imageids separated by comma's.
       */
      for (Integer imageid : deleteList) {
         stringBuilder.append(separator).append(imageid);
         separator = ",";
      }

      return new ApiCall(ApiEndpoint.DELETE_IMAGE, stringBuilder.toString());
   }

   public static ApiCall allTopics() {
      return new ApiCall(ApiEndpoint.ALL_TOPICS, NO_QUERY);
   }

   public static ApiCall sites() {
      return new ApiCall(ApiEndpoint.SITES, NO_QUERY);
   }

   public static ApiCall siteInfo() {
      return new ApiCall(ApiEndpoint.SITE_INFO, NO_QUERY);
   }
}
