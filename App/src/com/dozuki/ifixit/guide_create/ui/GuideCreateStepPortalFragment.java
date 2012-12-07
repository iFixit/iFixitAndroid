package com.dozuki.ifixit.guide_create.ui;

import java.util.List;

import android.R.color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

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
			mDragListView.invalidateViews();
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
		public ToggleButton mToggleEdit;
		public Button mDeleteButton;
		public Button mEditButton;
		public LinearLayout mEditBar;
		public ImageView mImageView;
	}

	private class StepAdapter extends ArrayAdapter<GuideCreateStepObject> {
		public StepAdapter(List<GuideCreateStepObject> list) {
			super(getActivity(), R.layout.guide_create_step_list_item,
					R.id.step_title_textview, list);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			final GuideCreateStepObject stepObj = getItem(position);
			if (v != convertView && v != null) {
				final ViewHolder holder = new ViewHolder();

				TextView tv = (TextView) v
						.findViewById(R.id.step_title_textview);
				holder.stepsView = tv;
				holder.mToggleEdit = (ToggleButton) v.findViewById(R.id.step_item_toggle_edit);
				holder.mImageView = (ImageView) v.findViewById(R.id.guide_step_item_thumbnail);
				holder.mDeleteButton = (Button) v.findViewById(R.id.step_create_delete_button);
				holder.mDeleteButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						mParentRef.deleteStep(stepObj);
						mDragListView.invalidateViews();
					}
					
				});
				holder.mEditButton = (Button) v.findViewById(R.id.step_create_edit_button);
				holder.mEditButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
					}
					
				});
				holder.mEditBar = (LinearLayout) v.findViewById(R.id.step_create_item_edit_section);
				holder.mToggleEdit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						stepObj.setEditMode(isChecked);
						setEditMode(isChecked, true, holder.mToggleEdit, holder.mEditBar);
						mDragListView.invalidateViews();
					}
				});
				v.setTag(holder);
			}
			ViewHolder holder = (ViewHolder) v.getTag();
			String step = getItem(position).getTitle();
			holder.stepsView.setText(step);
			boolean isEdit = getItem(position).getEditMode();
			holder.mToggleEdit.setChecked(isEdit);
			mImageManager.displayImage("", mParentRef, holder.mImageView);
			setEditMode(isEdit, false, holder.mToggleEdit, holder.mEditBar);
			return v;
		}
		
		public void setEditMode(boolean isChecked, boolean animate,final ToggleButton mToggleEdit,final LinearLayout mEditBar) {
			if (isChecked) {
				if (animate) {
					Animation rotateAnimation = AnimationUtils.loadAnimation(
							mParentRef.getApplicationContext(), R.anim.rotate_clockwise);

					mToggleEdit.startAnimation(rotateAnimation);
					
					Animation slideDownAnimation = AnimationUtils.loadAnimation(
							mParentRef.getApplicationContext(), R.anim.slide_down);
					mEditBar.setVisibility(View.VISIBLE);
					mEditBar.startAnimation(slideDownAnimation);
				}
				else
				{
					mEditBar.setVisibility(View.VISIBLE);
				}

			} else {
				if (animate) {
					Animation rotateAnimation = AnimationUtils.loadAnimation(
							mParentRef.getApplicationContext(), R.anim.rotate_counterclockwise);

					mToggleEdit.startAnimation(rotateAnimation);
					Animation slideUpAnimation = AnimationUtils.loadAnimation(mParentRef.getApplicationContext(),
							R.anim.slide_up);
					slideUpAnimation.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationEnd(Animation animation) {
							mEditBar.setVisibility(View.GONE);
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationStart(Animation animation) {
						}
					});
					mEditBar.startAnimation(slideUpAnimation);
				}
				else
				{
					mEditBar.setVisibility(View.GONE);
				}
			}
		}
	}
	
	
}
