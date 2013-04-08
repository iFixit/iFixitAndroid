package com.dozuki.ifixit.util;

import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.gallery.UploadedImageInfo;
import com.dozuki.ifixit.model.gallery.UserEmbedList;
import com.dozuki.ifixit.model.gallery.UserImageList;
import com.dozuki.ifixit.model.gallery.UserVideoList;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.model.guide.UserGuide;
import com.dozuki.ifixit.model.login.User;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.model.topic.TopicNode;

import java.util.ArrayList;

/**
 * Base class for API events that are posted to the otto bus.
 */
public abstract class APIEvent<T> {
   public static class Categories extends APIEvent<TopicNode> {}
   public static class ViewGuide extends APIEvent<Guide> {}
   public static class Topic extends APIEvent<TopicLeaf> {}
   public static class Login extends APIEvent<User> {}
   public static class Logout extends APIEvent<String> {}
   public static class Register extends APIEvent<User> {}
   public static class UserImages extends APIEvent<UserImageList> {}
   public static class UserVideos extends APIEvent<UserVideoList> {}
   public static class UserEmbeds extends APIEvent<UserEmbedList> {}
   public static class UploadImage extends APIEvent<UploadedImageInfo> {}
   public static class DeleteImage extends APIEvent<String> {}
   public static class DeleteGuide extends APIEvent<String> {}
   public static class PublishStatus extends APIEvent<Guide> {}
   public static class UserGuides extends APIEvent<ArrayList<UserGuide>> {}
   public static class GuideForEdit extends APIEvent<Guide> {}
   public static class CreateGuide extends APIEvent<Guide> {}
   public static class StepSave extends APIEvent<GuideStep> {}
   public static class StepReorder extends APIEvent<Guide> {}
   public static class StepAdd extends APIEvent<Guide> {}
   public static class StepRemove extends APIEvent<Guide> {}
   public static class EditGuide extends APIEvent<Guide> {}
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
