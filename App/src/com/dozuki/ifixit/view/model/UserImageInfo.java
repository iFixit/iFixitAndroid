package com.dozuki.ifixit.view.model;

import java.io.Serializable;

public class UserImageInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6957010569324490644L;
	String mImageId;
	String mGuid;
	String mLocalPath;
	boolean mLoaded;
	
	
	public void setLoaded(boolean loaded)
	{
		mLoaded = loaded;
	}
	public String getmImageId() {
		return mImageId;
	}
	public void setmImageId(String mImageId) {
		this.mImageId = mImageId;
	}
	public String getlocalPath(){
		return mLocalPath;
	}
	public void setlocalPath(String mLocal)
	{
		mLocalPath = mLocal;
	}
	public String getmGuid() {
		return mGuid;
	}
	public void setGuid(String mGuid) {
		this.mGuid = mGuid;
	}
	public String getWidth() {
		return mWidth;
	}
	public void setWidth(String mWidth) {
		this.mWidth = mWidth;
	}
	public String getHeight() {
		return mHeight;
	}
	public void setHeight(String mHeight) {
		this.mHeight = mHeight;
	}
	public String getRatio() {
		return mRatio;
	}
	public void setRatio(String mRatio) {
		this.mRatio = mRatio;
	}

	String mWidth;
	String mHeight;
	String mRatio;


	public boolean getLoaded() {
		// TODO Auto-generated method stub
		return mLoaded;
	}

}
