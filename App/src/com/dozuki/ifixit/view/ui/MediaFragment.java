package com.dozuki.ifixit.view.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.APIService;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.view.model.AuthenicationPackage;
import com.dozuki.ifixit.view.model.LoginListener;
import com.dozuki.ifixit.view.model.TopicLeaf;
import com.dozuki.ifixit.view.model.User;
import com.dozuki.ifixit.view.model.UserImageInfo;
import com.dozuki.ifixit.view.model.UserImageList;
import com.ifixit.android.imagemanager.ImageManager;

public class MediaFragment extends SherlockFragment implements
		OnItemClickListener, OnClickListener, OnItemLongClickListener,
		LoginListener {

	private static final int MAX_LOADING_IMAGES = 20;
	private static final int MAX_STORED_IMAGES = 30;
	private static final int MAX_WRITING_IMAGES = 20;
	private static final int IMAGE_PAGE_SIZE = 40;
	protected static final String IMAGE_URL = "IMAGE_URL";
	protected static final String LOCAL_URL = "LOCAL_URL";
	private Context mContext;
	static final int SELECT_PICTURE = 1;
	static final int CAMERA_PIC_REQUEST = 2;
	private static final String USER_IMAGE_LIST = "USER_IMAGE_LIST";
	private static final String USER_SELECTED_LIST = "USER_SELECTED_LIST";
	private static final String CURRENT_PAGE = "CURRENT_PAGE";
	private static final String IMAGE_PREFIX = "IFIXIT_GALLERY";
	GridView mGridView;
	RelativeLayout mButtons;
	MediaAdapter galleryAdapter;
	private ImageManager mImageManager;
	private ArrayList<Boolean> selectedList;
	private HashMap<String, String> localURL;
	private ImageSizes mImageSizes;
	static UserImageList mImageList;
	private ActionMode mMode;
	private ProgressBar mProgressBar;
	int mCurrentPage;
	boolean mLastPage;
	int mImageTransactionsInProgress;
	String cameraTempFileName;

	private BroadcastReceiver mApiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			APIService.Result result = (APIService.Result) intent.getExtras()
					.getSerializable(APIService.RESULT);
			if (!result.hasError()
					&& result.getResult() instanceof UserImageList) {

				if (intent.getAction() == APIService.ACTION_USER_MEDIA) {
					UserImageList imageList = (UserImageList) result
							.getResult();
					if (imageList.getmImages().size() > 0) {
						for (int i = 0; i < imageList.getmImages().size(); i++) {
							selectedList.add(false);
							mImageList.addImage(imageList.getmImages().get(i));
						}
						galleryAdapter.invalidatedView();
						mCurrentPage++;
						mLastPage = false;
					} else {
						mLastPage = true;
					}
				} else if (intent.getAction() == APIService.ACTION_UPLOAD_MEDIA) {

				} else if (intent.getAction() == APIService.ACTION_DELETE_MEDIA) {

				}

			}
			// a transaction is complete
			mImageTransactionsInProgress--;
			if (mImageTransactionsInProgress <= 0) {
				hideLoading();
				mImageTransactionsInProgress = 0;
			}
		}
	};

	public MediaFragment(ImageManager imageManager) {
		
	}

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
		selectedList = new ArrayList<Boolean>();
		localURL = new HashMap<String, String>();
		if (savedInstanceState != null) {
			mCurrentPage = savedInstanceState.getInt(CURRENT_PAGE);
			mImageList = (UserImageList) savedInstanceState
					.getSerializable(USER_IMAGE_LIST);
			boolean[] selected = savedInstanceState
					.getBooleanArray(USER_SELECTED_LIST);
			for (boolean b : selected)
				selectedList.add(b);
			galleryAdapter = new MediaAdapter();
		} else {
			mImageList = new UserImageList();
			galleryAdapter = new MediaAdapter();
		}
		mImageTransactionsInProgress = 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.gallery_view, container, false);

		mGridView = (GridView) view.findViewById(R.id.gridview);
		mProgressBar = (ProgressBar) view
				.findViewById(R.id.gallery_loading_bar);
		mGridView.setOnScrollListener(new GalleryOnScrollListener());

		mGridView.setAdapter(galleryAdapter);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);

		mButtons = (RelativeLayout) view.findViewById(R.id.button_holder);

		((ImageButton) view.findViewById(R.id.gallery_button))
				.setOnClickListener(this);

		((ImageButton) view.findViewById(R.id.camera_button))
				.setOnClickListener(this);
		hideLoading();

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBooleanArray(USER_SELECTED_LIST,
				toPrimitiveArray(selectedList));
		savedInstanceState.putInt(CURRENT_PAGE, mCurrentPage);
		savedInstanceState.putSerializable(USER_IMAGE_LIST, mImageList);
	}

	public void retrieveUserImages() {
		showLoading();
		AuthenicationPackage authenicationPackage = new AuthenicationPackage();
		authenicationPackage.session = ((MainApplication) ((Activity) mContext)
				.getApplication()).getUser().getSession();
		mImageTransactionsInProgress++;
		mContext.startService(APIService.userMediaIntent(mContext,
				authenicationPackage, "?limit=" + IMAGE_PAGE_SIZE + "&offset="
						+ (IMAGE_PAGE_SIZE * mCurrentPage)));
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

	public void showLoading() {
		if (!(mProgressBar == null)) {
			mProgressBar.startAnimation(AnimationUtils.makeInAnimation(
					mContext, true));
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	public void hideLoading() {
		if (!(mProgressBar == null)) {
			mProgressBar.startAnimation(AnimationUtils.makeOutAnimation(
					mContext, true));
			mProgressBar.setVisibility(View.INVISIBLE);
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
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.gallery_button:
			Intent intent = new Intent(); 
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(
					Intent.createChooser(intent, "Select Picture"),
					SELECT_PICTURE);
			break;
		case R.id.camera_button:
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
			break;
		}
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = IMAGE_PREFIX + timeStamp + "_";
		File image = File.createTempFile(imageFileName, ".jpg",
				getAlbumDir());
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
						Log.d("MediaFrag", "failed to create directory iFixitImages");
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
				mImageTransactionsInProgress++;
				mContext.startService(APIService.getUploadImageIntent(mContext,
						authenicationPackage, getPath(selectedImageUri)));
				showLoading();
			} else if (requestCode == MediaFragment.CAMERA_PIC_REQUEST
					&& cameraTempFileName != null) {
				Log.i("MediaFragment", "Adding camera pic...");
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inSampleSize = 2;
				opt.inDither = true;
				opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
				Bitmap img = BitmapFactory.decodeFile(cameraTempFileName, opt);
				Log.i("MediaFrag", "img path: " + cameraTempFileName + " img width: " + img.getWidth()
						+ " img height: " + img.getHeight());

				// Uri selectedImageUri = data.getData();
				galleryAdapter.addFile(cameraTempFileName);
				AuthenicationPackage authenicationPackage = new AuthenicationPackage();
				authenicationPackage.session = ((MainApplication) ((Activity) mContext)
						.getApplication()).getUser().getSession();
				mImageTransactionsInProgress++;
				mContext.startService(APIService.getUploadImageIntent(mContext,
						authenicationPackage, cameraTempFileName));
				showLoading();

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
			userImageInfo.setmGuid(url);
			userImageInfo.setmImageId(null);
			mImageList.getmImages().add(userImageInfo);
			selectedList.add(false);
			localURL.put(url, getPath(uri));
			invalidatedView();
		}

		public void addFile(String path) {
			UserImageInfo userImageInfo = new UserImageInfo();
			String url = path;
			userImageInfo.setmGuid(url);
			userImageInfo.setmImageId(null);
			mImageList.getmImages().add(userImageInfo);
			selectedList.add(false);
			localURL.put(url, path);
			invalidatedView();
		}

		public void invalidatedView() {
			mGridView.invalidateViews();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mImageList.getmImages().size();
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

			if (mImageList != null) {
				if (mImageList.getmImages().get(position).getmImageId() != null) {
					String image = mImageList.getmImages().get(position)
							.getmGuid()
							+ mImageSizes.getThumb();
					itemView.setImageItem(image, getActivity());
					itemView.listRef = mImageList.getmImages().get(position);
				} else {
					/*Uri temp = Uri.parse(mImageList.getmImages().get(position)
							.getmGuid()); // mediaList.get(position);
					Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
							mContext.getContentResolver(),
							ContentUris.parseId(temp),
							MediaStore.Images.Thumbnails.MINI_KIND,
							(BitmapFactory.Options) null);
					itemView.imageview.setImageBitmap(bitmap);
					itemView.listRef = mImageList.getmImages().get(position);*/
					//itemView.imageview.setImageDrawable(getResources().getDrawable(R.drawable.progress_small_holo));	
					itemView.setLoading(true);
				}
			}
			if (selectedList.get(position))
				itemView.selectImage.setVisibility(View.VISIBLE);
			else
				itemView.selectImage.setVisibility(View.INVISIBLE);
			itemView.setTag(mImageList.getmImages().get(position).getmGuid());
			return itemView;
		}
	}

	public final class ModeCallback implements ActionMode.Callback {

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
			if (mImageList == null)
				return true;
			if (mImageList == null)
				return true;
			String deleteQuery = "?";

			for (int i = selectedList.size() - 1; i > -1; i--) {
				if (selectedList.get(i)) {
					selectedList.remove(i);
					deleteQuery += "imageids[]="
							+ mImageList.getmImages().get(i).getmImageId()
							+ "&";
					mImageList.getmImages().remove(i);
				}
			}

			if (deleteQuery.length() > 1) {
				deleteQuery = deleteQuery
						.substring(0, deleteQuery.length() - 1);
			}

			AuthenicationPackage authenicationPackage = new AuthenicationPackage();
			authenicationPackage.session = ((MainApplication) ((Activity) mContext)
					.getApplication()).getUser().getSession();
			mContext.startService(APIService.getDeleteMediaIntent(mContext,
					authenicationPackage, deleteQuery));

			mode.finish();
			return true;
		}

	};

	@Override
	public void onLogin(User user) {
		retrieveUserImages();
	}

	public MenuInflater getSupportMenuInflater() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
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
					new ModeCallback());
		}
		return false;
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
				intent.putExtra(IMAGE_URL, localURL.get(url));
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

	public final class GalleryOnScrollListener implements AbsListView.OnScrollListener
	{
		int mCurScrollState;
		
		
		//used to determine when to load more images
		@Override
		public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
			if ((arg1 + arg2) >= arg3 && arg2 < arg3
					&& mImageTransactionsInProgress == 0
					&& !mLastPage
				) {
				showLoading();
				AuthenicationPackage authenicationPackage = new AuthenicationPackage();
				authenicationPackage.session = ((MainApplication) ((Activity) mContext)
						.getApplication()).getUser().getSession();
				mImageTransactionsInProgress++;
				mContext.startService(APIService.userMediaIntent(mContext,
						authenicationPackage, "?limit=" + IMAGE_PAGE_SIZE
								+ "&offset=" + (IMAGE_PAGE_SIZE + mCurrentPage)));
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			mCurScrollState = scrollState;
		}
	
	}

}
