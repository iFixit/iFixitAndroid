package com.dozuki.ifixit.ui.gallery;

import com.dozuki.ifixit.model.gallery.GalleryEmbedList;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.dozuki.ifixit.util.api.Api;
import com.squareup.otto.Subscribe;

public class EmbedMediaFragment extends MediaFragment {
   @Subscribe
   public void onUserVideos(ApiEvent.UserEmbeds event) {
      if (!event.hasError()) {
         GalleryEmbedList videoList = event.getResult();
         if (videoList.getItems().size() > 0) {
            int oldImageSize = mMediaList.getItems().size();
            for (int i = 0; i < videoList.getItems().size(); i++) {
               videoList.getItems().get(i).setSelected(false);
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
      Api.call(getActivity(),
       Api.getUserEmbedsAPICall("?limit=" + IMAGE_PAGE_SIZE));
   }
}
