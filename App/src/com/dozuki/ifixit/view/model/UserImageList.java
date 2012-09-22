package com.dozuki.ifixit.view.model;

import java.io.Serializable;
import java.util.ArrayList;

public class UserImageList  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7067096480019401662L;
	
	private User mUser;
	private ArrayList<UserImageInfo> mImages;
	
	
	
	public UserImageList() {
		mImages = new ArrayList<UserImageInfo> ();
	}
	
	public User getUser() {
		return mUser;
	}
	
	public void addImage(UserImageInfo userImageInfo) {
		mImages.add(userImageInfo);
	}



	public void setUser(User mUser) {
		this.mUser = mUser;
	}


	public ArrayList<UserImageInfo> getImages() {
		return mImages;
	}


	public void setImages(ArrayList<UserImageInfo> mImages) {
		this.mImages = mImages;
	}

}
