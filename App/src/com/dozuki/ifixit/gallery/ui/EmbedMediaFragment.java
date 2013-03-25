package com.dozuki.ifixit.gallery.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.model.UploadedImageInfo;
import com.dozuki.ifixit.gallery.model.UserEmbedList;
import com.dozuki.ifixit.gallery.model.UserImageList;
import com.dozuki.ifixit.gallery.model.UserVideoList;
import com.dozuki.ifixit.login.model.LoginEvent;
import com.dozuki.ifixit.login.ui.LocalImage;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.squareup.otto.Subscribe;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

public class EmbedMediaFragment extends MediaFragment {
   
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }
 
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      
      View v = super.onCreateView(inflater, container, savedInstanceState);
         mNoMediaText.setText(R.string.no_embeds_text);
      return v;
   }
   
   
   @Subscribe
   public void onUserVideos(APIEvent.UserEmbeds event) {
      if (!event.hasError()) {
         UserEmbedList videoList = event.getResult();
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
   @Override
   protected void retrieveUserMedia() {
      mNextPageRequestInProgress = true;
      APIService.call((Activity) getActivity(),
         APIService.getUserEmbedsAPICall("?limit=" + (IMAGE_PAGE_SIZE) + "&offset=" + mItemsDownloaded));
   }
}