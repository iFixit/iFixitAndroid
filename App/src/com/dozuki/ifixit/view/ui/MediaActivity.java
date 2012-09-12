package com.dozuki.ifixit.view.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;  
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.R;

public class MediaActivity extends SherlockFragmentActivity {
	private static final int SELECT_PICTURE = 1;
	private static final int CAMERA_PIC_REQUEST = 2;
	private String selectedImagePath;
	private String filemanagerstring;
	GridView mGridView;
	MediaAdapter galleryAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.media);
	

		Log.i("mediaact", "oncreate!");
		mGridView = (GridView) findViewById(R.id.gridview);
		galleryAdapter = new MediaAdapter(this, mGridView);
		mGridView.setAdapter(galleryAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Toast.makeText(MediaActivity.this, "" + position,
						Toast.LENGTH_SHORT).show();
			}
		});

		((Button) findViewById(R.id.gallery_button))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(
								Intent.createChooser(intent, "Select Picture"),
								SELECT_PICTURE);
					}
				});

		((Button) findViewById(R.id.camera_button))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						Intent cameraIntent = new Intent(
								android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
					}
				});
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  ArrayList<Uri> mArr = galleryAdapter.getMediaList();
	  String arr[] = new String[mArr.size()];
	  for(int i = 0 ; i < arr.length ; i++)
	  {
		  arr[i] = mArr.get(i).getEncodedPath();
	  }
	  savedInstanceState.putStringArray("URIs", arr);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  String arr[] = savedInstanceState.getStringArray("URIs");
	  ArrayList<Uri> uriArr = new ArrayList<Uri>();
	  for(int i = 0 ; i < arr.length ; i++)
	  {
		  uriArr.add(Uri.parse(arr[i]));
	  }
	  galleryAdapter.setMediaList(uriArr);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu_bar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();

				galleryAdapter.addUri(selectedImageUri);
				/*
				 * // OI FILE Manager filemanagerstring =
				 * selectedImageUri.getPath();
				 * 
				 * // MEDIA GALLERY selectedImagePath =
				 * getPath(selectedImageUri);
				 * 
				 * // DEBUG PURPOSE - you can delete this if you want if
				 * (selectedImagePath != null)
				 * System.out.println(selectedImagePath); else
				 * System.out.println("selectedImagePath is null"); if
				 * (filemanagerstring != null)
				 * System.out.println(filemanagerstring); else
				 * System.out.println("filemanagerstring is null");
				 * 
				 * // NOW WE HAVE OUR WANTED STRING if (selectedImagePath !=
				 * null) System.out
				 * .println("selectedImagePath is the right one for you!"); else
				 * System.out
				 * .println("filemanagerstring is the right one for you!");
				 */
			} else if (requestCode == CAMERA_PIC_REQUEST) {
				// store returned image
				Log.i("mediact", "ret from camera");
				Uri selectedImageUri = data.getData();
				galleryAdapter.addUri(selectedImageUri);

				// Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			getSupportFragmentManager().popBackStack();
			return true;
		case R.id.guides_button:
			Intent i = new Intent(this, TopicsActivity.class);
			startActivityForResult(i, 0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
