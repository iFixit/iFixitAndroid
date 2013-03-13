package com.dozuki.ifixit.util;

import java.util.ArrayList;

import com.dozuki.ifixit.dozuki.model.Site;
import com.dozuki.ifixit.gallery.model.UploadedImageInfo;
import com.dozuki.ifixit.gallery.model.UserEmbedList;
import com.dozuki.ifixit.gallery.model.UserImageList;
import com.dozuki.ifixit.gallery.model.UserVideoList;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.model.UserGuide;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.topic_view.model.TopicLeaf;
import com.dozuki.ifixit.topic_view.model.TopicNode;

/**
 * Base class for API events that are posted to the otto bus.
 */
public abstract class APIEvent<T> {
   public static class Categories extends APIEvent<TopicNode> {}
   public static class Guide extends APIEvent<com.dozuki.ifixit.guide_view.model.Guide> {}
   public static class Topic extends APIEvent<TopicLeaf> {}
   public static class Login extends APIEvent<User> {}
   public static class Logout extends APIEvent<String> {}
   public static class Register extends APIEvent<User> {}
   public static class UserImages extends APIEvent<UserImageList> {}
   public static class UserVideos extends APIEvent<UserVideoList> {}
   public static class UserEmbeds extends APIEvent<UserEmbedList> {}
   public static class UploadImage extends APIEvent<UploadedImageInfo> {}
   public static class DeleteImage extends APIEvent<String> {}
   public static class UserGuides extends APIEvent<ArrayList<UserGuide>> {}
   public static class GuideForEdit extends APIEvent<GuideCreateObject> {}
   public static class CreateGuide extends APIEvent<GuideCreateObject> {}
   public static class StepSave extends APIEvent<GuideCreateObject> {}
   public static class StepReorder extends APIEvent<GuideCreateObject> {}
   public static class StepAdd extends APIEvent<GuideCreateObject> {}
   public static class StepRemove extends APIEvent<GuideCreateObject> {}
   public static class EditGuide extends APIEvent<GuideCreateObject> {}
   public static class Sites extends APIEvent<ArrayList<Site>> {}
   public static class UserInfo extends APIEvent<User> {}

   public String mResponse;
   public T mResult;
   public APIError mError;
   public String mExtraInfo;

   public APIEvent<T> setResult(T result) {
      mResult = result;

      return this;
   }

   public T getResult() {
      return mResult;
   }

   public void setExtraInfo(String info) {
      mExtraInfo = info;
   }

   public String getExtraInfo() {
      return mExtraInfo;
   }

   public boolean hasError() {
      return mError != null;
   }

   public APIEvent<T> setResponse(String response) {
      mResponse = response;
      return this;
   }

   public String getResponse() {
      return mResponse;
   }

   public APIError getError() {
      return mError;
   }

   public APIEvent<T> setError(APIError error) {
      mError = error;
      return this;
   }
}
