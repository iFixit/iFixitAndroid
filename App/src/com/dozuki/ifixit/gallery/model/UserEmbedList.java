package com.dozuki.ifixit.gallery.model;

import java.io.Serializable;
import java.util.ArrayList;

public class UserEmbedList implements Serializable{
	private static final long serialVersionUID = -8093775979422512869L;
	private ArrayList<UserEmbedInfo> mEmbeds;

	   public UserEmbedList() {
	      mEmbeds = new ArrayList<UserEmbedInfo>();
	   }

	   public void addEmbed(UserEmbedInfo userEmbedInfo) {
	    
	   }

	   public ArrayList<UserEmbedInfo> getEmbeds() {
	      return mEmbeds;
	   }

	   public void setEmbeds(ArrayList<UserEmbedInfo> embeds) {
	      mEmbeds = embeds;
	   }
}