package com.dozuki.ifixit.util;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;

/**
 * Defines image sizes for various parts of the app.
 */
public class ImageSizes {
   public static String stepThumb;
   public static String stepMain;
   public static String stepFull;
   public static String stepList;
   public static String guideList;
   public static String topicMain;
   public static String logo;
   public static String commentAvatar;

   public static void init(App app) {
      stepThumb = app.getString(R.string.is__guide_step_thumbnail);
      stepMain = app.getString(R.string.is__guide_step_main);
      stepFull = app.getString(R.string.is__guide_step_fullsize);
      stepList = app.getString(R.string.is__step_list);
      guideList = app.getString(R.string.is__guide_list);
      topicMain = app.getString(R.string.is__topic_main);
      logo = app.getString(R.string.is__actionbar_logo);
      commentAvatar = app.getString(R.string.is__comment_avatar);
   }
}
