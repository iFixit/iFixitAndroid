package com.dozuki.ifixit.ui.gallery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.gallery.GalleryImage;
import com.dozuki.ifixit.model.gallery.GalleryMediaList;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.guide.view.FullImageViewActivity;
import com.dozuki.ifixit.util.CaptureHelper;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public abstract class MediaFragment extends BaseFragment
 implements OnItemClickListener, OnItemLongClickListener {

   private static final String GOOGLE_PHOTOS_URI = "content://com.google.android.apps.photos.contentprovider";

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

      mMode = null;

      mGalleryAdapter = new MediaAdapter();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.gallery_view, container, false);

      String[] permissions;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
         permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
      } else {
         permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
      }

      if (ContextCompat.checkSelfPermission(getActivity(),
       Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
       ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
         ActivityCompat.requestPermissions(getActivity(),
          permissions, CaptureHelper.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
      }

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
      if (resultCode == Activity.RESULT_OK) {
         if (requestCode == SELECT_PICTURE && data != null) {

            ArrayList<Uri> selectedImages = new ArrayList<>();

            if (this.hasClipData(data)) {
               selectedImages = this.getUrisFromClipData(data.getClipData());
            } else {
               selectedImages.add(data.getData());
            }

            for (int i = 0; i < selectedImages.size(); i++) {
               Uri selectedImage = selectedImages.get(i);

               // check file type
               String path = getPath(selectedImage);

               if (selectedImage.toString().startsWith(GOOGLE_PHOTOS_URI) && (path == null || path.length() == 0)) {
                  // Try downloading the image from google photos if it's there.
                  Uri uri = this.downloadFileFromExternalSource(selectedImage);

                  path = uri.getPath();
               }

               if (!this.isImageValid(path)) {
                  return;
               }

               String key = mGalleryAdapter.addFile(path);
               Api.call(getActivity(), ApiCall.uploadImage(path, key));
            }

         } else if (requestCode == CaptureHelper.CAMERA_REQUEST_CODE) {
            if (mCameraTempFileName == null) {
               Log.e("iFixit", "Error cameraTempFile is null!");
               return;
            }

            String key = mGalleryAdapter.addFile(mCameraTempFileName);
            Api.call(getActivity(), ApiCall.uploadImage(mCameraTempFileName, key));
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
         MediaViewItem itemView = (MediaViewItem) view;
         String url = (String) view.getTag();

         if (url == null || (url.equals("") || url.indexOf(".") == 0) || itemView.getImage().isLocal()) {
            return;
         }

         Intent selectResult = new Intent();
         selectResult.putExtra(GalleryActivity.MEDIA_RETURN_KEY, mMediaList.get(position));
         getActivity().setResult(Activity.RESULT_OK, selectResult);
         getActivity().finish();
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

         startActivity(FullImageViewActivity.viewImage(getActivity(), url, false));
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
      if (Build.VERSION.SDK_INT <= 19) {
         Intent intent = new Intent();
         intent.setType("image/*");
         intent.setAction(Intent.ACTION_GET_CONTENT);
         startActivityForResult(Intent.createChooser(intent,
          getString(R.string.image_chooser_title)), SELECT_PICTURE);
      } else if (Build.VERSION.SDK_INT > 19) {
         Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
         intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
         startActivityForResult(intent, SELECT_PICTURE);
      }
   }

   protected void launchCamera() {
      File file;
      try {
         file = CaptureHelper.createImageFile(getActivity());
         mCameraTempFileName = file.getAbsolutePath();
         Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         // Ensure that there's a camera activity to handle the intent
         if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {

            // Continue only if the File was successfully created
            if (file != null) {
               Context context = getContext();
               Uri photoURI = FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider",
                file);
               takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
               startActivityForResult(takePictureIntent, CaptureHelper.CAMERA_REQUEST_CODE);
            }
         }
      } catch (IOException e) {
         Log.e("MediaFragment", "Launch camera", e);
         Toast.makeText(getActivity(), "We had a problem launching your camera.", Toast.LENGTH_SHORT).show();
      }
   }

   private boolean hasClipData(Intent data) {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && data.getClipData() != null;
   }

   private ArrayList<Uri> getUrisFromClipData(ClipData data) {
      ArrayList<Uri> result = new ArrayList<>();

      for (int i = 0; i < data.getItemCount(); i++) {
         ClipData.Item item = data.getItemAt(i);

         Uri uri = item.getUri();

         result.add(uri);
      }

      return result;
   }

   private boolean isImageValid(String path) {
      boolean result = true;
      if (path == null) {
         Toast.makeText(getActivity(), getString(R.string.non_image_error),
          Toast.LENGTH_LONG).show();
         result = false;
      }

      File file = new File(path);
      if (file.length() == 0) {
         Toast.makeText(getActivity(), getString(R.string.empty_image_error),
          Toast.LENGTH_LONG).show();
         result = false;
      }

      if (mMediaList.countUploadingImages() >= MAX_UPLOAD_COUNT) {
         Toast.makeText(getActivity(), getString(R.string.too_many_image_error),
          Toast.LENGTH_LONG).show();
         result = false;
      }

      return result;
   }

   private Uri downloadFileFromExternalSource(Uri source) {
      Uri result = null;
      FileOutputStream fos = null;

      try {
         InputStream is = getActivity().getContentResolver().openInputStream(source);
         if (is != null) {
            File f = CaptureHelper.createImageFile(getActivity());

            Bitmap bmp = BitmapFactory.decodeStream(is);
            fos = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            result = Uri.fromFile(f);
         }
      } catch (FileNotFoundException e) {
         Toast.makeText(getActivity(), R.string.google_photos_download_error,
          Toast.LENGTH_LONG).show();

         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         try {
            if (fos != null) {
               fos.close();
            }
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      return result;
   }

   private String getPath(Uri uri) {
      String[] projection = {MediaStore.MediaColumns.DATA};

      Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);

      String path = "";

      try {
         if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(column_index);
         } else {
            return null;
         }
      } finally {
         if (cursor != null) {
            cursor.close();
         }
      }

      return path;
   }

   private void deleteSelectedPhotos() {
      ArrayList<Integer> deleteList = new ArrayList<Integer>();

      for (int i = mMediaList.size() - 1; i >= 0; i--) {
         if (mMediaList.get(i).isSelected()) {

            if (mMediaList.get(i).isLocal()) {
               Toast.makeText(getActivity(), getString(R.string.delete_loading_image_error),
                Toast.LENGTH_LONG).show();
            } else {
               deleteList.add(mMediaList.get(i).getId());
               mMediaList.remove(i);
            }
         }
      }

      Api.call(getActivity(), ApiCall.deleteImage(deleteList));

      mMode.finish();
   }

   private void setDeleteMode() {
      if (mMode == null) {
         Animation animHide = AnimationUtils.loadAnimation(getActivity(),
          R.anim.slide_out_bottom_slow);
         animHide.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {}

            @Override
            public void onAnimationRepeat(Animation arg0) {}

            @Override
            public void onAnimationStart(Animation arg0) {}
         });
         mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ModeCallback());
      }
   }

   private AlertDialog createDeleteConfirmDialog() {
      mShowingDelete = true;

      int selectedCount = mMediaList.countSelected();

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
            itemView = new MediaViewItem(getActivity());
         } else {
            itemView = (MediaViewItem) convertView;
            itemView.clearImage();
         }

         GalleryImage image = (GalleryImage) getItem(position);
         itemView.setImage(image);
         itemView.setTag(image.getPath());
         itemView.setSelected(image.isSelected());

         return itemView;
      }
   }

   private class ModeCallback implements ActionMode.Callback {
      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
         // Create the menu from the xml file
         getActivity().getMenuInflater().inflate(R.menu.contextual_delete, menu);
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
