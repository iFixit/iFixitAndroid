package com.dozuki.ifixit.guide_create.ui;

import java.util.List;

import android.R.color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.ifixit.android.imagemanager.ImageManager;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public class GuideCreateStepPortalFragment extends SherlockFragment {
	private static int StepID = 0;
	private DragSortListView mDragListView;
	private ImageManager mImageManager;
	private GuideCreateStepPortalFragment mSelf;
	private GuideCreateStepsActivity mParentRef;
	private StepAdapter mAdapter;
	private DragSortController mController;
	private RelativeLayout mAddStepBar;
	private TextView mNoStepsText;

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			GuideCreateStepObject item = mAdapter.getItem(from);

			mAdapter.remove(item);
			mAdapter.insert(item, to);
		}
	};

	private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
		@Override
		public void remove(int which) {
			mAdapter.remove(mAdapter.getItem(which));
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (mImageManager == null) {
			mImageManager = ((MainApplication) getActivity().getApplication())
					.getImageManager();
		}
		mSelf = this;
		mParentRef = (GuideCreateStepsActivity) getActivity();
		mAdapter = new StepAdapter(mParentRef.getStepList());
	}

	/**
	 * Called in onCreateView. Override this to provide a custom
	 * DragSortController.
	 */
	public DragSortController buildController(DragSortListView dslv) {
		// defaults are
		// dragStartMode = onDown
		// removeMode = flingRight
		DragSortController controller = new DragSortController(dslv);
		controller.setDragHandleId(R.id.drag_handle);
		// controller.setClickRemoveId(R.id.click_remove);
		controller.setRemoveEnabled(false);
		controller.setSortEnabled(true);
		controller.setDragInitMode(DragSortController.ON_DOWN);
		controller.setRemoveMode(DragSortController.FLING_RIGHT_REMOVE);
		controller.setBackgroundColor(color.background_light);
		return controller;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.guide_create_steps_portal,
				container, false);
		mDragListView = (DragSortListView) view
				.findViewById(R.id.steps_portal_list);
		mAddStepBar = (RelativeLayout) view.findViewById(R.id.add_step_bar);
		mAddStepBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mNoStepsText.getVisibility() == View.VISIBLE)
					mNoStepsText.setVisibility(View.GONE);
				GuideCreateStepObject item = new GuideCreateStepObject(StepID++);
				item.setTitle("Test Step " + StepID);
				mParentRef.getStepList().add(item);
				mDragListView.invalidateViews();
			}
		});
		mNoStepsText = (TextView) view.findViewById(R.id.no_steps_text);
		if (mParentRef.getStepList().isEmpty())
			mNoStepsText.setVisibility(View.VISIBLE);
		mDragListView.setDropListener(onDrop);
		mDragListView.setRemoveListener(onRemove);
		mDragListView.setAdapter(mAdapter);
		mController = buildController(mDragListView);
		mDragListView.setFloatViewManager(mController);
		mDragListView.setOnTouchListener(mController);
		mDragListView.setDragEnabled(true);
		return view;
	}

	private class ViewHolder {
		public TextView stepsView;
	}

	private class StepAdapter extends ArrayAdapter<GuideCreateStepObject> {
		public StepAdapter(List<GuideCreateStepObject> list) {
			super(getActivity(), R.layout.guide_create_step_list_item,
					R.id.step_title_textview, list);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			if (v != convertView && v != null) {
				ViewHolder holder = new ViewHolder();

				TextView tv = (TextView) v
						.findViewById(R.id.step_title_textview);
				holder.stepsView = tv;

				v.setTag(holder);
			}
			ViewHolder holder = (ViewHolder) v.getTag();
			String step = getItem(position).getTitle();
			holder.stepsView.setText(step);
			return v;
		}
	}
}
