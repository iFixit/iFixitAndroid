package com.dozuki.ifixit.gallery.ui;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.model.UploadedImageInfo;
import com.dozuki.ifixit.gallery.model.UserImageInfo;
import com.dozuki.ifixit.gallery.model.UserImageList;
import com.dozuki.ifixit.guide_view.ui.FullImageViewActivity;
import com.dozuki.ifixit.login.model.LoginEvent;
import com.dozuki.ifixit.login.model.User;
import com.dozuki.ifixit.login.ui.LocalImage;
import com.dozuki.ifixit.login.ui.LogoutDialog;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.IfixitActivity;
import com.dozuki.ifixit.util.ImageSizes;
import com.ifixit.android.imagemanager.ImageManager;
import com.squareup.otto.Subscribe;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GalleryActivity extends IfixitActivity implements
 OnItemClickListener, OnClickListener, OnItemLongClickListener {
   private static final int MAX_LOADING_IMAGES = 15;
   private static final int MAX_STORED_IMAGES = 20;
   private static final int MAX_WRITING_IMAGES = 15;
   private static final int IMAGE_PAGE_SIZE = 40;
   private static final String CAMERA_PATH = "CAMERA_PATH";
   private static final int SELECT_PICTURE = 1;
   private static final int CAMERA_PIC_REQUEST = 2;
   private static final String USER_IMAGE_LIST = "USER_IMAGE_LIST";
   private static final String USER_SELECTED_LIST = "USER_SELECTED_LIST";
   private static final String IMAGES_DOWNLOADED = "IMAGES_DOWNLOADED";
   private static final String IMAGE_PREFIX = "IFIXIT_GALLERY";
   private static final String HASH_MAP = "HASH_MAP";
   private static final String SHOWING_HELP = "SHOWING_HELP";
   private static final String SHOWING_DELETE = "SHOWING_DELETE";
   private static final int MAX_UPLOAD_COUNT = 4;

   private TextView mNoImagesText;
   private GridView mGridView;
   private RelativeLayout mButtons;
   private MediaAdapter mGalleryAdapter;
   private TextView mLoginText;
   private String mUserName;
   private ImageManager mImageManager;
   private ArrayList<Boolean> mSelectedList;
   private HashMap<String, LocalImage> mLocalURL;
   private HashMap<String, Bitmap> mLimages;
   private ImageSizes mImageSizes;
   private UserImageList mImageList;
   private ActionMode mMode;
   private int mImagesDownloaded;
   private boolean mLastPage;
   private String mCameraTempFileName;
   private boolean mNextPageRequestInProgress;
   private boolean mShowingHelp;
   private boolean mShowingDelete;
   private Intent mCurIntent;
   private ActionBar mActionBar;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      mActionBar = getSupportActionBar();
      mActionBar.setTitle("");

      super.onCreate(savedInstanceState);

      setContentView(R.layout.gallery_view);

      mGridView = (GridView)findViewById(R.id.gridview);
      mNoImagesText = (TextView)findViewById(R.id.no_images_text);
      mButtons = (RelativeLayout)findViewById(R.id.button_holder);
      mLoginText = (TextView)findViewById(R.id.login_text);

      mActionBar.setDisplayHomeAsUpEnabled(true);

      if (mImageManager == null) {
         mImageManager = MainApplication.get().getImageManager();
         mImageManager.setMaxLoadingImages(MAX_LOADING_IMAGES);
         mImageManager.setMaxStoredImages(MAX_STORED_IMAGES);
         mImageManager.setMaxWritingImages(MAX_WRITING_IMAGES);
      }

      mImageSizes = MainApplication.get().getImageSizes();
      mMode = null;
      mShowingHelp = false;
      mShowingDelete = false;
      mLimages = new HashMap<String, Bitmap>();

      if (savedInstanceState != null) {
         mShowingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         if (mShowingHelp) {
            createHelpDialog().show();
         }

         mShowingDelete = savedInstanceState.getBoolean(SHOWING_DELETE);
         if (mShowingDelete) {
            createDeleteConfirmDialog().show();
         }

         mImagesDownloaded = savedInstanceState.getInt(IMAGES_DOWNLOADED);
         mImageList = (UserImageList)savedInstanceState.getSerializable(USER_IMAGE_LIST);

         mSelectedList = (ArrayList<Boolean>)savedInstanceState.getSerializable(USER_SELECTED_LIST);

         if (savedInstanceState.getString(CAMERA_PATH) != null) {
            mCameraTempFileName = savedInstanceState.getString(CAMERA_PATH);
         }

         mLocalURL = (HashMap<String, LocalImage>)savedInstanceState.getSerializable(HASH_MAP);
         for (LocalImage li : mLocalURL.values()) {
            if (li.mPath.contains(".jpg")) {
               mLimages.put(li.mPath, buildBitmap(li.mPath));
            }
         }
      } else {
         mImageList = new UserImageList();
         mSelectedList = new ArrayList<Boolean>();
         mLocalURL = new HashMap<String, LocalImage>();
      }

      mGalleryAdapter = new MediaAdapter();
      mGridView.setAdapter(mGalleryAdapter);
      mGridView.setOnScrollListener(new GalleryOnScrollListener());
      mGridView.setOnItemClickListener(this);
      mGridView.setOnItemLongClickListener(this);

      updateNoImagesText();

      if (mSelectedList.contains(true)) {
         setDeleteMode();
      }

      if (mImageList.getImages().size() == 0 && !mNextPageRequestInProgress) {
         retrieveUserImages();
      }

      if (MainApplication.get().isUserLoggedIn()) {
         setupUser(MainApplication.get().getUser());
      }
   }

   @Override
   public boolean showGalleryIcon() {
      return false;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            finish();
            return true;
         case R.id.top_camera_button:
            launchCamera();
            return true;
         case R.id.top_gallery_button:
            launchImageChooser();
            return true;
         case R.id.top_question_button:
            createHelpDialog().show();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Subscribe
   public void onLogin(LoginEvent.Login event) {
      if (MainApplication.get().isFirstTimeGalleryUser()) {
         createHelpDialog().show();
         MainApplication.get().setFirstTimeGalleryUser(false);
      }

      setupUser(event.getUser());
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      if (MainApplication.get().isUserLoggedIn()) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.gallery_menu, menu);
      }

      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putSerializable(USER_SELECTED_LIST, mSelectedList);
      savedInstanceState.putInt(IMAGES_DOWNLOADED, mImagesDownloaded);
      savedInstanceState.putSerializable(HASH_MAP, mLocalURL);
      savedInstanceState.putSerializable(USER_IMAGE_LIST, mImageList);
      savedInstanceState.putBoolean(SHOWING_HELP, mShowingHelp);
      savedInstanceState.putBoolean(SHOWING_DELETE, mShowingDelete);

      if (mCameraTempFileName != null) {
         savedInstanceState.putString(CAMERA_PATH, mCameraTempFileName);
      }
   }

   @Subscribe
   public void onUserImages(APIEvent.UserImages event) {
      if (!event.hasError()) {
         UserImageList imageList = event.getResult();
         if (imageList.getImages().size() > 0) {
            int oldImageSize = mImageList.getImages().size();
            for (int i = 0; i < imageList.getImages().size(); i++) {
               mSelectedList.add(false);
               mImageList.addImage(imageList.getImages().get(i));
            }
            mImagesDownloaded += (mImageList.getImages().size() - oldImageSize);
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
         mImagesDownloaded++;
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

   private void retrieveUserImages() {
      mNextPageRequestInProgress = true;
      mCurIntent = APIService.getUserImagesIntent(this,
       "?limit=" + (IMAGE_PAGE_SIZE) + "&offset=" + mImagesDownloaded);
      APIService.call(this, mCurIntent);
   }

   @Override
   public void onClick(View view) {
      switch (view.getId()) {
      case R.id.button_holder:
         LogoutDialog.create(this).show();
         break;
      }
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
         e.printStackTrace();
      }
   }

   private File createImageFile() throws IOException {
      // Create an image file name
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      String imageFileName = IMAGE_PREFIX + timeStamp + "_";
      File image = File.createTempFile(imageFileName, ".jpg", getAlbumDir());
      mCameraTempFileName = image.getAbsolutePath();
      return image;
   }

   private File getAlbumDir() {
      File storageDir = null;
      if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
         storageDir = new File(Environment.getExternalStoragePublicDirectory(
          Environment.DIRECTORY_PICTURES), "iFixitImages/");

         if (storageDir != null && !storageDir.mkdirs() && !storageDir.exists()) {
            Log.w("iFixit", "Failed to create directory iFixitImages");
            return null;
         }
      } else {
         Log.w("iFixit", "External storage is not mounted READ/WRITE.");
      }

      return storageDir;
   }

   private String getPath(Uri uri) {
      String[] projection = { MediaStore.Images.Media.DATA };
      Cursor cursor = managedQuery(uri, projection, null, null, null);
      if (cursor != null) {
         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
         return cursor.getString(column_index);
      } else {
         return null;
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
               Toast.makeText(this, this.getString(R.string.non_image_error),
                Toast.LENGTH_LONG).show();

               return;
            }

            // check how many images are being uploaded
            int imagesBeingUploaded = 0;
            for (String key : mLocalURL.keySet()) {
               if (mLocalURL.get(key).mImgid == null) {
                  imagesBeingUploaded++;
               }
            }

            if (imagesBeingUploaded >= MAX_UPLOAD_COUNT) {
               Toast.makeText(this, this.getString(R.string.too_many_image_error),
                Toast.LENGTH_LONG).show();
               return;
            }

            String key = mGalleryAdapter.addUri(selectedImageUri);
            mCurIntent = APIService.getUploadImageIntent(this,
             getPath(selectedImageUri), key);
            APIService.call(this, mCurIntent);
            updateNoImagesText();
         } else if (requestCode == CAMERA_PIC_REQUEST) {
            if (mCameraTempFileName == null) {
               Log.w("iFixit", "Error cameraTempFile is null!");
               return;
            }
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 2;
            opt.inDither = true;
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;

            String key = mGalleryAdapter.addFile(mCameraTempFileName);
            updateNoImagesText();
            mCurIntent = APIService.getUploadImageIntent(this, mCameraTempFileName, key);
            APIService.call(this, mCurIntent);
         }
      }
   }

   private class MediaAdapter extends BaseAdapter {
      @Override
      public long getItemId(int id) {
         return id;
      }

      public String addUri(Uri uri) {
         String key = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
         UserImageInfo userImageInfo = new UserImageInfo();
         String url = uri.toString();

         userImageInfo.setGuid(url);
         userImageInfo.setImageid(null);
         userImageInfo.setKey(key);
         mImageList.addImage(userImageInfo);
         mSelectedList.add(false);

         mLocalURL.put(key, new LocalImage(getPath(uri)));
         invalidatedView();
         return key;
      }

      public String addFile(String path) {
         String key = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
         UserImageInfo userImageInfo = new UserImageInfo();
         String url = path;
         userImageInfo.setGuid(path);
         userImageInfo.setImageid(null);
         userImageInfo.setKey(key);
         mImageList.addImage(userImageInfo);
         mSelectedList.add(false);

         mLocalURL.put(key, new LocalImage(path));
         mLimages.put(url, buildBitmap(url));
         invalidatedView();
         return key;
      }

      public void invalidatedView() {
         mGridView.invalidateViews();
      }

      @Override
      public int getCount() {
         return mImageList.getImages().size();
      }

      @Override
      public Object getItem(int arg0) {
         return null;
      }

      public View getView(int position, View convertView, ViewGroup parent) {
         MediaViewItem itemView = (MediaViewItem)convertView;

         if (convertView == null) {
            itemView = new MediaViewItem(GalleryActivity.this, mImageManager);
         }

         itemView.setLoading(false);

         if (mImageList != null) {
            UserImageInfo image = mImageList.getImages().get(position);

            // image was pulled from the server
            if (mImageList.getImages().get(position).getImageid() != null &&
             mImageList.getImages().get(position).getKey() == null) {
               String imageUrl = image.getGuid() + mImageSizes.getThumb();
               itemView.setImageItem(imageUrl, GalleryActivity.this, !image.getLoaded());
               itemView.mListRef = image;
               image.setLoaded(true);
               itemView.setTag(image.getGuid());
               // image was added locally
            } else {
               Uri temp = Uri.parse(image.getGuid());
               Bitmap bitmap;
               if (temp.toString().contains(".jpg")) {
                  // camera image
                  bitmap = mLimages.get(image.getGuid());
               } else {
                  // gallery image
                  bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                   getContentResolver(), ContentUris.parseId(temp),
                   MediaStore.Images.Thumbnails.MINI_KIND, null);
               }

               itemView.mImageview.setImageBitmap(bitmap);
               itemView.mListRef = image;
               if (image.getKey() != null) {
                  if (mLocalURL.get(image.getKey()).mImgid == null) {
                     // Has not received an imageID so is still uploading
                     itemView.setLoading(true);
                  } else {
                     image.setImageid(mLocalURL.get(image.getKey()).mImgid);
                     itemView.setLoading(false);
                  }
               }

               itemView.setTag(image.getKey());
            }
         }

         if (mSelectedList.get(position)) {
            itemView.mSelectImage.setVisibility(View.VISIBLE);
         } else {
            itemView.mSelectImage.setVisibility(View.INVISIBLE);
         }

         return itemView;
      }
   }

   private final class ModeCallback implements ActionMode.Callback {
      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
         // Create the menu from the xml file
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.contextual_delete, menu);
         return true;
      }

      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
         return false;
      }

      @Override
      public void onDestroyActionMode(ActionMode mode) {
         if (mode == mMode) {
            mMode = null;
         }

         for (int i = mSelectedList.size() - 1; i > -1; i--) {
            if (mSelectedList.get(i)) {
               mSelectedList.set(i, false);
            }
         }
         mGalleryAdapter.invalidatedView();
         mButtons.setVisibility(View.VISIBLE);
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
         if (!mSelectedList.contains(true)) {
            mode.finish();
            return true;
         }

         createDeleteConfirmDialog().show();

         return true;
      }
   };

   private void deleteSelectedPhotos() {
      String deleteQuery = "?";

      for (int i = mSelectedList.size() - 1; i >= 0; i--) {
         if (mSelectedList.get(i)) {
            mSelectedList.remove(i);
            deleteQuery += "imageids[]=" + mImageList.getImages().get(i).getImageid() + "&";

            if (mImageList.getImages().get(i).getImageid() == null) {
               Toast.makeText(this, this.getString(R.string.delete_loading_image_error),
                Toast.LENGTH_LONG).show();
            }
            mImageList.getImages().remove(i);
         }
      }

      if (deleteQuery.length() > 1) {
         deleteQuery = deleteQuery.substring(0, deleteQuery.length() - 1);
      }
      mCurIntent = APIService.getDeleteImageIntent(this, deleteQuery);
      APIService.call(this, mCurIntent);

      updateNoImagesText();

      mMode.finish();
   }

   private void setupUser(User user) {
      mUserName = user.getUsername();
      mLoginText.setText(getString(R.string.logged_in_as) + " " + mUserName);
      mButtons.setOnClickListener(this);
      mButtons.setVisibility(View.VISIBLE);
      updateNoImagesText();
   }

   private void updateNoImagesText() {
      if (mImageList.getImages().size() < 1 && MainApplication.get().isUserLoggedIn()) {
         mNoImagesText.setVisibility(View.VISIBLE);
      } else {
         mNoImagesText.setVisibility(View.GONE);
      }
   }

   @Override
   public boolean onItemLongClick(AdapterView<?> parent, View view,
    int position, long id) {
      setDeleteMode();
      return false;
   }

   private void setDeleteMode() {
      if (mMode == null) {
         Animation animHide = AnimationUtils.loadAnimation(this,
          R.anim.slide_out_bottom_slow);
         animHide.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
               mButtons.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
         });
         mButtons.startAnimation(animHide);
         mMode = startActionMode(new ModeCallback());
      }
   }

   public void onItemClick(AdapterView<?> adapterView, View view, int position,
    long id) {
      MediaViewItem cell = (MediaViewItem)view;
      // Long-click delete mode
      if (mMode != null) {
         if (cell == null) {
            Log.i("iFixit", "Delete cell null!");
            return;
         }

         mSelectedList.set(position, mSelectedList.get(position) ? false : true);
         mGalleryAdapter.invalidatedView();
      } else {
         String url = (String)view.getTag();

         if (url == null) {
            return;
         } else if (url.equals("") || url.indexOf(".") == 0) {
            return;
         }

         String imageUrl;
         boolean isLocal;
         if (mLocalURL.get(url) != null) {
            imageUrl = mLocalURL.get(url).mPath;
            isLocal = true;
         } else {
            imageUrl = url;
            isLocal = false;
         }

         Intent intent = new Intent(this, FullImageViewActivity.class);
         intent.putExtra(FullImageViewActivity.IMAGE_URL, imageUrl);
         intent.putExtra(FullImageViewActivity.LOCAL_URL, isLocal);
         startActivity(intent);
      }
   }

   private final class GalleryOnScrollListener implements AbsListView.OnScrollListener {
      int mCurScrollState;

      // Used to determine when to load more images.
      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
       int totalItemCount) {
         if ((firstVisibleItem + visibleItemCount) >= totalItemCount / 2 && !mLastPage) {
            if (MainApplication.get().isUserLoggedIn() &&
             !mNextPageRequestInProgress && mCurScrollState ==
             OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
               retrieveUserImages();
            }
         }
      }

      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
         mCurScrollState = scrollState;
      }
   }

   private AlertDialog createHelpDialog() {
      mShowingHelp = true;
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder
            .setTitle(getString(R.string.media_help_title))
            .setMessage(getString(R.string.media_help_messege))
            .setPositiveButton(getString(R.string.media_help_confirm),
               new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                     mShowingHelp = false;
                     dialog.cancel();
                  }
               });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            mShowingHelp = false;
         }
      });

      return dialog;
   }

   private AlertDialog createDeleteConfirmDialog() {
      mShowingDelete = true;
      int selectedCount = 0;
      for (boolean selected : mSelectedList) {
         if (selected) {
            selectedCount++;
         }
      }

      String msg = getString(R.string.confirm_delete_message) + " " +
       selectedCount + " ";
      if (selectedCount > 1) {
         msg += getString(R.string.images);
      } else {
         msg += getString(R.string.image);
      }
      msg += "?";

      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage(msg)
            .setPositiveButton(getString(R.string.logout_confirm),
               new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int id) {
                     mShowingDelete = false;
                     deleteSelectedPhotos();
                     dialog.cancel();
                  }
               })
            .setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
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

   private Bitmap buildBitmap(String url) {
      BitmapFactory.Options opt = new BitmapFactory.Options();
      opt.inSampleSize = 4;
      opt.inDither = false;
      opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
      Bitmap bitmap;
      bitmap = BitmapFactory.decodeFile(url, opt);
      return bitmap;
   }
}
