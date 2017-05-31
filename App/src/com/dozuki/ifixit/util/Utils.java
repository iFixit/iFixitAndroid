package com.dozuki.ifixit.util;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.URLSpan;
import android.widget.ImageView;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;

public class Utils {
   /**
    * Trim off whitespace from the beginning and end of a given string.
    * @param s
    * @param start
    * @param end
    * @return the trimmed string
    */
   public static CharSequence trim(CharSequence s, int start, int end) {
      while (start < end && Character.isWhitespace(s.charAt(start))) {
         start++;
      }

      while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
         end--;
      }

      return s.subSequence(start, end);
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

   public static String cleanWikiHtml(String html) {

      // Remove anchor elements from html
      html = html.replaceAll("<a class=\\\"anchor\\\".+?<\\/a>", "");
      html = html.replaceAll("<span class=\\\"editLink headerLink\\\".+?<\\/span>", "");

      return html;
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
                  urlSpan = new URLSpan("https://" + site.mDomain + urlSpan.getURL());
               } else {
                  urlSpan = new URLSpan("https://" + site.mDomain + "/" + urlSpan.getURL());
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

   public static CharSequence getRelativeTime(Context context, long timeInMs) {
      final long MS_IN_MINUTE = 60000;
      if (System.currentTimeMillis() - timeInMs < MS_IN_MINUTE) {
         return context.getString(R.string.just_now);
      } else {
         return DateUtils.getRelativeTimeSpanString(timeInMs);
      }
   }
}
