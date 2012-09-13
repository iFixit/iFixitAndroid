package com.dozuki.ifixit.view.ui;

import java.util.ArrayList;

import android.R;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MediaAdapter extends BaseAdapter {

	ArrayList<Uri> mediaList;
	private GridView _viewRef;
	private Context _context;

	public MediaAdapter(Context c, GridView viewRef) {
		_context = c;
		_viewRef = viewRef;
		mediaList = new ArrayList<Uri>();
	}

	public void setMediaList(ArrayList<Uri> medList) {
		mediaList = medList;
		_viewRef.invalidateViews();
	}

	public ArrayList<Uri> getMediaList() {
		return mediaList;
	}

	public Uri getImageAt(int i) {
		return mediaList.get(i);
	}

	public void addUri(Uri uri) {
		mediaList.add(uri);
		_viewRef.invalidateViews();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mediaList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView thumb;
		if (convertView == null) {
			thumb = new ImageView(_context);
			thumb.setLayoutParams(new GridView.LayoutParams(256, 256));
			thumb.setPadding(8, 8, 8, 8);
		} else {
			thumb = (ImageView) convertView;
		}
		Uri temp = mediaList.get(position);
		Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
				_context.getContentResolver(), ContentUris.parseId(temp),
				MediaStore.Images.Thumbnails.MINI_KIND,
				(BitmapFactory.Options) null);
		thumb.setImageBitmap(bitmap);
		return thumb;
	}
}
