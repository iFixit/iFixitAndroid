package com.dozuki.ifixit.gallery.ui;

import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;

import com.dozuki.ifixit.gallery.model.UploadedImageInfo;
import com.dozuki.ifixit.gallery.model.UserImageList;
import com.dozuki.ifixit.login.model.LoginEvent;
import com.dozuki.ifixit.login.ui.LocalImage;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class PhotoMediaFragment extends MediaFragment {

   static final String FILTERED_MEDIA = "FILTERED_MEDIA";
   ArrayList<String> mFilteredMedia = new ArrayList<String>();

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // mFilteredMedia =
      Bundle b = getArguments();
      if (b != null) {
         mFilteredMedia = (ArrayList<String>) b.getStringArrayList(FILTERED_MEDIA);
      }
      return super.onCreateView(inflater, container, savedInstanceState);
   }

   @Subscribe
   public void onUserImages(APIEvent.UserImages event) {
      if (!event.hasError()) {
         UserImageList imageList = event.getResult();
         if (imageList.getItems().size() > 0) {
            int oldImageSize = mMediaList.getItems().size();
            for (int i = 0; i < imageList.getItems().size(); i++) {
               if (mFilteredMedia != null && mFilteredMedia.contains(imageList.getItems().get(i).getItemId())) {
                  continue;
               } else {
                  mSelectedList.add(false);
                  mMediaList.addItem(imageList.getItems().get(i));
               }
            }
            mItemsDownloaded += (mMediaList.getItems().size() - oldImageSize);
            mGalleryAdapter.invalidatedView();
            mLastPage = false;
            updateNoImagesText();
         } else {
            mLastPage = true;
         }
         mNextPageRequestInProgress = false;
      } else {
         // TODO
         APIService.getErrorDialog(getActivity(), event.getError(), APIService.getUserImagesAPICall("?limit=" + (IMAGE_PAGE_SIZE) + "&offset=" + mItemsDownloaded));
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
         mItemsDownloaded++;
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

   @Override
   protected void retrieveUserMedia() {
      mNextPageRequestInProgress = true;
      APIService.call((Activity) getActivity(),
         APIService.getUserImagesAPICall("?limit=" + (IMAGE_PAGE_SIZE) + "&offset=" + mItemsDownloaded));
   }
}
