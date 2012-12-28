package com.dozuki.ifixit.gallery.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class UserVideoList implements Serializable{

	private static final long serialVersionUID = 7212089949959439124L;

	
	 private ArrayList<UserVideoInfo> mVideos;

	   public UserVideoList() {
	      mVideos = new ArrayList<UserVideoInfo>();
	   }

	   public void addImage(UserVideoInfo userVideoInfo) {
	    
	   }

	   public ArrayList<UserVideoInfo> getVideos() {
	      return mVideos;
	   }

	   public void setImages(ArrayList<UserVideoInfo> videos) {
	      mVideos = videos;
	   }
}
