package com.ifixit.android.ifixit;

import java.io.Serializable;
import java.util.ArrayList;

public class Device implements Serializable {
   private String mName;
   private ArrayList<Device> mChildren;

   public Device(String name) {
      mName = name;
      mChildren = new ArrayList<Device>();
   }

   public String getName() {
      return mName;
   }

   public ArrayList<Device> getChildren() {
      return mChildren;
   }

   public void addAllDevices(ArrayList<Device> devices) {
      mChildren.addAll(devices);
   }

   public boolean isLeaf() {
      return mChildren.size() == 0;
   }

   public String toString() {
      return "{Name: " + mName + ", Devices: " + mChildren + "}";
   }
}
