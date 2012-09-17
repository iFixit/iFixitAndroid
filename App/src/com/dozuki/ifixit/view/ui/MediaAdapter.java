package com.dozuki.ifixit.view.ui;

import java.util.ArrayList;

import android.R;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MediaAdapter extends BaseAdapter {

	ArrayList<Uri> mediaList;
	ArrayList<Boolean> checkedList;
	MediaFragment _mediaFragRef;
	private GridView _viewRef;
	private Context _context;
	private LayoutInflater _mInflater;

	public MediaAdapter(Context c, GridView viewRef, MediaFragment mediaFrag) {
		_context = c;
		_viewRef = viewRef;
		mediaList = new ArrayList<Uri>();
		checkedList = new ArrayList<Boolean>();
		_mInflater = (LayoutInflater) _context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_mediaFragRef = mediaFrag;
	}

	public void setMediaList(ArrayList<Uri> medList) {
		mediaList = medList;
		checkedList = new ArrayList<Boolean>(mediaList.size());
		for(int i = 0 ; i < mediaList.size() ; i++)
		{
			checkedList.add(false);
		}
		_viewRef.invalidateViews();
	}

	public void setCheckedList(ArrayList<Boolean> checkedList) {
		this.checkedList = checkedList;
	}
	
	public ArrayList<Boolean> getCheckedList()
	{
		return checkedList;
	}
	
	public boolean isChecked(int position)
	{
		return checkedList.get(position);
	}

	public ArrayList<Uri> getMediaList() {
		return mediaList;
	}

	public Uri getImageAt(int i) {
		return mediaList.get(i);
	}

	public void addUri(Uri uri) {
		mediaList.add(uri);
		checkedList.add(new Boolean(false));
		_viewRef.invalidateViews();
	}
	
	public void invalidatedView()
	{
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

	public class ViewHolder {
		ImageView imageview;
		CheckBox checkbox;
		int id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder cell;
		if (convertView == null) {
			cell = new ViewHolder();
			convertView = _mInflater.inflate(
					com.dozuki.ifixit.R.layout.media_cell, null);
			cell.imageview = (ImageView) convertView
					.findViewById(com.dozuki.ifixit.R.id.media_image);
			cell.checkbox = (CheckBox) convertView
					.findViewById(com.dozuki.ifixit.R.id.del_box);
			convertView.setTag(cell);
		} else {
			cell = (ViewHolder) convertView.getTag();
		}
		cell.checkbox.setId(position);
		cell.checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checkedList.set(cell.checkbox.getId(), isChecked);			
			}		
		});
		cell.imageview.setId(position);
		cell.imageview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageView img = (ImageView) v;
				_mediaFragRef.expandImage(img.getId());
			}
		});
		Uri temp = mediaList.get(position);
		Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
				_context.getContentResolver(), ContentUris.parseId(temp),
				MediaStore.Images.Thumbnails.MINI_KIND,
				(BitmapFactory.Options) null);
		cell.imageview.setImageBitmap(bitmap);
		cell.checkbox.setChecked(checkedList.get(position));
		cell.id = position;
		return convertView;
	}

	public View getViewOld(int position, View convertView, ViewGroup parent) {
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
