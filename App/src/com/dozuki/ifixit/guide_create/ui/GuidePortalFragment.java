package com.dozuki.ifixit.guide_create.ui;

import org.holoeverywhere.app.Fragment;


import android.os.Bundle;
import org.holoeverywhere.LayoutInflater;

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
import com.dozuki.ifixit.util.APIEvent;
import com.ifixit.android.imagemanager.ImageManager;
import com.squareup.otto.Subscribe;

public class GuidePortalFragment extends Fragment {
	private static final int NO_ID = -1;
   private static final String CURRENT_OPEN_ITEM = null;
   private ListView mGuideList;
	private ImageManager mImageManager;
	private GuideCreateListAdapter mGuideAdapter;
	private GuidePortalFragment mSelf;
	private GuideCreateActivity mParentRef;
	private RelativeLayout mAddGuideBar;
	private TextView mNoGuidesText;
	private int mCurOpenGuideObjectID;

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
		mCurOpenGuideObjectID = NO_ID;
		if(savedInstanceState != null) {
		   mCurOpenGuideObjectID = savedInstanceState.getInt(CURRENT_OPEN_ITEM);
		}
		
     // APIService.call((Activity) getActivity(),
       //  APIService.getUserGuidesAPICall(((MainApplication) getActivity().getApplication()).getUser().getUserId()));
	}

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.guide_create_portal, container,
				false);
		mGuideList = (ListView) view.findViewById(R.id.guide_create_listview);
		mGuideList.setAdapter(mGuideAdapter);
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
			mGuideList.invalidateViews();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
         GuideCreateListItem itemView = (GuideCreateListItem) convertView;
         GuideCreateObject listRef = mParentRef.getGuideList().get(position);
         itemView = new GuideCreateListItem(getActivity(), mImageManager, mSelf, listRef);
         itemView.setTag(listRef.getGuideid());
         itemView.setGuideObject(listRef);
        // itemView.setEditMode(listRef.getEditMode(), false);
         itemView.setGuideItem(listRef.getTitle(), "");
         return itemView;
		}
	}
	
	@Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putInt(CURRENT_OPEN_ITEM, mCurOpenGuideObjectID);
      super.onSaveInstanceState(savedInstanceState);
   }
   
	
   public void onItemSelected(int id, boolean sel) {
      if (!sel) {
         mCurOpenGuideObjectID = NO_ID;
         return;
      }

      if (mCurOpenGuideObjectID != NO_ID) {

         GuideCreateListItem view = ((GuideCreateListItem) mGuideList.findViewWithTag(mCurOpenGuideObjectID));
         if (view != null) {
            view.setChecked(false);
         }

         for (int i = 0; i < mParentRef.getGuideList().size(); i++) {

            if (mParentRef.getGuideList().get(i).getGuideid() == mCurOpenGuideObjectID) {
               mParentRef.getGuideList().get(i).setEditMode(false);
            }
         }
      }
      mCurOpenGuideObjectID = id;
   }
   
   public void invalidateViews() {
      mGuideList.invalidateViews();
   }
}
