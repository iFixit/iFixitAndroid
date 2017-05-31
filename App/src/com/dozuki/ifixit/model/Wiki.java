package com.dozuki.ifixit.model;

import com.dozuki.ifixit.model.Image;
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
