package com.dozuki.ifixit.model.user;

import com.dozuki.ifixit.model.Badges;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.util.LatLon;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {
   private static final long serialVersionUID = 6209686573978334361L;

   @SerializedName("userid") private int mUserid;
   @SerializedName("username") private String mUsername;
   @SerializedName("image") private Image mAvatar;
   @SerializedName("reputation") private int mReputation;
   @SerializedName("join_date") private int mDate;
   @SerializedName("location") private LatLon mLocation;
   @SerializedName("certification_count") private int mCertificationCount;
   @SerializedName("badge_counts") private Badges mBadges;
   @SerializedName("summary") private String mSummary;
   @SerializedName("about_raw") private String mAboutRaw;
   @SerializedName("about_rendered") private String mAboutRendered;
   @SerializedName("authToken") private String mAuthToken;

   public User() {}

   public int getUserid() {
      return mUserid;
   }

   public void setUserid(int mUserid) {
      this.mUserid = mUserid;
   }

   public String getUsername() {
      return mUsername;
   }

   public void setUsername(String mUsername) {
      this.mUsername = mUsername;
   }

   public Image getAvatar() {
      return mAvatar;
   }

   public void setAvatar(Image mAvatar) {
      this.mAvatar = mAvatar;
   }

   public int getReputation() {
      return mReputation;
   }

   public void setReputation(int mReputation) {
      this.mReputation = mReputation;
   }

   public LatLon getLocation() {
      return mLocation;
   }

   public void setLocation(LatLon mLocation) {
      this.mLocation = mLocation;
   }

   public void setLocation(String location) {
      mLocation = new LatLon(location);
   }

   public int getJoinDate() {
      return mDate;
   }

   public void setJoinDate(int mDate) {
      this.mDate = mDate;
   }

   public int getCertificationCount() {
      return mCertificationCount;
   }

   public void setCertificationCount(int mCertificationCount) {
      this.mCertificationCount = mCertificationCount;
   }

   public Badges getBadges() {
      return mBadges;
   }

   public void setBadges(Badges mBadges) {
      this.mBadges = mBadges;
   }

   public String getSummary() {
      return mSummary;
   }

   public void setSummary(String mSummary) {
      this.mSummary = mSummary;
   }

   public String getAboutRaw() {
      return mAboutRaw;
   }

   public void setAboutRaw(String mAboutRaw) {
      this.mAboutRaw = mAboutRaw;
   }

   public String getAboutRendered() {
      return mAboutRendered;
   }

   public void setAboutRendered(String mAboutRendered) {
      this.mAboutRendered = mAboutRendered;
   }

   public String getAuthToken() {
      return mAuthToken;
   }

   public void setAuthToken(String mAuthToken) {
      this.mAuthToken = mAuthToken;
   }
}