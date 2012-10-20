package com.dozuki.ifixit.view.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
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
import android.widget.TextView;
import android.widget.Toast;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APIEndpoint;
import com.dozuki.ifixit.util.APIReceiver;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.view.model.AuthenicationPackage;
import com.dozuki.ifixit.view.model.LoginListener;
import com.dozuki.ifixit.view.model.UploadedImageInfo;
import com.dozuki.ifixit.view.model.User;
import com.dozuki.ifixit.view.model.UserImageInfo;
import com.dozuki.ifixit.view.model.UserImageList;
import com.ifixit.android.imagemanager.ImageManager;

public class MediaFragment extends SherlockFragment implements
 OnItemClickListener, OnClickListener, OnItemLongClickListener, LoginListener {

   public TextView noImagesText;
   public static boolean showingHelp;
   public static boolean showingLogout;
   public static boolean showingDelete;

   private static final String TAG = "MediaFragment";
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
   private static final String SHOWING_LOGOUT = "SHOWING_LOGOUT";
   private static final String SHOWING_DELETE = "SHOWING_DELETE";
   private static final int MAX_UPLOAD_COUNT = 4;;

   private Context mContext;
   private GridView mGridView;
   private RelativeLayout mButtons;
   private MediaAdapter mGalleryAdapter;
   private TextView mLoginText;

   private String mUserName;
   private ImageManager mImageManager;
   private static ArrayList<Boolean> mSelectedList;
   private HashMap<String, LocalImage> mLocalURL;
   private HashMap<String, Bitmap> mLimages;
   private ImageSizes mImageSizes;
   private static UserImageList mImageList;
   private ActionMode mMode;
   private int mImagesDownloaded;
   private boolean mLastPage;
   private String mCameraTempFileName;
   private boolean mNextPageRequestInProgress;

   private APIReceiver mApiReceiver = new APIReceiver() {
      public void onSuccess(Object result, Intent intent) {
         if (intent.getAction().equals(APIEndpoint.USER_IMAGES.mAction)) {
            UserImageList imageList = (UserImageList) result;
            if (imageList.getImages().size() > 0) {
               int oldImageSize = mImageList.getImages().size();
               for (int i = 0; i < imageList.getImages().size(); i++) {
                  mSelectedList.add(false);
                  mImageList.addImage(imageList.getImages().get(i));
               }
               mImagesDownloaded += (mImageList.getImages().size() - oldImageSize);
               mGalleryAdapter.invalidatedView();
               mLastPage = false;
               noImagesText.setVisibility(View.GONE);
            } else {
               mLastPage = true;
               noImagesText.setVisibility(View.VISIBLE);
            }
            mNextPageRequestInProgress = false;
         } else if (intent.getAction().equals(APIEndpoint.UPLOAD_IMAGE.mAction)) {
            UploadedImageInfo imageinfo = (UploadedImageInfo)result;
            String url = intent.getExtras().getString(APIService.REQUEST_RESULT_INFORMATION);

            LocalImage cur = mLocalURL.get(url);
            if (cur == null)
               return;
            cur.mImgid = imageinfo.getImageid();
            mLocalURL.put(url, cur);
            mImagesDownloaded++;
            mGalleryAdapter.invalidatedView();
         } else if (intent.getAction().equals(APIEndpoint.DELETE_IMAGE.mAction)) {

         }
      }

      public void onFailure(APIService.Error error, Intent intent) {
         APIService.getListMediaErrorDialog(mContext).show();
         mNextPageRequestInProgress = false;
      }
   };

   public MediaFragment(Context con) {
      mContext = con;
   }

   public MediaFragment() {

   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (mImageManager == null) {
         mImageManager = ((MainApplication)getActivity().getApplication()).getImageManager();
         mImageManager.setMaxLoadingImages(MAX_LOADING_IMAGES);
         mImageManager.setMaxStoredImages(MAX_STORED_IMAGES);
         mImageManager.setMaxWritingImages(MAX_WRITING_IMAGES);
      }

      mImageSizes = ((MainApplication)getActivity().getApplication()).getImageSizes();
      mMode = null;
      showingHelp = false;
      showingLogout = false;
      showingDelete = false;
      mSelectedList = new ArrayList<Boolean>();
      mLocalURL = new HashMap<String, LocalImage>();
      mLimages = new HashMap<String, Bitmap>();
      if (savedInstanceState != null) {
         showingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
         if (showingHelp)
            createHelpDialog(mContext).show();
         showingLogout = savedInstanceState.getBoolean(SHOWING_LOGOUT);
         if (showingLogout)
            LoginFragment.getLogoutDialog(mContext).show();
         showingDelete = savedInstanceState.getBoolean(SHOWING_DELETE);

         mImagesDownloaded = savedInstanceState.getInt(IMAGES_DOWNLOADED);
         mImageList = (UserImageList)savedInstanceState.getSerializable(USER_IMAGE_LIST);
         mGalleryAdapter = new MediaAdapter();

         boolean[] selected = savedInstanceState.getBooleanArray(USER_SELECTED_LIST);
         for (boolean b : selected) {
            mSelectedList.add(b);
         }

         if (showingDelete) {
            createDeleteConfirmDialog(mContext).show();
         }

         if (savedInstanceState.getString(CAMERA_PATH) != null) {
            mCameraTempFileName = savedInstanceState.getString(CAMERA_PATH);
         }

         mLocalURL = (HashMap<String, LocalImage>)savedInstanceState.getSerializable(HASH_MAP);
         for (LocalImage li : mLocalURL.values()) {
            if (li.mPath.contains(".jpg"))
               mLimages.put(li.mPath, buildBitmap(li.mPath));
         }
      } else {
         mImageList = new UserImageList();
         mGalleryAdapter = new MediaAdapter();
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.gallery_view, container, false);

      noImagesText = ((TextView)view.findViewById(R.id.no_images_text));
      if (mImageList.getImages().size() < 1) {
         noImagesText.setVisibility(View.VISIBLE);
      } else {
         noImagesText.setVisibility(View.GONE);
      }

      mGridView = (GridView)view.findViewById(R.id.gridview);
      mGridView.setOnScrollListener(new GalleryOnScrollListener());

      mGridView.setAdapter(mGalleryAdapter);
      mGridView.setOnItemClickListener(this);
      mGridView.setOnItemLongClickListener(this);

      mButtons = (RelativeLayout)view.findViewById(R.id.button_holder);
      mLoginText = ((TextView)view.findViewById(R.id.login_text));

      if (!((MainApplication)((Activity)mContext).getApplication()).isUserLoggedIn()) {
         mButtons.setVisibility(View.GONE);
      } else {
         mUserName = ((MainApplication)((Activity)mContext).getApplication()).
          getUser().getUsername();
         mLoginText.setText("Logged in as " + mUserName);
         mButtons.setOnClickListener(this);
         if (mImageList.getImages().size() == 0) {
            retrieveUserImages();
         }
      }

      if (mSelectedList.contains(true)) {
         setDeleteMode();
      }

      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putBooleanArray(USER_SELECTED_LIST,
       toPrimitiveArray(mSelectedList));
      savedInstanceState.putInt(IMAGES_DOWNLOADED, mImagesDownloaded);
      savedInstanceState.putSerializable(HASH_MAP, mLocalURL);
      savedInstanceState.putSerializable(USER_IMAGE_LIST, mImageList);
      savedInstanceState.putBoolean(SHOWING_HELP, showingHelp);
      savedInstanceState.putBoolean(SHOWING_LOGOUT, showingLogout);
      savedInstanceState.putBoolean(SHOWING_DELETE, showingDelete);

      if (mCameraTempFileName != null) {
         savedInstanceState.putString(CAMERA_PATH, mCameraTempFileName);
      }
   }

   public void retrieveUserImages() {
      AuthenicationPackage authenicationPackage = new AuthenicationPackage();
      authenicationPackage.session = ((MainApplication)((Activity)mContext).getApplication())
       .getUser().getSession();
      mNextPageRequestInProgress = true;
      int initialPageSize = 5;
      mContext.startService(APIService.getUserImagesIntent(mContext,
       authenicationPackage, "?limit=" + (IMAGE_PAGE_SIZE + initialPageSize) +
       "&offset=" + mImagesDownloaded));
      mUserName = ((MainApplication)((Activity)mContext).getApplication()).
       getUser().getUsername();
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
      mContext = (Context)activity;
   }

   @Override
   public void onResume() {
      super.onResume();
      IntentFilter filter = new IntentFilter();
      filter.addAction(APIEndpoint.USER_IMAGES.mAction);
      filter.addAction(APIEndpoint.UPLOAD_IMAGE.mAction);
      filter.addAction(APIEndpoint.DELETE_IMAGE.mAction);
      mContext.registerReceiver(mApiReceiver, filter);
   }

   @Override
   public void onDestroy() {
      try {
         mContext.unregisterReceiver(mApiReceiver);
      } catch (IllegalArgumentException e) {
         // Do nothing. This happens in the unlikely event that
         // unregisterReceiver has been called already.
      }

      super.onDestroy();
   }

   @Override
   public void onPause() {
      super.onPause();

      // This helps out with managing memory on context changes.
      int count = mGridView.getCount();
      for (int i = 0; i < count; i++) {
         MediaViewItem mediaView = (MediaViewItem)mGridView.getChildAt(i);
         if (mediaView != null) {
            if (mediaView.imageview.getDrawable() != null) {
               mediaView.imageview.getDrawable().setCallback(null);
            }
         }
      }
      System.gc();
   }

   @Override
   public void onClick(View view) {
      switch (view.getId()) {
      case R.id.button_holder:
         showingLogout = true;
         LoginFragment.getLogoutDialog(mContext).show();
         break;
      }
   }

   protected void launchGallery() {
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

   public String getPath(Uri uri) {
      String[] projection = { MediaStore.Images.Media.DATA };
      Cursor cursor = ((Activity)mContext).managedQuery(uri, projection, null,
       null, null);
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
      if (resultCode == Activity.RESULT_OK) {
         if (requestCode == MediaFragment.SELECT_PICTURE) {
            Uri selectedImageUri = data.getData();

            // check file type
            String path = getPath(selectedImageUri);
            if (path == null || !(path.toLowerCase().contains(".jpeg") ||
             path.toLowerCase().contains(".jpg") || path.toLowerCase().contains(".png"))) {
               Toast.makeText(mContext, mContext.getString(R.string.non_image_error),
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
               Toast.makeText(mContext, mContext.getString(R.string.too_many_image_error),
                Toast.LENGTH_LONG).show();
               return;
            }

            String key = mGalleryAdapter.addUri(selectedImageUri);
            AuthenicationPackage authenicationPackage = new AuthenicationPackage();
            authenicationPackage.session = ((MainApplication)((Activity)mContext)
             .getApplication()).getUser().getSession();
            mContext.startService(APIService.getUploadImageIntent(mContext, authenicationPackage,
             getPath(selectedImageUri), key));
         } else if (requestCode == MediaFragment.CAMERA_PIC_REQUEST) {
            if (mCameraTempFileName == null) {
               Log.w(TAG, "Error cameraTempFile is null!");
               return;
            }
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 2;
            opt.inDither = true;
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            String fPath = new String(mCameraTempFileName);
            Bitmap img = BitmapFactory.decodeFile(fPath, opt);

            String key = mGalleryAdapter.addFile(new String(fPath));
            AuthenicationPackage authenicationPackage = new AuthenicationPackage();
            authenicationPackage.session = ((MainApplication)((Activity)mContext)
             .getApplication()).getUser().getSession();
            mContext.startService(APIService.getUploadImageIntent(mContext,
             authenicationPackage, fPath, key));
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
         if (mImageList.getImages().size() > 0)
            noImagesText.setVisibility(View.GONE);
         return mImageList.getImages().size();
      }

      @Override
      public Object getItem(int arg0) {
         return null;
      }

      public View getView(int position, View convertView, ViewGroup parent) {
         MediaViewItem itemView = (MediaViewItem)convertView;

         if (convertView == null) {
            itemView = new MediaViewItem(getActivity(), mImageManager);
         }

         itemView.setLoading(false);

         if (mImageList != null) {
            UserImageInfo image = mImageList.getImages().get(position);

            // image was pulled from the server
            if (mImageList.getImages().get(position).getImageid() != null &&
             mImageList.getImages().get(position).getKey() == null) {
               String imageUrl = image.getGuid() + mImageSizes.getThumb();
               itemView.setImageItem(imageUrl, getActivity(), !image.getLoaded());
               itemView.listRef = image;
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
                   mContext.getContentResolver(), ContentUris.parseId(temp),
                   MediaStore.Images.Thumbnails.MINI_KIND, null);
               }

               itemView.imageview.setImageBitmap(bitmap);
               itemView.listRef = image;
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
            itemView.selectImage.setVisibility(View.VISIBLE);
         } else {
            itemView.selectImage.setVisibility(View.INVISIBLE);
         }

         return itemView;
      }
   }

   public final class ModeCallback implements ActionMode.Callback {
      private Context mParentContext;

      public ModeCallback(Context parentContext) {
         mParentContext = parentContext;
      }

      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
         // Create the menu from the xml file
         MenuInflater inflater = getSherlockActivity().getSupportMenuInflater();
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

         createDeleteConfirmDialog(mParentContext).show();

         return true;
      }
   };

   private void deleteSelectedPhotos() {
      if (mImageList == null)
         return;

      String deleteQuery = "?";

      for (int i = mSelectedList.size() - 1; i >= 0; i--) {
         if (mSelectedList.get(i)) {
            mSelectedList.remove(i);
            deleteQuery += "imageids[]=" + mImageList.getImages().get(i).getImageid() + "&";

            if (mImageList.getImages().get(i).getImageid() == null) {
               Toast.makeText(mContext, mContext.getString(R.string.delete_loading_image_error),
                Toast.LENGTH_LONG).show();
            }
            mImageList.getImages().remove(i);
         }
      }

      if (deleteQuery.length() > 1) {
         deleteQuery = deleteQuery.substring(0, deleteQuery.length() - 1);
      }

      AuthenicationPackage authenicationPackage = new AuthenicationPackage();
      authenicationPackage.session = ((MainApplication) ((Activity) mContext).getApplication())
       .getUser().getSession();
      mContext.startService(APIService.getDeleteImageIntent(mContext,
       authenicationPackage, deleteQuery));

      if (mImageList.getImages().size() == 0) {
         noImagesText.setVisibility(View.VISIBLE);
      }

      mMode.finish();
   }

   @Override
   public void onLogin(User user) {
      if (mImageList.getImages().size() == 0) {
         mUserName = ((MainApplication)((Activity)mContext).getApplication()).
          getUser().getUsername();
         mLoginText.setText(getString(R.string.logged_in_as) + " " + mUserName);
         mButtons.setOnClickListener(this);
         retrieveUserImages();
         mButtons.setVisibility(View.VISIBLE);
         mButtons.setAnimation(AnimationUtils.loadAnimation(mContext,
          R.anim.slide_in_bottom));
      }
   }

   @Override
   public boolean onItemLongClick(AdapterView<?> parent, View view,
    int position, long id) {
      setDeleteMode();
      return false;
   }

   public void setDeleteMode() {
      if (mMode == null) {
         Animation animHide = AnimationUtils.loadAnimation(mContext,
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
         mMode = getSherlockActivity().startActionMode(
          new ModeCallback(mContext));
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

         Intent intent = new Intent(getActivity(), FullImageViewActivity.class);
         intent.putExtra(FullImageViewActivity.IMAGE_URL, imageUrl);
         intent.putExtra(FullImageViewActivity.LOCAL_URL, isLocal);
         startActivity(intent);
      }
   }

   private boolean[] toPrimitiveArray(final List<Boolean> booleanList) {
      final boolean[] primitives = new boolean[booleanList.size()];
      int index = 0;
      for (Boolean object : booleanList) {
         primitives[index++] = object;
      }
      return primitives;
   }

   public final class GalleryOnScrollListener implements AbsListView.OnScrollListener {
      int mCurScrollState;

      // Used to determine when to load more images.
      @Override
      public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
         if ((arg1 + arg2) >= arg3 / 2 && !mLastPage) {
            if (((MainApplication)((Activity)mContext).getApplication()).isUserLoggedIn() &&
             !mNextPageRequestInProgress && mCurScrollState ==
             OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
               mNextPageRequestInProgress = true;
               AuthenicationPackage authenicationPackage = new AuthenicationPackage();
               authenicationPackage.session = ((MainApplication)((Activity)mContext)
                .getApplication()).getUser().getSession();
               mContext.startService(APIService.getUserImagesIntent(mContext,
                authenicationPackage, "?limit=" + IMAGE_PAGE_SIZE + "&offset=" + mImagesDownloaded));
            }
         }
      }

      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
         mCurScrollState = scrollState;
      }
   }

   @Override
   public void onLogout() {
   }

   public static AlertDialog createHelpDialog(final Context context) {
      showingHelp = true;
      HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(context);
      builder
            .setTitle(context.getString(R.string.media_help_title))
            .setMessage(context.getString(R.string.media_help_messege))
            .setPositiveButton(context.getString(R.string.media_help_confirm),
               new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                     showingHelp = false;
                     dialog.cancel();
                  }
               });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            showingHelp = false;
         }
      });

      return dialog;
   }

   private AlertDialog createDeleteConfirmDialog(final Context context) {
      showingDelete = true;
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

      HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(context);
      builder
            .setTitle(context.getString(R.string.confirm_delete_title))
            .setMessage(msg)
            .setPositiveButton(context.getString(R.string.logout_confirm),
               new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int id) {
                     showingDelete = false;
                     deleteSelectedPhotos();
                     dialog.cancel();
                  }
               })
            .setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                  showingDelete = false;
                  dialog.cancel();
               }
            });

      AlertDialog dialog = builder.create();
      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
            showingDelete = false;
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
