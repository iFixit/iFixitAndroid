package com.dozuki.ifixit.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.model.gallery.GalleryVideoList;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;

public class VideoMediaFragment extends MediaFragment {
   @Subscribe
   public void onUserVideos(APIEvent.UserVideos event) {
      if (!event.hasError()) {
         GalleryVideoList videoList = event.getResult();
         if (videoList.getItems().size() > 0) {
            int oldImageSize = mMediaList.getItems().size();
            for (int i = 0; i < videoList.getItems().size(); i++) {
               mMediaList.addItem(videoList.getItems().get(i));
            }
            mGalleryAdapter.invalidatedView();
         }
         mNextPageRequestInProgress = false;
      } else {
         APIService.getErrorDialog(getActivity(), event.getError(),
          APIService.getUserVideosAPICall("?limit=" + IMAGE_PAGE_SIZE)).show();
      }
   }

   @Subscribe
   public void onLogin(LoginEvent.Login event) {
      setupUser(event.getUser());
   }

   @Override
   protected void retrieveUserMedia() {
      mNextPageRequestInProgress = true;
      APIService.call(getActivity(),
       APIService.getUserVideosAPICall("?limit=" + IMAGE_PAGE_SIZE));
   }
}
