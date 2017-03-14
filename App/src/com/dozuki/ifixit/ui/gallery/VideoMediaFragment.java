package com.dozuki.ifixit.ui.gallery;

import com.dozuki.ifixit.model.gallery.GalleryVideoList;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.squareup.otto.Subscribe;

public class VideoMediaFragment extends MediaFragment {
   @Subscribe
   public void onUserVideos(ApiEvent.UserVideos event) {
      if (!event.hasError()) {
         GalleryVideoList videoList = event.getResult();
         if (videoList.getItems().size() > 0) {
            for (int i = 0; i < videoList.getItems().size(); i++) {
               mMediaList.addItem(videoList.getItems().get(i));
            }
            mGalleryAdapter.invalidatedView();
         }
         mNextPageRequestInProgress = false;
      } else {
         Api.getErrorDialog(getActivity(), event).show();
      }
   }

   @Override
   protected void retrieveUserMedia() {
      mNextPageRequestInProgress = true;
      Api.call(getActivity(), ApiCall.userVideos("?limit=" + IMAGE_PAGE_SIZE));
   }
}
