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
import com.squareup.otto.Subscribe;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

public class EmbedMediaFragment extends MediaFragment {
   
   
//   @Override
//   public void onCreate(Bundle savedInstanceState) {
//      super.onCreate(savedInstanceState);
//   }
// 
//   @Override
//   public View onCreateView(LayoutInflater inflater, ViewGroup container,
//    Bundle savedInstanceState) {
//      return super.onCreateView(inflater, container, savedInstanceState);
//   }
//   
//   
//   @Subscribe
//   public void onUserImages(APIEvent.UserImages event) {
//      if (!event.hasError()) {
//         UserImageList imageList = event.getResult();
//         if (imageList.getImages().size() > 0) {
//            int oldImageSize = mImageList.getImages().size();
//            for (int i = 0; i < imageList.getImages().size(); i++) {
//               mSelectedList.add(false);
//               mImageList.addImage(imageList.getImages().get(i));
//            }
//            mImagesDownloaded += (mImageList.getImages().size() - oldImageSize);
//            mGalleryAdapter.invalidatedView();
//            mLastPage = false;
//            updateNoImagesText();
//         } else {
//            mLastPage = true;
//         }
//         mNextPageRequestInProgress = false;
//      } else {
//         // TODO
//      }
//   }
//
//   @Subscribe
//   public void onUploadImage(APIEvent.UploadImage event) {
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
//
//   @Subscribe
//   public void onDeleteImage(APIEvent.DeleteImage event) {
//      if (!event.hasError()) {
//         // TODO
//      } else {
//         // TODO
//      }
//   }
//   
//   
//   @Subscribe
//   public void onLogin(LoginEvent.Login event) {
//      setupUser(event.getUser());
//   }
}