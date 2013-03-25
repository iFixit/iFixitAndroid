package com.dozuki.ifixit.gallery.ui;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.model.UserVideoList;
import com.dozuki.ifixit.login.model.LoginEvent;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;



public class VideoMediaFragment extends MediaFragment {
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }
 
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      
      View v = super.onCreateView(inflater, container, savedInstanceState);
         mNoMediaText.setText(R.string.no_videos_text);
      return v;
   }
   
   
   @Subscribe
   public void onUserVideos(APIEvent.UserVideos event) {
      if (!event.hasError()) {
         UserVideoList videoList = event.getResult();
         if (videoList.getItems().size() > 0) {
            int oldImageSize = mMediaList.getItems().size();
            for (int i = 0; i < videoList.getItems().size(); i++) {
               mSelectedList.add(false);
               mMediaList.addItem(videoList.getItems().get(i));
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
      }
   }

//   @Subscribe
//   public void onUploadVideo(APIEvent.UploadVideo event) {
//      if (!event.hasError()) {
//         UploadedImageInfo imageinfo = event.getResult();
//         String url = event.getExtraInfo();
//
//         LocalImage cur = mLocalURL.get(url);
//         if (cur == null)
//            return;
//         cur.mImgid = imageinfo.getImageid();
//         mLocalURL.put(url, cur);
//         mImagesDownloaded++;
//         mGalleryAdapter.invalidatedView();
//      } else {
//         // TODO
//      }
//   }

//   @Subscribe
//   public void onDeleteVideo(APIEvent.DeleteVideo event) {
//      if (!event.hasError()) {
//         // TODO
//      } else {
//         // TODO
//      }
//   }
   
   @Subscribe
   public void onLogin(LoginEvent.Login event) {
      setupUser(event.getUser());
   }
   
   @Override
   protected void retrieveUserMedia() {
      mNextPageRequestInProgress = true;
      APIService.call((Activity) getActivity(),
      APIService.getUserVideosAPICall("?limit=" + (IMAGE_PAGE_SIZE) + "&offset=" + mItemsDownloaded));
   }
   
}