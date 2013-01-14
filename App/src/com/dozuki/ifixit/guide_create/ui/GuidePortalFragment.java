package com.dozuki.ifixit.guide_create.ui;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;


import android.os.Bundle;
import org.holoeverywhere.LayoutInflater;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import org.holoeverywhere.widget.ListView;
import android.widget.RelativeLayout;
import org.holoeverywhere.widget.TextView;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.topic_view.ui.TopicsActivity;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.ifixit.android.imagemanager.ImageManager;
import com.squareup.otto.Subscribe;

public class GuidePortalFragment extends Fragment {
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
     // APIService.call((Activity) getActivity(),
       //  APIService.getUserGuidesAPICall(((MainApplication) getActivity().getApplication()).getUser().getUserId()));
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
   
   @Subscribe
   public void onUserGuides(APIEvent.UserGuides event) {
      if (!event.hasError()) {
         mParentRef.getGuideList().removeAll(event.getResult());
         mParentRef.getGuideList().addAll(event.getResult());
         mGuideAdapter.notifyDataSetChanged();
      } else {
         //TODO
       //  APIService.getErrorDialog(TopicsActivity.this, event.getError(), APIService.getCategoriesAPICall()).show();
      }
   }
   
   
   @Subscribe
   public void onGuideCreated(APIEvent.CreateGuide event) {
      if (!event.hasError()) {
         mParentRef.getGuideList().add(event.getResult());
         mGuideAdapter.notifyDataSetChanged();
      } else {
         //TODO
       //  APIService.getErrorDialog(TopicsActivity.this, event.getError(), APIService.getCategoriesAPICall()).show();
      }
   }

   @Override
   public void onResume() {
      super.onResume();
      mGuideAdapter.invalidatedView();
      MainApplication.getBus().register(this);
   }

   @Override
   public void onPause() {
      super.onPause();

      MainApplication.getBus().unregister(this);
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
