package com.dozuki.ifixit.model.story;

import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.Product;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.model.user.User;
import com.dozuki.ifixit.util.JSONHelper;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Story implements Serializable {
   public int mStoryid;
   public Date mDate;
   public String mUrl;
   public String mTitleRendered;
   public String mIntroRendered;
   public String mBodyRendered;
   public String mConclusionRendered;
   public User mAuthor;
   public ArrayList<Image> mImages;
   public ArrayList<GuideInfo> mGuides;
   public ArrayList<Product> mProducts;

   public Story(String json) throws JSONException {
      this(new JSONObject(json));
   }

   public Story(JSONObject jStory) throws JSONException {
      mStoryid = jStory.getInt("storyid");
      mDate = new Date(jStory.getInt("date") * 1000);
      mUrl = jStory.getString("view_url");
      mTitleRendered = jStory.getString("title_rendered");
      mIntroRendered = jStory.getString("intro_rendered");
      mBodyRendered = jStory.getString("body_rendered");
      mConclusionRendered = jStory.getString("conclusion_rendered");
      mAuthor = JSONHelper.parseUserLight(jStory.getJSONObject("author"));

      mImages = new ArrayList<Image>();
      JSONArray jImages = jStory.getJSONArray("images");
      for (int i = 0; i < jImages.length(); i++) {
         mImages.add(JSONHelper.parseImage(jImages.getJSONObject(i), null));
      }

      Gson gson = new Gson();
      mGuides = new ArrayList<GuideInfo>();
      JSONArray jGuides = jStory.getJSONArray("guides");
      for (int i = 0; i < jGuides.length(); i++) {
         mGuides.add(gson.fromJson(jGuides.getJSONObject(i).toString(), GuideInfo.class));
      }

      mProducts = new ArrayList<Product>();
      JSONArray jProducts = jStory.getJSONArray("products");
      for (int i = 0; i < jProducts.length(); i++) {
         mProducts.add(new Product(jProducts.getJSONObject(i)));
      }
   }
}
