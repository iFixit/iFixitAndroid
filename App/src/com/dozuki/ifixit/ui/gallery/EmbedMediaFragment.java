package com.dozuki.ifixit.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.model.gallery.GalleryEmbedList;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;

public class EmbedMediaFragment extends MediaFragment {

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
   public void onUserVideos(APIEvent.UserEmbeds event) {
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
         // TODO
      }
   }
   @Override
   protected void retrieveUserMedia() {
      mNextPageRequestInProgress = true;
      APIService.call(getActivity(),
         APIService.getUserEmbedsAPICall("?limit=" + (IMAGE_PAGE_SIZE)));
   }
}
