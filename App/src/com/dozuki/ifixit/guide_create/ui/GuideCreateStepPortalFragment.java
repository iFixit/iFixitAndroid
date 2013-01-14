package com.dozuki.ifixit.guide_create.ui;

import java.util.List;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import org.holoeverywhere.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateObject;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.dozuki.ifixit.guide_view.model.GuideStep;
import com.dozuki.ifixit.util.APIEvent;
import com.dozuki.ifixit.util.APIService;
import com.ifixit.android.imagemanager.ImageManager;
import com.squareup.otto.Subscribe;

public class GuideCreateStepPortalFragment extends Fragment {
	public static int StepID = 0;
	private static int ANIMATION_DURATION = 300;
	private ListView mDragListView;
	private ImageManager mImageManager;
	private StepAdapter mAdapter;
	private TextView mAddStepBar;
	private TextView mEditIntroBar;
	private TextView mReorderStepsBar;
	private GuideCreateObject mGuide;
	private TextView mNoStepsText;

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
		//APIService.call((Activity) getActivity(), APIService.getGuideAPICall(mGuide.getGuideid()));
	}
	
	 @Subscribe
	   public void onGuideCreated(APIEvent.Guide event) {
	      if (!event.hasError()) {
	         for(GuideStep gs : event.getResult().getStepList())
	            mGuide.getSteps().add(new GuideCreateStepObject(gs));
	         mAdapter.notifyDataSetChanged();
	      } else {
	        
	      }
	   }

	   @Override
	   public void onResume() {
	      super.onResume();
	      MainApplication.getBus().register(this);
	   }

	   @Override
	   public void onPause() {
	      super.onPause();

	      MainApplication.getBus().unregister(this);
	   }


	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.guide_create_steps_portal,
				container, false);
		mDragListView = (ListView) view
				.findViewById(R.id.steps_portal_list);
		mAddStepBar = (TextView) view.findViewById(R.id.add_step_bar);
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
		mEditIntroBar = (TextView) view.findViewById(R.id.edit_intro_bar);
		mEditIntroBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchGuideEditIntro();
			}
		});
		mReorderStepsBar = (TextView) view.findViewById(R.id.reorder_steps_bar);
		mReorderStepsBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchStepReorder();
			}
		});
		
		mNoStepsText = (TextView) view.findViewById(R.id.no_steps_text);

		if (mGuide.getSteps().isEmpty())
			mNoStepsText.setVisibility(View.VISIBLE);
		mDragListView.setAdapter(mAdapter);
		return view;
	}

	private class ViewHolder {
		public TextView stepsView;
		public ToggleButton mToggleEdit;
		public TextView mDeleteButton;
		public TextView mEditButton;
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
				holder.mDeleteButton = (TextView) v
						.findViewById(R.id.step_create_item_delete);

				holder.mEditButton = (TextView) v
						.findViewById(R.id.step_create_item_edit);

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
							//mDragListView.invalidateViews();
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

      public void setEditMode(boolean isChecked, boolean animate, final ToggleButton mToggleEdit,
         final LinearLayout mEditBar) {
         if (isChecked) {
            if (animate) {
               Animation rotateAnimation =
                  AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_clockwise);

               mToggleEdit.startAnimation(rotateAnimation);

               // Creating the expand animation for the item
               ExpandAnimation expandAni = new ExpandAnimation(mEditBar, ANIMATION_DURATION);
               expandAni.setAnimationListener(new AnimationListener() {

                  @Override
                  public void onAnimationEnd(Animation animation) {
                     mDragListView.invalidateViews();
                  }

                  @Override
                  public void onAnimationRepeat(Animation animation) {
                     // TODO Auto-generated method stub

                  }

                  @Override
                  public void onAnimationStart(Animation animation) {

                  }
               });
               // Start the animation on the toolbar
               mEditBar.startAnimation(expandAni);
            } else {
               mEditBar.setVisibility(View.VISIBLE);
               ((LinearLayout.LayoutParams) mEditBar.getLayoutParams()).bottomMargin = 0;
            }

         } else {
            if (animate) {
               Animation rotateAnimation =
                  AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_counterclockwise);

               mToggleEdit.startAnimation(rotateAnimation);
               // Creating the expand animation for the item
               ExpandAnimation expandAni = new ExpandAnimation(mEditBar, ANIMATION_DURATION);
               mDragListView.invalidate();
               expandAni.setAnimationListener(new AnimationListener() {

                  @Override
                  public void onAnimationEnd(Animation animation) {
                     mDragListView.invalidateViews();
                     mDragListView.requestLayout();
                  }

                  @Override
                  public void onAnimationRepeat(Animation animation) {}

                  @Override
                  public void onAnimationStart(Animation animation) {

                  }
               });
               // Start the animation on the toolbar
               mEditBar.startAnimation(expandAni);
            } else {
               mEditBar.setVisibility(View.GONE);
               ((LinearLayout.LayoutParams) mEditBar.getLayoutParams()).bottomMargin = -50;
            }
         }
      }
   }

   private void launchStepEdit(int curStep) {
		// GuideCreateStepsEditActivity
		Intent intent = new Intent(getActivity(),
				GuideCreateStepsEditActivity.class);
		intent.putExtra(GuideCreateStepsEditActivity.GuideKey, mGuide);
		intent.putExtra(GuideCreateStepsEditActivity.GuideStepKey, curStep);
		startActivityForResult(intent,
				GuideCreateStepsActivity.GUIDE_EDIT_STEP_REQUEST);
	}

	private void launchGuideEditIntro() {
		GuideIntroFragment newFragment = new GuideIntroFragment();
		newFragment.setGuideOBject(mGuide);
		FragmentTransaction transaction = getActivity()
				.getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.guide_create_fragment_steps_container,
				newFragment);
		transaction.addToBackStack(null);
		transaction.commitAllowingStateLoss();
	}
	
	private void launchStepReorder()
	{
		GuideCreateStepReorderFragment newFragment = new GuideCreateStepReorderFragment();
		newFragment.setGuide(mGuide);
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
