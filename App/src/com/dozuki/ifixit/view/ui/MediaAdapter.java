package com.dozuki.ifixit.view.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class MediaAdapter extends BaseAdapter{

	private Context mContext;
	
	public MediaAdapter(Context c)
	{
		mContext = c;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 20;
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
		TextView text;
		if(convertView == null)
		{
			text = new TextView(mContext);
			text.setLayoutParams(new GridView.LayoutParams(85,85));
			text.setPadding(8, 8, 8, 8);
		}
		else
		{
			text = (TextView) convertView;
		}
		text.setText("iFixit");
		return text;
	}

}
