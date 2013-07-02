package com.dozuki.ifixit.model.topic;

import com.dozuki.ifixit.model.Flag;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.Item;
import com.dozuki.ifixit.model.guide.GuideInfo;
import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;

public class TopicLeaf implements Serializable {
   private static final long serialVersionUID = 1L;

   private String mName;
   private String mTitle;
   private String mLocale;
   private Image mImage;
   private String mDescription;
   private ArrayList<Flag> mFlags;
   private ArrayList<GuideInfo> mGuides;
   private int mSolutions;
   private String mSolutionsUrl;
   private ArrayList<Item> mParts;
   private ArrayList<Item> mTools;
   private String mContentsRaw;
   private String mContentsRendered;

   public TopicLeaf(String name) {
      mName = name;
      mGuides = new ArrayList<GuideInfo>();
      mParts = new ArrayList<Item>();
      mTools = new ArrayList<Item>();
      mFlags = new ArrayList<Flag>();
   }

   public void addGuide(GuideInfo guideInfo) {
      mGuides.add(guideInfo);
   }

   public String getName() {
      return mName;
   }

   public ArrayList<GuideInfo> getGuides() {
      return mGuides;
   }

   public void setImage(Image image) {
      mImage = image;
   }

   public void setSolutionsUrl(String url) {
      mSolutionsUrl = url;
   }

   public void setNumSolutions(int solutions) {
      mSolutions = solutions;
   }

   public String getSolutionsUrl() {
      return mSolutionsUrl;
   }

   public int getNumSolutions() {
      return mSolutions;
   }

   public void setDescription(String description) {
      mDescription = description;
   }

   public String getDescription() {
      return mDescription;
   }

   public void addPart(Item part) {
      mParts.add(part);
   }

   public void addParts(JSONArray parts) {
      int numParts = parts.length();

      for (int i = 0; i < numParts; i++) {

      }
   }

   public void addTool(Item tool) {
      mTools.add(tool);
   }

   public void addFlag(Flag flag) {
      mFlags.add(flag);
   }

   public String toString() {
      return mName + ", " + mGuides;
   }

   public boolean equals(Object other) {
      return other instanceof TopicLeaf &&
       ((TopicLeaf)other).getName().equals(mName);
   }

   public void setLocale(String locale) {
      mLocale = locale;
   }

   public void setContentsRaw(String contentsRaw) {
      mContentsRaw = contentsRaw;
   }

   public void setContentsRendered(String contentsRendered) {
      mContentsRendered = contentsRendered;
   }

   public String getTitle() {
      return mTitle;
   }

   public void setTitle(String title) {
      mTitle = title;
   }

   public String getContentRendered() {
      return mContentsRendered;
   }

   public Image getImage() {
      return mImage;
   }
}
