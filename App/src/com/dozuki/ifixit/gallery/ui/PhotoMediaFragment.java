package com.dozuki.ifixit.gallery.ui;

import org.holoeverywhere.LayoutInflater;

import com.dozuki.ifixit.gallery.model.UploadedImageInfo;
import com.dozuki.ifixit.gallery.model.UserImageList;
import com.dozuki.ifixit.login.model.LoginEvent;
import com.dozuki.ifixit.login.ui.LocalImage;
import com.dozuki.ifixit.util.APIEvent;
import com.squareup.otto.Subscribe;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;



public class PhotoMediaFragment extends MediaFragment {
   
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }
 
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      return super.onCreateView(inflater, container, savedInstanceState);
   }
   
   
   @Subscribe
   public void onUserImages(APIEvent.UserImages event) {
      if (!event.hasError()) {
         UserImageList imageList = event.getResult();
         if (imageList.getItems().size() > 0) {
            int oldImageSize = mImageList.getItems().size();
            for (int i = 0; i < imageList.getItems().size(); i++) {
               mSelectedList.add(false);
               mImageList.addItem(imageList.getItems().get(i));
            }
            mImagesDownloaded += (mImageList.getItems().size() - oldImageSize);
            mGalleryAdapter.invalidatedView();
            mLastPage = false;
            updateNoImagesText();
         } else {
            mLastPage = true;
         }
         mNextPageRequestInProgress = false;
      } else {
         // TODO
      }
   }

   @Subscribe
   public void onUploadImage(APIEvent.UploadImage event) {
      if (!event.hasError()) {
         UploadedImageInfo imageinfo = event.getResult();
         String url = event.getExtraInfo();

         LocalImage cur = mLocalURL.get(url);
         if (cur == null)
            return;
         cur.mImgid = imageinfo.getImageid();
         mLocalURL.put(url, cur);
         mImagesDownloaded++;
         mGalleryAdapter.invalidatedView();
      } else {
         // TODO
      }
   }

   @Subscribe
   public void onDeleteImage(APIEvent.DeleteImage event) {
      if (!event.hasError()) {
         // TODO
      } else {
         // TODO
      }
   }
   
   
   @Subscribe
   public void onLogin(LoginEvent.Login event) {
      setupUser(event.getUser());
   }
}
