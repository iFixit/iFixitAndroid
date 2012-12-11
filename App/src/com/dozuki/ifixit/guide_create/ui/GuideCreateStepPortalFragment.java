package com.dozuki.ifixit.guide_create.ui;

import java.util.List;

import android.R.color;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.ifixit.android.imagemanager.ImageManager;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public class GuideCreateStepPortalFragment extends SherlockFragment {
	private static int StepID = 0;
	private DragSortListView mDragListView;
	private ImageManager mImageManager;
	private StepAdapter mAdapter;
	private DragSortController mController;
	private RelativeLayout mAddStepBar;
	private RelativeLayout mEditIntroBar;
	private GuideCreateObject mGuide;
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


	public GuideCreateStepPortalFragment(GuideCreateObject guide) {
		super();
       mGuide = guide;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (mImageManager == null) {
			mImageManager = ((MainApplication) getActivity().getApplication())
					.getImageManager();
		}
		mAdapter = new StepAdapter(mGuide.getSteps());
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
				mGuide.getSteps().add(item);
				mDragListView.invalidateViews();
			}
		});
		mEditIntroBar = (RelativeLayout) view.findViewById(R.id.edit_intro_bar);
		mAddStepBar = (RelativeLayout) view.findViewById(R.id.add_step_bar);
		mEditIntroBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchGuideEditIntro();
			}
		});
		mNoStepsText = (TextView) view.findViewById(R.id.no_steps_text);

		if (mGuide.getSteps().isEmpty())
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
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			final int p = position;
			final GuideCreateStepObject stepObj = getItem(position);
			if (v != convertView && v != null) {
				final ViewHolder holder = new ViewHolder();

				TextView tv = (TextView) v
						.findViewById(R.id.step_title_textview);
				holder.stepsView = tv;
				holder.mToggleEdit = (ToggleButton) v
						.findViewById(R.id.step_item_toggle_edit);
				holder.mImageView = (ImageView) v
						.findViewById(R.id.guide_step_item_thumbnail);
				holder.mDeleteButton = (Button) v
						.findViewById(R.id.step_create_delete_button);

				holder.mEditButton = (Button) v
						.findViewById(R.id.step_create_edit_button);

				holder.mEditBar = (LinearLayout) v
						.findViewById(R.id.step_create_item_edit_section);

				v.setTag(holder);
			}
			final ViewHolder holder = (ViewHolder) v.getTag();
			boolean isEdit = getItem(position).getEditMode();
			holder.mToggleEdit.setOnCheckedChangeListener(null);
			holder.mToggleEdit.setChecked(isEdit);
			holder.mToggleEdit
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							stepObj.setEditMode(isChecked);
							setEditMode(isChecked, true, holder.mToggleEdit,
									holder.mEditBar);
							mDragListView.invalidateViews();
						}
					});
			holder.mDeleteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mGuide.deleteStep(stepObj);
					mDragListView.invalidateViews();
				}
			});
			holder.mEditButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					launchStepEdit(p);
				}
			});
			String step = getItem(position).getTitle();
			holder.stepsView.setText(step);
			mImageManager.displayImage("", getActivity(), holder.mImageView);
			setEditMode(isEdit, false, holder.mToggleEdit, holder.mEditBar);
			return v;
		}

		public void setEditMode(boolean isChecked, boolean animate,
				final ToggleButton mToggleEdit, final LinearLayout mEditBar) {
			if (isChecked) {
				if (animate) {
					Animation rotateAnimation = AnimationUtils.loadAnimation(
							getActivity().getApplicationContext(),
							R.anim.rotate_clockwise);

					mToggleEdit.startAnimation(rotateAnimation);

					Animation slideDownAnimation = AnimationUtils
							.loadAnimation(getActivity().getApplicationContext(),
									R.anim.slide_down);
					mEditBar.setVisibility(View.VISIBLE);
					mEditBar.startAnimation(slideDownAnimation);
				} else {
					mEditBar.setVisibility(View.VISIBLE);
				}

			} else {
				if (animate) {
					Animation rotateAnimation = AnimationUtils.loadAnimation(
							getActivity().getApplicationContext(),
							R.anim.rotate_counterclockwise);

					mToggleEdit.startAnimation(rotateAnimation);
					Animation slideUpAnimation = AnimationUtils
							.loadAnimation(getActivity().getApplicationContext(),
									R.anim.slide_up);
					slideUpAnimation
							.setAnimationListener(new AnimationListener() {

								@Override
								public void onAnimationEnd(Animation animation) {
									mEditBar.setVisibility(View.GONE);
								}

								@Override
								public void onAnimationRepeat(
										Animation animation) {
								}

								@Override
								public void onAnimationStart(Animation animation) {
								}
							});
					mEditBar.startAnimation(slideUpAnimation);
				} else {
					mEditBar.setVisibility(View.GONE);
				}
			}
		}
	}
	
	private void launchStepEdit(int curStep)
	{
		//GuideCreateStepsEditActivity
		Intent intent = new Intent(getActivity(), GuideCreateStepsEditActivity.class);
		intent.putExtra(GuideCreateStepsEditActivity.GuideKey, mGuide);
		intent.putExtra(GuideCreateStepsEditActivity.GuideStepKey, curStep);
		startActivityForResult(intent, GuideCreateStepsActivity.GUIDE_EDIT_STEP_REQUEST);
	}
	
	private void launchGuideEditIntro()
	{
		GuideIntroFragment newFragment = new GuideIntroFragment();
		newFragment.setGuideOBject(mGuide);
		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.guide_create_fragment_steps_container, newFragment);
		transaction.addToBackStack(null);
		transaction.commitAllowingStateLoss();	
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("StepPortalFragmetn", "hit fragment on activity result");
		
		if (requestCode == GuideCreateStepsActivity.GUIDE_EDIT_STEP_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				GuideCreateObject guide = (GuideCreateObject) data
						.getSerializableExtra(GuideCreateStepsEditActivity.GuideKey);
				if (guide != null) {
					Log.i("StepPortalFragmetn", "non null guide update");
					mGuide = guide;
					mAdapter = new StepAdapter(mGuide.getSteps());
					mDragListView.setAdapter(mAdapter);
					mDragListView.invalidateViews();
				}
			}
		}
	}

}
