package com.dozuki.ifixit.ui.gallery;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.model.gallery.UploadedImageInfo;
import com.dozuki.ifixit.model.gallery.UserImageList;
import com.dozuki.ifixit.model.login.LoginEvent;
import com.dozuki.ifixit.ui.login.LocalImage;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;

import java.util.ArrayList;

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

   @Override
   protected void retrieveUserMedia() {
      mNextPageRequestInProgress = true;
      ((GalleryActivity)getActivity()).showLoading();

      APIService.call((Activity) getActivity(),
       APIService.getUserImagesAPICall("?limit=" + (IMAGE_PAGE_SIZE) + "&offset=" + mItemsDownloaded));
   }

   @Subscribe
   public void onUserImages(APIEvent.UserImages event) {
      ((GalleryActivity)getActivity()).hideLoading();

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
         } else {
            mLastPage = true;
         }
         mNextPageRequestInProgress = false;
      } else {
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

}
