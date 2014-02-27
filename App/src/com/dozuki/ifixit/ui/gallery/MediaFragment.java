package com.dozuki.ifixit.ui.gallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.gallery.GalleryImage;
import com.dozuki.ifixit.model.gallery.GalleryMediaList;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.guide.view.FullImageViewActivity;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.CaptureHelper;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.api.ApiCall;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public abstract class MediaFragment extends BaseFragment
 implements OnItemClickListener, OnItemLongClickListener {

   protected static final int IMAGE_PAGE_SIZE = 1000;
   private static final String CAMERA_PATH = "CAMERA_PATH";
   private static final int SELECT_PICTURE = 1;
   private static final int CAMERA_PIC_REQUEST = 2;
   private static final String GALLERY_MEDIA_LIST = "GALLERY_MEDIA_LIST";
   private static final String IMAGES_DOWNLOADED = "IMAGES_DOWNLOADED";
   private static final String HASH_MAP = "HASH_MAP";
   private static final String SHOWING_DELETE_KEY = "SHOWING_DELETE_KEY";
   private static final int MAX_UPLOAD_COUNT = 4;
   private static final String RETURNING_VAL = "RETURNING_VAL";
   private static final String DELETE_MODE = "DELETE_MODE";

   protected MediaAdapter mGalleryAdapter;
   protected GalleryMediaList mMediaList;
   protected boolean mNextPageRequestInProgress;
   protected ArrayList<Image> mAlreadyAttachedImages;
   private GridView mGridView;
   private ImageSizes mImageSizes;
   private ActionMode mMode;
   private String mCameraTempFileName;
   private boolean mShowingDelete = false;
   private boolean mSelectForReturn;
   private TextView mNoMediaView;

   protected abstract void retrieveUserMedia();

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setHasOptionsMenu(true);

      mImageSizes = App.get().getImageSizes();
      mMode = null;

      mGalleryAdapter = new MediaAdapter();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.gallery_view, container, false);

      if (savedInstanceState != null) {
         mShowingDelete = savedInstanceState.getBoolean(SHOWING_DELETE_KEY);

         mMediaList = (GalleryMediaList) savedInstanceState.getSerializable(GALLERY_MEDIA_LIST);

         mSelectForReturn = savedInstanceState.getBoolean(RETURNING_VAL);

         if (mShowingDelete) {
            createDeleteConfirmDialog().show();
         }

         if (savedInstanceState.getString(CAMERA_PATH) != null) {
            mCameraTempFileName = savedInstanceState.getString(CAMERA_PATH);
         }
      } else {
         mMediaList = new GalleryMediaList();
      }

      if (mMediaList.size() == 0 && !mNextPageRequestInProgress) {
         retrieveUserMedia();
      }

      mGridView = (GridView) view.findViewById(R.id.gridview);
      mNoMediaView = (TextView) view.findViewById(R.id.no_images_text);

      mGridView.setAdapter(mGalleryAdapter);
      mGridView.setOnItemClickListener(this);
      mGridView.setOnItemLongClickListener(this);

      if (savedInstanceState != null) {
         if (savedInstanceState.getBoolean(DELETE_MODE)) {
            setDeleteMode();
         }
      }

      return view;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.top_camera_button:
            launchCamera();
            return true;
         case R.id.top_gallery_button:
            launchImageChooser();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      if (resultCode == Activity.RESULT_OK) {
         if (requestCode == SELECT_PICTURE) {
            Uri selectedImageUri = data.getData();

            // check file type
            String path = getPath(selectedImageUri);
            if (path == null || !(path.toLowerCase().contains(".jpeg") ||
             path.toLowerCase().contains(".jpg") || path.toLowerCase().contains(".png"))) {
               Toast.makeText(getActivity(), getString(R.string.non_image_error),
                Toast.LENGTH_LONG).show();

               return;
            }
            
            File file = new File(path);

            if (file.length() == 0) {
               Toast.makeText(getActivity(), getString(R.string.empty_image_error),
                Toast.LENGTH_LONG).show();
               return;
            }

            if (mMediaList.countUploadingImages() >= MAX_UPLOAD_COUNT) {
               Toast.makeText(getActivity(), getString(R.string.too_many_image_error),
                Toast.LENGTH_LONG).show();
               return;
            }

            String key = mGalleryAdapter.addUri(selectedImageUri);
            Api.call(getSherlockActivity(), ApiCall.uploadImage(path, key));
         } else if (requestCode == CAMERA_PIC_REQUEST) {
            if (mCameraTempFileName == null) {
               Log.e("iFixit", "Error cameraTempFile is null!");
               return;
            }

            String key = mGalleryAdapter.addFile(mCameraTempFileName);
            Api.call(getSherlockActivity(), ApiCall.uploadImage(
             mCameraTempFileName, key));
         }
      }
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putSerializable(GALLERY_MEDIA_LIST, mMediaList);
      savedInstanceState.putBoolean(SHOWING_DELETE_KEY, mShowingDelete);
      savedInstanceState.putBoolean(RETURNING_VAL, mSelectForReturn);
      savedInstanceState.putBoolean(DELETE_MODE, mMode != null);

      if (mCameraTempFileName != null) {
         savedInstanceState.putString(CAMERA_PATH, mCameraTempFileName);
      }
   }

   @Override
   public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
      if (!mSelectForReturn)
         setDeleteMode();

      return false;
   }

   public void onItemClick(AdapterView<?> adapterView, View view, int position,
    long id) {
      MediaViewItem cell = (MediaViewItem) view;
      if (mSelectForReturn) {
         String url = (String) view.getTag();

         if (url == null || (url.equals("") || url.indexOf(".") == 0)) {
            return;
         }

         Intent selectResult = new Intent();
         selectResult.putExtra(GalleryActivity.MEDIA_RETURN_KEY, mMediaList.get(position));
         getSherlockActivity().setResult(Activity.RESULT_OK, selectResult);
         getSherlockActivity().finish();
      } else if (mMode != null) {
         if (cell == null) {
            Log.i("iFixit", "Delete cell null!");
            return;
         }
         mMediaList.get(position).toggleSelected();
         view.invalidate();
         mGalleryAdapter.invalidatedView();
      } else {
         String url = (String) view.getTag();

         if (url == null || (url.equals("") || url.indexOf(".") == 0)) {
            return;
         }

         Intent intent = new Intent(getActivity(), FullImageViewActivity.class);
         intent.putExtra(FullImageViewActivity.IMAGE_URL, url);
         startActivity(intent);
      }
   }

   public void setForReturn(boolean returnItem) {
      mSelectForReturn = returnItem;
   }

   public void setAlreadyAttachedImages(ArrayList<Image> images) {
      mAlreadyAttachedImages = new ArrayList<Image>(images);
   }

   protected void setEmptyListView() {
      mGridView.setEmptyView(mNoMediaView);
   }

   protected void launchImageChooser() {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent,
       getString(R.string.image_chooser_title)), SELECT_PICTURE);
   }

   protected void launchCamera() {
      Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
      File file;
      try {
         file = createImageFile();
         cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
          Uri.fromFile(file));
         startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
      } catch (IOException e) {
         Log.e("MediaFragment", "Launch camera", e);
         Toast.makeText(getActivity(), "Please insert an SD card.", Toast.LENGTH_SHORT).show();
      }
   }

   private File createImageFile() throws IOException {
      // Create an image file name
      String imageFileName = CaptureHelper.getFileName();
      File image = File.createTempFile(imageFileName, ".jpg", CaptureHelper.getAlbumDir());

      mCameraTempFileName = image.getAbsolutePath();
      return image;
   }

   private String getPath(Uri uri) {
      String[] projection = {MediaStore.Images.Media.DATA};
      Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
      if (cursor.moveToFirst()) {
         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         return cursor.getString(column_index);
      } else {
         return null;
      }
   }

   private void deleteSelectedPhotos() {
      ArrayList<Integer> deleteList = new ArrayList<Integer>();

      for (int i = mMediaList.size() - 1; i >= 0; i--) {
         if (mMediaList.get(i).isSelected()) {

            if (mMediaList.get(i).isLocal()) {
               Toast.makeText(getSherlockActivity(), getString(R.string.delete_loading_image_error),
                Toast.LENGTH_LONG).show();
            } else {
               deleteList.add(mMediaList.get(i).getId());
               mMediaList.remove(i);
            }
         }
      }

      Api.call(getSherlockActivity(), ApiCall.deleteImage(deleteList));

      mMode.finish();
   }

   private void setDeleteMode() {
      if (mMode == null) {
         Animation animHide = AnimationUtils.loadAnimation(getSherlockActivity(),
          R.anim.slide_out_bottom_slow);
         animHide.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {}

            @Override
            public void onAnimationRepeat(Animation arg0) {}

            @Override
            public void onAnimationStart(Animation arg0) {}
         });
         mMode = getSherlockActivity().startActionMode(new ModeCallback());
      }
   }

   private AlertDialog createDeleteConfirmDialog() {
      mShowingDelete = true;

      int selectedCount = mMediaList.countSelected();

      AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
      builder
       .setTitle(getString(R.string.confirm_delete_title))
       .setMessage(getString(R.string.media_delete_body, selectedCount,
        selectedCount > 1 ? getString(R.string.images) : getString(R.string.image)))
       .setPositiveButton(getString(R.string.yes),
        new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int id) {
              mShowingDelete = false;
              deleteSelectedPhotos();
              dialog.cancel();
           }
        })
       .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
             mShowingDelete = false;
             dialog.cancel();
          }
       });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            mShowingDelete = false;
         }
      });

      return dialog;
   }

   class MediaAdapter extends BaseAdapter {
      public String addUri(Uri uri) {
         return addFile(uri.toString());
      }

      public String addFile(String path) {
         GalleryImage image = new GalleryImage();
         image.setLocalImage(path);
         mMediaList.addItem(0, image);

         notifyDataSetChanged();
         invalidatedView();
         return path;
      }

      public void invalidatedView() {
         mGridView.invalidateViews();
      }

      @Override
      public int getCount() {
         return mMediaList.getItems().size();
      }

      @Override
      public Object getItem(int position) {
         return mMediaList.get(position);
      }

      @Override
      public long getItemId(int id) {
         return id;
      }

      public View getView(int position, View convertView, ViewGroup parent) {
         MediaViewItem itemView;

         if (convertView == null) {
            itemView = new MediaViewItem(getSherlockActivity());
         } else {
            itemView = (MediaViewItem) convertView;
         }

         GalleryImage image = (GalleryImage) getItem(position);

         itemView.clearImage();

         // image was added from the local gallery or captured on the phone
         if (image.isLocal()) {
            Uri temp = Uri.parse(image.getPath());
            image.setLocalImage(temp.toString());

            if (image.fromMediaStore()) {
               // Media Store image
               itemView.setImageItem(temp.toString());
            } else {
               // image was added locally from camera
               itemView.setImageItem(new File(temp.toString()));
            }

         } else {
            itemView.setImageItem(image.getPath(mImageSizes.getThumb()));
         }

         itemView.setTag(image.getPath());
         itemView.setSelected(image.isSelected());

         return itemView;
      }
   }

   private final class ModeCallback implements ActionMode.Callback {
      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
         // Create the menu from the xml file
         getSherlockActivity().getSupportMenuInflater().inflate(R.menu.contextual_delete, menu);
         return true;
      }

      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
         return false;
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
         if (!mMediaList.hasSelected()) {
            mode.finish();
            return true;
         }

         createDeleteConfirmDialog().show();

         return true;
      }

      @Override
      public void onDestroyActionMode(ActionMode mode) {
         if (mode == mMode) {
            mMode = null;
         }

         mMediaList.clearSelected();
         mGalleryAdapter.invalidatedView();
      }
   }
}
