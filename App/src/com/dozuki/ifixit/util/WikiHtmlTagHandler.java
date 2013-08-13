package com.dozuki.ifixit.util;

import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.BulletSpan;
import android.text.style.StrikethroughSpan;
import org.xml.sax.XMLReader;


public class WikiHtmlTagHandler implements Html.TagHandler {

   private static final String LIST_INDENT = "    ";
   private static final String BULLET = "•";
   private static final String NEWLINE = "\n";

   private int level = 0;

   public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
      if (tag.equalsIgnoreCase("strike") || tag.equals("s")) {
         handleStrike(opening, output);
      } else if (tag.equalsIgnoreCase("ul") || tag.equalsIgnoreCase("ol")) {
         handleList(opening, tag, output);
      } else if (tag.equalsIgnoreCase("li")) {
         handleListItem(opening, output);
      } else if (tag.equalsIgnoreCase("img")) {
         output.append(NEWLINE);
      }
   }

   private void handleList(boolean opening, String tag, Editable output) {
      if (opening) {
         level++;
      } else {
         level--;
         output.append(NEWLINE);
      }
   }

   private void handleListItem(boolean opening, Editable output) {
      int len = output.length();

      if (opening) {
         output.setSpan(new BulletSpan(), len, len, Spannable.SPAN_MARK_MARK);
      } else {
         Object obj = getLast(output, BulletSpan.class);
         int where = output.getSpanStart(obj);

         output.removeSpan(obj);

         if (where != len) {
            if (output.charAt(where - 1) != '\n') {
               output.insert(where, NEWLINE);
               where += NEWLINE.length(); // so the margin and bullet are inserted after the newline
            }

            output.insert(where, Utils.repeat(LIST_INDENT, level) + BULLET + " ");
         }
      }
   }

   private void handleStrike(boolean opening, Editable output) {
      int len = output.length();
      if (opening) {
         output.setSpan(new StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK);
      } else {
         Object obj = getLast(output, StrikethroughSpan.class);
         int where = output.getSpanStart(obj);

         output.removeSpan(obj);

         if (where != len) {
            output.setSpan(new StrikethroughSpan(), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
         }
      }
   }

   private Object getLast(Editable text, Class kind) {
      Object[] objs = text.getSpans(0, text.length(), kind);

      if (objs.length == 0) {
         return null;
      } else {
         for (int i = objs.length; i > 0; i--) {
            if (text.getSpanFlags(objs[i - 1]) == Spannable.SPAN_MARK_MARK) {
               return objs[i - 1];
            }
         }
         return null;
      }
   }
}

