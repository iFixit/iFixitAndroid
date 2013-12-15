package com.dozuki.ifixit.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.Serializable;

public class Embed implements Serializable {

   private static final long serialVersionUID = 1L;
   public String mSourceUrl;
   public int mEmbedid;
   public String mUrl;
   public String mTitle;
   public String mType;
   public String mProviderUrl;
   public String mHtml;
   public String mProviderName;
   public String mAuthorUrl;
   public String mAuthorName;
   public String mCacheAge;
   public String mVersion;
   public int mWidth;
   public int mHeight;

   public Embed(JSONObject embed) {
      try {
         mUrl = embed.getString("url");
         mWidth = embed.getInt("width");
         mHeight = embed.getInt("height");
         mAuthorName = embed.isNull("author_name") ? "" : embed.getString("author_name");
         mAuthorUrl = embed.isNull("author_url") ? "" : embed.getString("author_url");
         mCacheAge = embed.isNull("cache_age") ? "" : embed.getString("cache_age");
         mProviderName = embed.isNull("provider_name") ? "" : embed.getString("provider_name");
         mVersion = embed.getString("version");
         mHtml = embed.getString("html");
         mSourceUrl = getSourceUrl(mHtml);
         mProviderUrl = embed.getString("provider_url");
         mType = embed.getString("type");
         mEmbedid = embed.getInt("embedid");
         mTitle = embed.getString("title");

      } catch (JSONException e) {
         e.printStackTrace();
      }
   }

   private String getSourceUrl(String html) {
      Document doc = Jsoup.parse(html);

      return doc.getElementsByAttribute("src").get(0).attr("src");
   }
}
