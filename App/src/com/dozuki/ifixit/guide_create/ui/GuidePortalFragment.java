package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.MediaViewItem;
import com.ifixit.android.imagemanager.ImageManager;

public class GuidePortalFragment extends SherlockFragment {

	private GridView mGridView;
	private ImageManager mImageManager;
	private GuideCreateListAdapter mGuideAdapter;
	private GuidePortalFragment mSelf;
	private ArrayList<GuideCreateListItem> mGuideList;
	public static int GuideItemID = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (mImageManager == null) {
			mImageManager = ((MainApplication) getActivity().getApplication())
					.getImageManager();
		}
		mSelf = this;
		mGuideList = new ArrayList<GuideCreateListItem>();
		mGuideAdapter = new GuideCreateListAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.guide_create_portal, container,
				false);
		mGridView = (GridView) view.findViewById(R.id.guide_create_gridview);
		mGridView.setAdapter(mGuideAdapter);
		return view;
	}

	public void createGuide() {
		if (mGuideList == null)
			return;
		GuideCreateListItem temp = new GuideCreateListItem(getActivity(),
				mImageManager, mSelf, GuideItemID++);
		temp.setText("Test Title" + Math.random());
		mGuideList.add(temp);
		mGuideAdapter.invalidatedView();
	}

	public void deleteGuide(GuideCreateListItem item) {
		mGuideList.remove(item);
		Log.e("portalfrag", "remove guide id: " + item.getId() + " list size: " + mGuideList.size());
		mGuideAdapter.invalidatedView();
	}

	public class GuideCreateListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mGuideList.size();
		}

		@Override
		public Object getItem(int position) {
			return mGuideList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		 public void invalidatedView() {
	         mGridView.invalidateViews();
	      }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			GuideCreateListItem itemView = (GuideCreateListItem) convertView;
			if (convertView == null) {
				itemView = new GuideCreateListItem(getActivity(),
						mImageManager, mSelf, -1);
			}
			
			GuideCreateListItem listRef = mGuideList.get(position);
			itemView.setId(listRef.getId());
			itemView.setEditMode(listRef.editEnabled(), false);
			return itemView;
		}

	}
}
