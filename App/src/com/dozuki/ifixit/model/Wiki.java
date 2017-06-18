package com.dozuki.ifixit.model;

import android.text.Html;
import android.text.Spanned;

import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.util.Utils;
import com.dozuki.ifixit.util.WikiHtmlTagHandler;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;

public class Wiki implements Serializable {
   private static final long serialVersionUID = 1L;

   public int wikiid;
   public int revisionid;
   public String langid = "en";
   @SerializedName("source_revisionid")
   public int sourceRevisionid;
   public String title = "";
   @SerializedName("display_title")
   public String displayTitle = "";
   public String namespace = "WIKI";
   public String summary = "";
   public String url = "";
   @SerializedName("contents_raw")
   public String contentsRaw = "";
   @SerializedName("contents_rendered")
   public String contentsRendered = "";
   public ArrayList<Flag> flags = new ArrayList<>();
   public ArrayList<Document> documents = new ArrayList<>();
   @SerializedName("can_edit")
   public boolean canEdit = false;
   @SerializedName("table_of_contents")
   public boolean tableOfContents = false;
   public String text = "";
   public Image image = new Image();


   public Wiki(int wikiid) {
      this.wikiid = wikiid;
   }

   public boolean hasImage() {
      return image != null;
   }

   public Spanned getContentSpanned(Html.ImageGetter imgGetter) {
      String html = Utils.cleanWikiHtml(contentsRendered);

      Spanned htmlParsed;

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
         htmlParsed = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY, imgGetter, new WikiHtmlTagHandler());
      } else {
         htmlParsed = Html.fromHtml(contentsRendered, imgGetter, new WikiHtmlTagHandler());
      }

      htmlParsed = Utils.correctLinkPaths(htmlParsed);


      return Utils.correctLinkPaths(htmlParsed);
   }

   public Image getImage() {
      return image;
   }

   public String getImagePath(String size) {
      String path = "";
      if (image != null) {
         path = image.getPath(size);
      }

      return path;
   }
}
