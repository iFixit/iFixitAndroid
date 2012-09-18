package com.dozuki.ifixit.view.model;

import java.io.Serializable;

public class UserImageInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6957010569324490644L;
	String mImageId;
	String mGuid;
	public String getmImageId() {
		return mImageId;
	}
	public void setmImageId(String mImageId) {
		this.mImageId = mImageId;
	}
	public String getmGuid() {
		return mGuid;
	}
	public void setmGuid(String mGuid) {
		this.mGuid = mGuid;
	}
	public String getmWidth() {
		return mWidth;
	}
	public void setmWidth(String mWidth) {
		this.mWidth = mWidth;
	}
	public String getmHeight() {
		return mHeight;
	}
	public void setmHeight(String mHeight) {
		this.mHeight = mHeight;
	}
	public String getmRatio() {
		return mRatio;
	}
	public void setmRatio(String mRatio) {
		this.mRatio = mRatio;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	String mWidth;
	String mHeight;
	String mRatio;

}
