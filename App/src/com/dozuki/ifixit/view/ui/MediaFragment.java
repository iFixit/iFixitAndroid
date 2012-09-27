package com.dozuki.ifixit.view.ui;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.HTTPRequestHelper;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.view.model.AuthenicationPackage;
import com.dozuki.ifixit.view.model.LoginListener;
import com.dozuki.ifixit.view.model.TopicLeaf;
import com.dozuki.ifixit.view.model.UploadedImageInfo;
import com.dozuki.ifixit.view.model.User;
import com.dozuki.ifixit.view.model.UserImageInfo;
import com.dozuki.ifixit.view.model.UserImageList;
import com.ifixit.android.imagemanager.ImageManager;

public class MediaFragment extends SherlockFragment implements
		OnItemClickListener, OnClickListener, OnItemLongClickListener,
		LoginListener {
	private static final String TAG = "MediaFragment";
	private static final int MAX_LOADING_IMAGES = 15;
	private static final int MAX_STORED_IMAGES = 20;
	private static final int MAX_WRITING_IMAGES = 15;
	private static final int IMAGE_PAGE_SIZE = 40;
	protected static final String IMAGE_URL = "IMAGE_URL";
	protected static final String LOCAL_URL = "LOCAL_URL";
	protected static final String CAMERA_PATH = "CAMERA_PATH";
	private Context mContext;
	static final int SELECT_PICTURE = 1;
	static final int CAMERA_PIC_REQUEST = 2;
	private static final String USER_IMAGE_LIST = "USER_IMAGE_LIST";
	private static final String USER_SELECTED_LIST = "USER_SELECTED_LIST";
	private static final String IMAGES_DOWNLOADED = "IMAGES_DOWNLOADED";
	private static final String IMAGE_PREFIX = "IFIXIT_GALLERY";
	private static final String FILE_URI_KEY = "FILE_URI_KEY";
	private static final String IMAGE_UP = "IMAGE_UPLOADED";
	private static final String HASH_MAP = "HASH_MAP";
	static final String GALLERY_TITLE = "Gallery";
	private static final String SHOWING_HELP = "SHOWING_HELP";
	private static final String SHOWING_LOGOUT = "SHOWING_LOGOUT";
	private static final String SHOWING_DELETE = "SHOWING_DELETE";

	GridView mGridView;
	RelativeLayout mButtons;
	MediaAdapter galleryAdapter;
	TextView loginText;
	String userName;
	private ImageManager mImageManager;
	private static ArrayList<Boolean> selectedList;
	private HashMap<String, LocalImage> localURL;
	private HashMap<String, Bitmap> limages;
	private ImageSizes mImageSizes;
	static UserImageList mImageList;
	private ActionMode mMode;
	int mImagesDownloaded;
	boolean mLastPage;
	String cameraTempFileName;
	boolean nextPageRequestInProgress;
	static boolean showingHelp;
	static boolean showingLogout;
	static boolean showingDelete;

	private BroadcastReceiver mApiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			APIService.Result result = (APIService.Result) intent.getExtras()
					.getSerializable(APIService.RESULT);
			if (!result.hasError()) {
				if (intent.getAction().equals(APIService.ACTION_USER_MEDIA)) {
					UserImageList imageList = (UserImageList) result
							.getResult();
					if (imageList.getImages().size() > 0) {
						int oldImageSize = mImageList.getImages().size();
						for (int i = 0; i < imageList.getImages().size(); i++) {
							selectedList.add(false);
							mImageList.addImage(imageList.getImages().get(i));
						}
						mImagesDownloaded += (mImageList.getImages().size() - oldImageSize);
						galleryAdapter.invalidatedView();
						mLastPage = false;

					} else {
						mLastPage = true;
					}
					nextPageRequestInProgress = false;
				} else if (intent.getAction().equals(
						APIService.ACTION_UPLOAD_MEDIA)) {
					UploadedImageInfo imageinfo = (UploadedImageInfo) result
							.getResult();
					String url = intent.getExtras().getString(
							APIService.REQUEST_RESULT_INFORMATION);
					Log.e("IMAGE UPLOADED", "KEY: " + imageinfo.getmImageid());
					LocalImage cur = localURL.get(url);
					if (cur == null)
						return;
					cur.imgId = imageinfo.getmImageid();
					localURL.put(url, cur);
					mImagesDownloaded++;
					galleryAdapter.invalidatedView();
				} else if (intent.getAction().equals(
						APIService.ACTION_DELETE_MEDIA)) {

				}
			} else {
				APIService.getListMediaErrorDialog(mContext).show();
				nextPageRequestInProgress = false;
			}

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
			mImageManager = ((MainApplication) getActivity().getApplication())
					.getImageManager();
			mImageManager.setMaxLoadingImages(MAX_LOADING_IMAGES);
			mImageManager.setMaxStoredImages(MAX_STORED_IMAGES);
			mImageManager.setMaxWritingImages(MAX_WRITING_IMAGES);
		}

		mImageSizes = ((MainApplication) getActivity().getApplication())
				.getImageSizes();
		mMode = null;
		showingHelp = false;
		showingLogout = false;
		showingDelete = false;
		selectedList = new ArrayList<Boolean>();
		localURL = new HashMap<String, LocalImage>();
		limages = new HashMap<String, Bitmap>();
		if (savedInstanceState != null) {
			showingHelp = savedInstanceState.getBoolean(SHOWING_HELP);
			if (showingHelp)
				createHelpDialog(mContext).show();
			showingLogout = savedInstanceState.getBoolean(SHOWING_LOGOUT);
			if (showingLogout)
				LoginFragment.getLogoutDialog(mContext).show();
			showingDelete = savedInstanceState.getBoolean(SHOWING_DELETE);
			
			mImagesDownloaded = savedInstanceState.getInt(IMAGES_DOWNLOADED);
			mImageList = (UserImageList) savedInstanceState
					.getSerializable(USER_IMAGE_LIST);
			boolean[] selected = savedInstanceState
					.getBooleanArray(USER_SELECTED_LIST);
			for (boolean b : selected)
				selectedList.add(b);
			if (showingDelete)
				createDeleteConfirmDialog(mContext).show();
			galleryAdapter = new MediaAdapter();
			if (savedInstanceState.getString(CAMERA_PATH) != null)
				cameraTempFileName = savedInstanceState.getString(CAMERA_PATH);
			localURL = (HashMap<String, LocalImage>) savedInstanceState
					.getSerializable(HASH_MAP);
			for (LocalImage li : localURL.values()) {
				if (li.path.contains(".jpg"))
					limages.put(li.path, buildBitmap(li.path));
			}
		} else {
			mImageList = new UserImageList();
			galleryAdapter = new MediaAdapter();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.gallery_view, container, false);

		mGridView = (GridView) view.findViewById(R.id.gridview);
		mGridView.setOnScrollListener(new GalleryOnScrollListener());

		mGridView.setAdapter(galleryAdapter);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);

		mButtons = (RelativeLayout) view.findViewById(R.id.button_holder);

		loginText = ((TextView) view.findViewById(R.id.login_text));

		if (!((MainApplication) ((Activity) mContext).getApplication())
				.isUserLoggedIn()) {
			mButtons.setVisibility(View.GONE);
		} else {
			userName = ((MainApplication) ((Activity) mContext)
					.getApplication()).getUser().getUsername();
			loginText.setText("Logged in as " + userName);
			mButtons.setOnClickListener(this);
			if (mImageList.getImages().size() == 0)
				retrieveUserImages();
		}

		if (selectedList.contains(true)) {
			Log.i(TAG, "selected count: " + selectedList.size());
			setDeleteMode();
		}

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBooleanArray(USER_SELECTED_LIST,
				toPrimitiveArray(selectedList));
		savedInstanceState.putInt(IMAGES_DOWNLOADED, mImagesDownloaded);
		savedInstanceState.putSerializable(HASH_MAP, localURL);
		savedInstanceState.putSerializable(USER_IMAGE_LIST, mImageList);
		savedInstanceState.putBoolean(SHOWING_HELP, showingHelp);
		savedInstanceState.putBoolean(SHOWING_LOGOUT, showingLogout);
		savedInstanceState.putBoolean(SHOWING_DELETE, showingDelete);
		if (cameraTempFileName != null)
			savedInstanceState.putString(CAMERA_PATH, cameraTempFileName);
	}

	public void retrieveUserImages() {

		AuthenicationPackage authenicationPackage = new AuthenicationPackage();
		authenicationPackage.session = ((MainApplication) ((Activity) mContext)
				.getApplication()).getUser().getSession();
		nextPageRequestInProgress = true;
		int initialPageSize = 5;
		mContext.startService(APIService.userMediaIntent(mContext,
				authenicationPackage, "?limit="
						+ (IMAGE_PAGE_SIZE + initialPageSize) + "&offset="
						+ (mImagesDownloaded)));
		userName = ((MainApplication) ((Activity) mContext).getApplication())
				.getUser().getUsername();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mContext = (Context) activity;

		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement TopicSelectedListener");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(APIService.ACTION_USER_MEDIA);
		filter.addAction(APIService.ACTION_UPLOAD_MEDIA);
		filter.addAction(APIService.ACTION_DELETE_MEDIA);
		mContext.registerReceiver(mApiReceiver, filter);
	}

	@Override
	public void onPause() {
		super.onPause();

		try {
			mContext.unregisterReceiver(mApiReceiver);
		} catch (IllegalArgumentException e) {
			// Do nothing. This happens in the unlikely event that
			// unregisterReceiver has been called already.
		}

		// this helps out with manageing memory on context changes
		int count = mGridView.getCount();
		for (int i = 0; i < count; i++) {
			MediaViewItem v = (MediaViewItem) mGridView.getChildAt(i);
			if (v != null) {
				if (v.imageview.getDrawable() != null) {
					v.imageview.getDrawable().setCallback(null);
				}
			}
		}
		System.gc();
	}

	@Override
	public void onClick(View arg0) {

		switch (arg0.getId()) {
		case R.id.button_holder:
			showingLogout = true;
			LoginFragment.getLogoutDialog(mContext).show();
			break;

		}
	}

	public void launchGallery() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				SELECT_PICTURE);
	}

	public void launchCamera() {
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		File f;
		try {
			f = createImageFile();
			cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(f));
			startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = IMAGE_PREFIX + timeStamp + "_";
		File image = File.createTempFile(imageFileName, ".jpg", getAlbumDir());
		cameraTempFileName = image.getAbsolutePath();
		return image; 
	}

	private File getAlbumDir() {
		File storageDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			storageDir = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					"iFixitImages/");
			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d("MediaFrag",
								"failed to create directory iFixitImages");
						return null;
					}
				}
			}
		} else {
			Log.v(getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}
		return storageDir;
	} // end getAlbumDir()

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = ((Activity) mContext).managedQuery(uri, projection,
				null, null, null);
		if (cursor != null) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == MediaFragment.SELECT_PICTURE) {
				Log.i("MediaFragment", "Adding gallery pic...");
				Uri selectedImageUri = data.getData();
				galleryAdapter.addUri(selectedImageUri);
				AuthenicationPackage authenicationPackage = new AuthenicationPackage();
				authenicationPackage.session = ((MainApplication) ((Activity) mContext)
						.getApplication()).getUser().getSession();
				mContext.startService(APIService.getUploadImageIntent(mContext,
						authenicationPackage, getPath(selectedImageUri),
						selectedImageUri.toString()));

			} else if (requestCode == MediaFragment.CAMERA_PIC_REQUEST) {
				if (cameraTempFileName == null) {
					Log.e(TAG, "Error cameraTempFile is null!");
					return;
				}
				Log.i(TAG, "Adding camera pic...");
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inSampleSize = 2;
				opt.inDither = true;
				opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
				String fPath = new String(cameraTempFileName);
				Bitmap img = BitmapFactory.decodeFile(fPath, opt);

				Log.i("MediaFrag", "img path: " + cameraTempFileName
						+ " img width: " + img.getWidth() + " img height: "
						+ img.getHeight());

				// Uri selectedImageUri = data.getData();
				// galleryAdapter.addFile(cameraTempFileName);
				galleryAdapter.addFile(new String(fPath));
				AuthenicationPackage authenicationPackage = new AuthenicationPackage();
				authenicationPackage.session = ((MainApplication) ((Activity) mContext)
						.getApplication()).getUser().getSession();
				mContext.startService(APIService.getUploadImageIntent(mContext,
						authenicationPackage, fPath, fPath));
			}
		}
	}

	private class MediaAdapter extends BaseAdapter {

		public MediaAdapter() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		public void addUri(Uri uri) {
			// mediaList.add(uri);
			UserImageInfo userImageInfo = new UserImageInfo();
			String url = uri.toString();
			if (localURL.containsKey(url)) {
				Log.e(TAG, "Duplicate image found: " + getPath(uri));
				return;
			}
			userImageInfo.setGuid(url);
			userImageInfo.setmImageId(null);
			mImageList.addImage(userImageInfo);
			selectedList.add(false);
			localURL.put(url, new LocalImage(getPath(uri)));
			// Log.i("MEdiaFrag", "KEY: " + url + " Path: " + getPath(uri));
			invalidatedView();
		}

		public void addFile(String path) {
			UserImageInfo userImageInfo = new UserImageInfo();
			String url = path;
			userImageInfo.setGuid(path);
			userImageInfo.setmImageId(null);
			mImageList.addImage(userImageInfo);
			selectedList.add(false);
			localURL.put(url, new LocalImage(path));
			limages.put(url, buildBitmap(url));
			invalidatedView();
		}

		public void invalidatedView() {
			mGridView.invalidateViews();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mImageList.getImages().size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			MediaViewItem itemView = (MediaViewItem) convertView;

			if (convertView == null) {
				itemView = new MediaViewItem(getActivity(), mImageManager);
			}

			itemView.setLoading(false);

			if (mImageList != null) {
				if (mImageList.getImages().get(position).getmImageId() != null
						&& localURL.containsKey(mImageList.getImages()
								.get(position).getmGuid()) == false) {
					String image = mImageList.getImages().get(position)
							.getmGuid()
							+ mImageSizes.getThumb();
					itemView.setImageItem(image, getActivity(), !mImageList
							.getImages().get(position).getLoaded());
					itemView.listRef = mImageList.getImages().get(position);
					mImageList.getImages().get(position).setLoaded(true);
				} else {
					Uri temp = Uri.parse(mImageList.getImages().get(position)
							.getmGuid());
					Bitmap bitmap;
					if (temp.toString().contains(".jpg")) {
						bitmap = limages.get(mImageList.getImages()
								.get(position).getmGuid());
					} else {
						bitmap = MediaStore.Images.Thumbnails.getThumbnail(
								mContext.getContentResolver(),
								ContentUris.parseId(temp),
								MediaStore.Images.Thumbnails.MINI_KIND,
								(BitmapFactory.Options) null);
					}
					itemView.imageview.setImageBitmap(bitmap);
					itemView.listRef = mImageList.getImages().get(position);
					if (localURL.containsKey(mImageList.getImages()
							.get(position).getmGuid())) {
						if (localURL.get(mImageList.getImages().get(position)
								.getmGuid()).imgId == null) {
							itemView.setLoading(true);
							Log.e(TAG, "image loading!");
						} else {
							mImageList
									.getImages()
									.get(position)
									.setmImageId(
											localURL.get(mImageList.getImages()
													.get(position).getmGuid()).imgId);
							itemView.setLoading(false);
							Log.e(TAG, "image stoped loading!");
						}
					}
				}

			}
			if (selectedList.get(position))
				itemView.selectImage.setVisibility(View.VISIBLE);
			else
				itemView.selectImage.setVisibility(View.INVISIBLE);
			itemView.setTag(mImageList.getImages().get(position).getmGuid());
			return itemView;
		}
	}

	public final class ModeCallback implements ActionMode.Callback {

		Context _pContext;
		
		public ModeCallback(Context parentContext)
		{
			_pContext = parentContext;
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Create the menu from the xml file
			MenuInflater inflater = getSherlockActivity()
					.getSupportMenuInflater();
			inflater.inflate(R.menu.contextual_delete, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// Here, you can checked selected items to adapt available actions
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			if (mode == mMode) {
				mMode = null;
			}
			for (int i = selectedList.size() - 1; i > -1; i--) {
				if (selectedList.get(i)) {
					selectedList.set(i, false);
				}
			}
			galleryAdapter.invalidatedView();
			mButtons.setVisibility(View.VISIBLE);
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			
			if(!selectedList.contains(true))
			{
				mode.finish();
				return true;
			}

			createDeleteConfirmDialog(_pContext).show();
			
			//mode.finish();
			return true;
		}

	};

	private void deleteSelectedPhotos() {
		if (mImageList == null)
			return;
		
		String deleteQuery = "?";

		for (int i = selectedList.size() - 1; i > -1; i--) {
			if (selectedList.get(i)) {
				selectedList.remove(i);
				deleteQuery += "imageids[]="
						+ mImageList.getImages().get(i).getmImageId() + "&";
				mImageList.getImages().remove(i);
			}
		}

		if (deleteQuery.length() > 1) {
			deleteQuery = deleteQuery.substring(0, deleteQuery.length() - 1);
		}

		AuthenicationPackage authenicationPackage = new AuthenicationPackage();
		authenicationPackage.session = ((MainApplication) ((Activity) mContext)
				.getApplication()).getUser().getSession();
		mContext.startService(APIService.getDeleteMediaIntent(mContext,
				authenicationPackage, deleteQuery));
		mMode.finish();
	}

	@Override
	public void onLogin(User user) {
		if (mImageList.getImages().size() == 0) {
			userName = ((MainApplication) ((Activity) mContext)
					.getApplication()).getUser().getUsername();
			loginText.setText("Logged in as " + userName);
			mButtons.setOnClickListener(this);
			retrieveUserImages();
			mButtons.setVisibility(View.VISIBLE);
			mButtons.setAnimation(AnimationUtils.loadAnimation(mContext,
					R.anim.slide_in_bottom));
		}
	}

	public MenuInflater getSupportMenuInflater() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		setDeleteMode();
		return false;
	}

	public void setDeleteMode() {
		if (mMode == null) {
			// mButtons.setVisibility(View.GONE);

			Animation animHide = AnimationUtils.loadAnimation(mContext,
					R.anim.slide_out_bottom_slow);
			animHide.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation arg0) {
					// TODO Auto-generated method stub
					mButtons.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub

				}

			});
			mButtons.startAnimation(animHide);
			mMode = this.getSherlockActivity().startActionMode(
					new ModeCallback(mContext));
		}
	}

	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {

		MediaViewItem cell = (MediaViewItem) view;
		// Long-click delete mode
		if (mMode != null) {
			if (cell == null) {
				Log.e("MediaFragment", "cell null!");
				return;
			}

			selectedList.set(position, (selectedList.get(position)) ? false
					: true);
			galleryAdapter.invalidatedView();

		} else {
			if (view.getTag() != null) {
				Log.i("MediaFragment", (String) view.getTag());
			} else
				return;
			String url = (String) view.getTag();
			if (url.equals("") || url.indexOf(".") == 0) {
				return;
			}
			if (localURL.get(url) != null) {
				Intent intent = new Intent(getActivity(),
						FullImageViewActivity.class);
				intent.putExtra(IMAGE_URL, localURL.get(url).path);
				intent.putExtra(LOCAL_URL, true);
				startActivity(intent);
			} else {
				Intent intent = new Intent(getActivity(),
						FullImageViewActivity.class);
				intent.putExtra(IMAGE_URL, url);
				startActivity(intent);
			}
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

	public final class GalleryOnScrollListener implements
			AbsListView.OnScrollListener {
		int mCurScrollState;

		// used to determine when to load more images
		@Override
		public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
			if ((arg1 + arg2) >= arg3 / 2 && !mLastPage) {
				if (((MainApplication) ((Activity) mContext).getApplication())
						.isUserLoggedIn()
						&& !nextPageRequestInProgress
						&& mCurScrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					nextPageRequestInProgress = true;
					AuthenicationPackage authenicationPackage = new AuthenicationPackage();
					authenicationPackage.session = ((MainApplication) ((Activity) mContext)
							.getApplication()).getUser().getSession();
					mContext.startService(APIService.userMediaIntent(mContext,
							authenicationPackage, "?limit=" + IMAGE_PAGE_SIZE
									+ "&offset=" + (mImagesDownloaded)));
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
		// TODO Auto-generated method stub

	}

	static AlertDialog createHelpDialog(final Context context) {
		showingHelp = true;
		HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(context);
		builder.setTitle(context.getString(R.string.media_help_title))
				.setMessage(context.getString(R.string.media_help_messege))
				.setPositiveButton(
						context.getString(R.string.media_help_confirm),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								showingHelp = false;
								dialog.cancel();
							}
						}).setCancelable(false);

		return builder.create();
	}

	AlertDialog createDeleteConfirmDialog(final Context context) {
		showingDelete = true;
		int selectedCount = 0;
		for(boolean b : selectedList)
		{
			if(b)
				selectedCount++;
		}
		HoloAlertDialogBuilder builder = new HoloAlertDialogBuilder(context);
		builder.setTitle(context.getString(R.string.confirm_delete))
				.setMessage("Are you sure you want to delete these " + selectedCount +  " images?")
				.setPositiveButton(context.getString(R.string.logout_confirm),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								showingDelete = false;
								deleteSelectedPhotos();
								dialog.cancel();
							}
						})
				.setNegativeButton(R.string.logout_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								showingDelete = false;
								dialog.cancel();
							}
						}).setCancelable(false);;

		return builder.create();
	}

	private Bitmap buildBitmap(String url) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inSampleSize = 4;
		opt.inDither = false;
		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap b;
		b = BitmapFactory.decodeFile(url, opt);
		return b;
	}

}
