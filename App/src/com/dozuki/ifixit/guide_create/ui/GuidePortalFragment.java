package com.dozuki.ifixit.guide_create.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.ifixit.android.imagemanager.ImageManager;

public class GuidePortalFragment extends SherlockFragment {
	private ListView mGridView;
	private ImageManager mImageManager;
	private GuideCreateListAdapter mGuideAdapter;
	private GuidePortalFragment mSelf;
	private GuideCreateActivity mParentRef;
	private RelativeLayout mAddGuideBar;
	private TextView mNoGuidesText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (mImageManager == null) {
			mImageManager = ((MainApplication) getActivity().getApplication())
					.getImageManager();
		}
		mSelf = this;
		mParentRef = (GuideCreateActivity) getActivity();
		mGuideAdapter = new GuideCreateListAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.guide_create_portal, container,
				false);
		mGridView = (ListView) view.findViewById(R.id.guide_create_listview);
		mGridView.setAdapter(mGuideAdapter);
		mNoGuidesText = (TextView) view.findViewById(R.id.no_guides_text);
		if (mParentRef.getGuideList().isEmpty())
			mNoGuidesText.setVisibility(View.VISIBLE);
		mAddGuideBar = (RelativeLayout) view.findViewById(R.id.add_guide_bar);
		mAddGuideBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mNoGuidesText.getVisibility() == View.VISIBLE)
					mNoGuidesText.setVisibility(View.GONE);
				mParentRef.createGuide();
			}
		});
		return view;
	}
	
	

	@Override
	public void onResume() {
		super.onResume();
		mGuideAdapter.invalidatedView();
	}

	public void deleteGuide(GuideCreateObject item) {
		mParentRef.getGuideList().remove(item);
		mGuideAdapter.invalidatedView();
	}

	public class GuideCreateListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mParentRef.getGuideList().size();
		}

		@Override
		public Object getItem(int position) {
			return mParentRef.getGuideList().get(position);
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
			GuideCreateObject listRef = mParentRef.getGuideList().get(position);
			if (convertView == null) {
				itemView = new GuideCreateListItem(getActivity(),
						mImageManager, mSelf, listRef);
			}
			itemView.setGuideObject(listRef);
			itemView.setEditMode(listRef.getEditMode(), false);
			itemView.setGuideItem(listRef.getTitle(), "");
			return itemView;
		}
	}
}
