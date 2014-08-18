package com.dozuki.ifixit.model.story;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Stub Story class.
 *
 * TODO: Fill in more fields.
 */
public class Story implements Serializable {
   public int mStoryid;

   public Story(String json) throws JSONException {
      this(new JSONObject(json));
   }

   public Story(JSONObject jStory) throws JSONException {
      mStoryid = jStory.getInt("storyid");
   }
}
