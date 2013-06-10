package com.dozuki.ifixit.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.model.gallery.UserVideoList;
import com.dozuki.ifixit.model.login.LoginEvent;
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
      APIService.call(getActivity(),
      APIService.getUserVideosAPICall("?limit=" + (IMAGE_PAGE_SIZE) + "&offset=" + mItemsDownloaded));
   }
   
}
