package com.dozuki.ifixit.util;

import com.dozuki.ifixit.dozuki.model.Site;
import com.dozuki.ifixit.gallery.model.UploadedImageInfo;
import com.dozuki.ifixit.gallery.model.UserImageList;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.topic_view.model.TopicLeaf;
import com.dozuki.ifixit.topic_view.model.TopicNode;

import java.util.ArrayList;

/**
 * Base class for API events that are posted to the otto bus.
 */
public abstract class APIEvent<T> {
   public static class Categories extends APIEvent<TopicNode> {}
   public static class Guide extends APIEvent<com.dozuki.ifixit.guide_view.model.Guide> {}
   public static class Topic extends APIEvent<TopicLeaf> {}
   public static class Login extends APIEvent<User> {}
   public static class Register extends APIEvent<User> {}
   public static class UserImages extends APIEvent<UserImageList> {}
   public static class UploadImage extends APIEvent<UploadedImageInfo> {}
   public static class DeleteImage extends APIEvent<String> {}
   public static class Sites extends APIEvent<ArrayList<Site>> {}

   public T mResult;

   public APIEvent<T> set(T result) {
       mResult = result;

       return this;
   }

   public T getResult() {
      return mResult;
   }
}
