package com.dozuki.ifixit.util.api;

import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.gallery.GalleryEmbedList;
import com.dozuki.ifixit.model.gallery.GalleryVideoList;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.search.SearchResults;
import com.dozuki.ifixit.model.story.Story;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.model.topic.TopicNode;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.model.user.UserImage;

import java.util.ArrayList;

/**
 * Base class for API events that are posted to the otto bus.
 */
public abstract class ApiEvent<T> {
   /**
    * Proxy for APIEvents. Api posts these to the bus, BaseActivity
    * listens for them and posts the underlying ApiEvent to the bus if the
    * activityids match.
    */
   public static class ActivityProxy {
      protected ApiEvent<?> mApiEvent;

      public ActivityProxy(ApiEvent<?> apiEvent) {
         mApiEvent = apiEvent;
      }

      public ApiEvent<?> getApiEvent() {
         return mApiEvent;
      }

      public int getActivityid() {
         return mApiEvent.mApiCall.mActivityid;
      }
   }

   public static class Unauthorized extends ApiEvent<String> {}

   public static class Search extends ApiEvent<SearchResults> {}

   public static class Categories extends ApiEvent<TopicNode> {}
   public static class Topic extends ApiEvent<TopicLeaf> {}
   public static class TopicList extends ApiEvent<ArrayList<String>> {}

   public static class Login extends ApiEvent<User> {}
   public static class Logout extends ApiEvent<String> {}
   public static class Register extends ApiEvent<User> {}

   public static class UserImages extends ApiEvent<ArrayList<UserImage>> {}
   public static class UserVideos extends ApiEvent<GalleryVideoList> {}
   public static class UserEmbeds extends ApiEvent<GalleryEmbedList> {}
   public static class UserInfo extends ApiEvent<User> {}
   public static class UserFavorites extends ApiEvent<ArrayList<GuideInfo>> {}

   public static class CopyImage extends ApiEvent<Image> {}
   public static class UploadImage extends ApiEvent<Image> {}
   public static class UploadStepImage extends ApiEvent<Image> {}
   public static class DeleteImage extends ApiEvent<String> {}

   public static class Guides extends ApiEvent<ArrayList<GuideInfo>> {}
   public static class ViewGuide extends ApiEvent<Guide> {}
   public static class DeleteGuide extends ApiEvent<String> {}
   public static class PublishStatus extends ApiEvent<Guide> {}
   public static class UserGuides extends ApiEvent<ArrayList<GuideInfo>> {}
   public static class CompleteGuide extends ApiEvent<Boolean> {}
   public static class GuideForEdit extends ApiEvent<Guide> {}
   public static class CreateGuide extends ApiEvent<Guide> {}
   public static class StepSave extends ApiEvent<GuideStep> {}
   public static class StepReorder extends ApiEvent<Guide> {}
   public static class StepAdd extends ApiEvent<Guide> {}
   public static class StepRemove extends ApiEvent<Guide> {}
   public static class EditGuide extends ApiEvent<Guide> {}
   public static class AddComment extends ApiEvent<Comment> {}
   public static class DeleteComment extends ApiEvent<String> {}
   public static class EditComment extends ApiEvent<Comment> {}
   public static class FavoriteGuide extends ApiEvent<Boolean> {}

   public static class Sites extends ApiEvent<ArrayList<Site>> {}
   public static class SiteInfo extends ApiEvent<Site> {}

   public static class Stories extends ApiEvent<ArrayList<Story>> {}

   public String mResponse;
   public T mResult;
   public ApiCall mApiCall;
   public ApiError mError;
   public int mCode;

   /**
    * True iff response came from the offline storage.
    */
   public boolean mStoredResponse;

   public ApiEvent<T> setResult(T result) {
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

   public ApiEvent<T> setResponse(String response) {
      mResponse = response;
      return this;
   }

   public String getResponse() {
      return mResponse;
   }

   public ApiEvent<T> setCode(int code) {
      mCode = code;
      return this;
   }

   public ApiError getError() {
      return mError;
   }

   public ApiEvent<T> setApiCall(ApiCall apiCall) {
      mApiCall = apiCall;
      return this;
   }

   public ApiEvent<T> setError(ApiError error) {
      mError = error;
      return this;
   }

   public ApiEvent<T> setStoredResponse(boolean stored) {
      mStoredResponse = stored;
      return this;
   }
}
