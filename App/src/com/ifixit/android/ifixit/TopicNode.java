package com.ifixit.android.ifixit;

import java.io.Serializable;
import java.util.ArrayList;

public class TopicNode implements Serializable {
   private static final long serialVersionUID = 1L;
   protected static final String ROOT_NAME = "ROOT";

   private String mName;
   private ArrayList<TopicNode> mChildren;

   public TopicNode() {
      this(ROOT_NAME);
   }

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

   public boolean isRoot() {
      return mName.equals(ROOT_NAME);
   }

   public String toString() {
      return "{Name: " + mName + ", Topics: " + mChildren + "}";
   }
}
