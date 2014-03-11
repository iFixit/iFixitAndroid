package com.dozuki.ifixit.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlImageGetter implements Html.ImageGetter {
   Context context;
   View container;

   /**
    * Construct the URLImageParser which will execute AsyncTask and refresh the container
    *
    * @param container View that needs to be invalidated in order to load the view after the AsyncTask is done
    * @param context
    */
   public UrlImageGetter(View container, Context context) {
      this.context = context;
      this.container = container;
   }

   public Drawable getDrawable(String source) {
      UrlDrawable urlDrawable = new UrlDrawable();

      // get the actual source
      ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(urlDrawable);

      asyncTask.execute(source);

      // return reference to URLDrawable where I will change with actual image from
      // the src tag
      return urlDrawable;
   }

   public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
      UrlDrawable urlDrawable;

      public ImageGetterAsyncTask(UrlDrawable d) {
         this.urlDrawable = d;
      }

      @Override
      protected Drawable doInBackground(String... params) {
         String source = params[0];
         return fetchDrawable(source);
      }

      @Override
      protected void onPostExecute(Drawable result) {
         int width = result.getIntrinsicWidth();
         int height = (int) ((width * (3f/4f)) - 0.5f);

         result.setBounds(0, 0, width, height);

         // change the reference of the current drawable to the result
         // from the HTTP call
         urlDrawable.drawable = result;

         // redraw the image by invalidating the container
         UrlImageGetter.this.container.invalidate();

         // For ICS
         ((TextView)UrlImageGetter.this.container).setHeight(UrlImageGetter.this.container.getHeight() + height);

         // Pre ICS
         ((TextView)UrlImageGetter.this.container).setEllipsize(null);
      }

      /**
       * Get the Drawable from URL
       *
       * @param source Url of image to download
       * @return drawable image from the Url
       */
      public Drawable fetchDrawable(String source) {
         try {
            InputStream is = null;

            try {
               is = fetch(source);
               Drawable drawable = new BitmapDrawable(
                UrlImageGetter.this.context.getResources(),
                BitmapFactory.decodeStream(is));

               return drawable;
            } finally {
               if (is != null) is.close();
            }
         } catch (Exception e) {
            return null;
         }
      }

      private InputStream fetch(String source) throws IOException {
         // TODO: This can be simplified by using HttpRequest.
         OkHttpClient client = Utils.createOkHttpClient();
         HttpURLConnection connection = client.open(new URL(source));
         return connection.getInputStream();
      }
   }
}
