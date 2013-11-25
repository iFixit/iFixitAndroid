package com.dozuki.ifixit.util;

import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.gallery.GalleryEmbedList;
import com.dozuki.ifixit.model.gallery.GalleryVideoList;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.search.SearchResults;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.model.topic.TopicNode;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.model.user.UserImage;

import java.util.ArrayList;

/**
 * Base class for API events that are posted to the otto bus.
 */
public abstract class APIEvent<T> {
   /**
    * Proxy for APIEvents. APIService posts these to the bus, BaseActivity
    * listens for them and posts the underlying APIEvent to the bus if the
    * activityids match.
    */
   public static class ActivityProxy {
      protected APIEvent<?> mApiEvent;

      public ActivityProxy(APIEvent<?> apiEvent) {
         mApiEvent = apiEvent;
      }

      public APIEvent<?> getApiEvent() {
         return mApiEvent;
      }

      public int getActivityid() {
         return mApiEvent.mApiCall.mActivityid;
      }
   }

   public static class Unauthorized extends APIEvent<String> {}

   public static class Search extends APIEvent<SearchResults> {}

   public static class Categories extends APIEvent<TopicNode> {}
   public static class Topic extends APIEvent<TopicLeaf> {}
   public static class TopicList extends APIEvent<ArrayList<String>> {}

   public static class Login extends APIEvent<User> {}
   public static class Logout extends APIEvent<String> {}
   public static class Register extends APIEvent<User> {}

   public static class Image extends APIEvent<com.dozuki.ifixit.api_2_0.Image> {}
   public static class UserImages extends APIEvent<ArrayList<UserImage>> {}
   public static class UserVideos extends APIEvent<GalleryVideoList> {}
   public static class UserEmbeds extends APIEvent<GalleryEmbedList> {}
   public static class UserInfo extends APIEvent<User> {}
   public static class UserFavorites extends APIEvent<ArrayList<GuideInfo>> {}

   public static class CopyImage extends APIEvent<com.dozuki.ifixit.model.Image> {}
   public static class UploadImage extends APIEvent<com.dozuki.ifixit.model.Image> {}
   public static class UploadStepImage extends APIEvent<com.dozuki.ifixit.model.Image> {}
   public static class DeleteImage extends APIEvent<String> {}

   public static class Guides extends APIEvent<ArrayList<GuideInfo>> {}
   public static class ViewGuide extends APIEvent<Guide> {}
   public static class DeleteGuide extends APIEvent<String> {}
   public static class PublishStatus extends APIEvent<Guide> {}
   public static class UserGuides extends APIEvent<ArrayList<GuideInfo>> {}
   public static class GuideForEdit extends APIEvent<Guide> {}
   public static class CreateGuide extends APIEvent<Guide> {}
   public static class StepSave extends APIEvent<GuideStep> {}
   public static class StepReorder extends APIEvent<Guide> {}
   public static class StepAdd extends APIEvent<Guide> {}
   public static class StepRemove extends APIEvent<Guide> {}
   public static class EditGuide extends APIEvent<Guide> {}

   public static class Sites extends APIEvent<ArrayList<Site>> {}
   public static class SiteInfo extends APIEvent<Site> {}

   public String mResponse;
   public byte[] mRawOutput;
   public T mResult;
   public APICall mApiCall;
   public APIError mError;
   public int mCode;

   public APIEvent<T> setResult(T result) {
      mResult = result;
      return this;
   }

   public T getResult() {
      return mResult;
   }

   public String getExtraInfo() {
      return mApiCall.mExtraInfo;
   }

   public boolean hasError() {
      return mError != null;
   }

   public APIEvent<T> setResponse(String response, byte[] rawOutput) {
      mResponse = response;
      mRawOutput = rawOutput;
      return this;
   }

   public String getResponse() {
      return mResponse;
   }

   public APIEvent<T> setCode(int code) {
      mCode = code;
      return this;
   }

   public APIError getError() {
      return mError;
   }

   public APIEvent<T> setApiCall(APICall apiCall) {
      mApiCall = apiCall;
      return this;
   }

   public APIEvent<T> setError(APIError error) {
      mError = error;
      return this;
   }
}
