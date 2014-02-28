package com.dozuki.ifixit.util;

import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.widget.ImageView;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.model.dozuki.Site;
import com.squareup.okhttp.OkHttpClient;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;

public class Utils {

   public static OkHttpClient createOkHttpClient() {
      OkHttpClient client = new OkHttpClient();
      SSLContext sslContext;
      try {
         sslContext = SSLContext.getInstance("TLS");
         sslContext.init(null, null, null);
      } catch (GeneralSecurityException e) {
         throw new AssertionError(); // The system has no TLS. Just give up.
      }
      client.setSslSocketFactory(sslContext.getSocketFactory());

      return client;
   }

   public static void stripImageView(ImageView view) {
      if (view.getDrawable() instanceof BitmapDrawable) {
         ((BitmapDrawable) view.getDrawable()).getBitmap().recycle();
      }

      safeStripImageView(view);
   }

   /**
    * ImageView stripping without the "dangerous" bitmap recycle.
    *
    * @param view ImageView to clean memory
    */
   public static void safeStripImageView(ImageView view) {
      if (view.getDrawable() != null) {
         view.getDrawable().setCallback(null);
      }

      view.setImageDrawable(null);
      view.getResources().flushLayoutCache();
      view.destroyDrawingCache();
   }

   /**
    * Strips out newlines from Editables
    */
   public static Editable stripNewlines(Editable s) {
      for (int i = s.length(); i > 0; i--) {
         if (s.subSequence(i-1, i).toString().equals("\n")) {
            s.replace(i-1, i, "");
         }
      }
      return s;
   }

   /**
    * Removes relative link hrefs
    *
    * @param spantext (from Html.fromhtml())
    * @return spanned with fixed links
    */
   public static Spanned correctLinkPaths(Spanned spantext) {
      Object[] spans = spantext.getSpans(0, spantext.length(), Object.class);
      for (Object span : spans) {
         int start = spantext.getSpanStart(span);
         int end = spantext.getSpanEnd(span);
         int flags = spantext.getSpanFlags(span);

         Site site = App.get().getSite();

         if (span instanceof URLSpan) {
            URLSpan urlSpan = (URLSpan) span;
            if (!urlSpan.getURL().startsWith("http")) {
               if (urlSpan.getURL().startsWith("/")) {
                  urlSpan = new URLSpan("http://" + site.mDomain + urlSpan.getURL());
               } else {
                  urlSpan = new URLSpan("http://" + site.mDomain + "/" + urlSpan.getURL());
               }
            }
            ((Spannable) spantext).removeSpan(span);
            ((Spannable) spantext).setSpan(urlSpan, start, end, flags);
         }
      }

      return spantext;
   }

   public static String capitalize(String word) {
      return Character.toUpperCase(word.charAt(0)) + word.substring(1);
   }

   public static String repeat(String string, int times) {
      StringBuilder builder = new StringBuilder(string);
      for (int i = 1; i < times; i++) {
         builder.append(string);
      }

      return builder.toString();
   }
}
