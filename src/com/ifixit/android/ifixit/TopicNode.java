package com.ifixit.android.ifixit;

import java.io.Serializable;
import java.util.ArrayList;

public class TopicNode implements Serializable {
   private static final long serialVersionUID = 1L;

   private String mName;
   private ArrayList<TopicNode> mChildren;

   public TopicNode(String name) {
      mName = name;
      mChildren = new ArrayList<TopicNode>();
   }

   public String getName() {
      return mName;
   }

   public ArrayList<TopicNode> getChildren() {
      return mChildren;
   }

   public void addAllTopics(ArrayList<TopicNode> topics) {
      mChildren.addAll(topics);
   }

   public boolean isLeaf() {
      return mChildren.size() == 0;
   }

   public String toString() {
      return "{Name: " + mName + ", Topics: " + mChildren + "}";
   }
}
