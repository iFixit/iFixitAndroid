package com.dozuki.ifixit.model;

public class Badges {
   private int mBronze;
   private int mSilver;
   private int mGold;

   public Badges() {
      mBronze = 0;
      mSilver = 0;
      mGold = 0;
   }

   public Badges(int bronze, int silver, int gold) {
      mBronze = bronze;
      mSilver = silver;
      mGold = gold;
   }

   public int getGold() {
      return mGold;
   }

   public int getSilver() {
      return mSilver;
   }

   public int getBronze() {
      return mBronze;
   }

   public int getTotal() {
      return getGold() + getSilver() + getBronze();
   }

}
