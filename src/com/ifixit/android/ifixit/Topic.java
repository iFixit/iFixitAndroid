package com.ifixit.android.ifixit;

import java.io.Serializable;
import java.util.ArrayList;

public class Topic implements Serializable {
   private String mName;
   private ArrayList<Topic> mChildren;

   public Topic(String name) {
      mName = name;
      mChildren = new ArrayList<Topic>();
   }

   public String getName() {
      return mName;
   }

   public ArrayList<Topic> getChildren() {
      return mChildren;
   }

   public void addAllTopics(ArrayList<Topic> topics) {
      mChildren.addAll(topics);
   }

   public boolean isLeaf() {
      return mChildren.size() == 0;
   }

   public String toString() {
      return "{Name: " + mName + ", Topics: " + mChildren + "}";
   }
}
