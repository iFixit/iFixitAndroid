package com.dozuki.ifixit.model;

import com.dozuki.ifixit.util.JSONHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Product implements Serializable {
   public String mTitle;
   public String mText;
   public String mUrl;
   public double mPrice;
   public String mProductCode;
   public Image mImage;

   public Product(String json) throws JSONException {
      this(new JSONObject(json));
   }

   public Product(JSONObject jProduct) throws JSONException {
      mTitle = jProduct.getString("title");
      mText = jProduct.getString("text");
      mUrl = jProduct.getString("url");
      mPrice = jProduct.getDouble("price");
      mProductCode = jProduct.getString("productCode");
      mImage = JSONHelper.parseImage(jProduct, "image");
   }
}
