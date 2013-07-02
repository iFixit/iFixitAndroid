package com.dozuki.ifixit.util;

public class LatLon {
   private double latitude;
   private double longitude;

   public LatLon(String latlon) {
      if (latlon.length() != 0) {
         String[] values = latlon.split(",");
         this.latitude = Double.valueOf(values[0]);
         this.longitude = Double.valueOf(values[1]);
      } else {
         this.latitude = 0.0;
         this.longitude = 0.0;
      }
   }

   public LatLon(double latitude, double longitude) {
      this.latitude = latitude;
      this.longitude = longitude;
   }

   public Double getLatitude() {
      return this.latitude;
   }

   public Double getLongitude() {
      return this.longitude;
   }
}
