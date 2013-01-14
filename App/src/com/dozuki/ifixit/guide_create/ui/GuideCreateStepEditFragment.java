package com.dozuki.ifixit.guide_create.ui;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import org.holoeverywhere.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout.LayoutParams;

import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.FrameLayout;
import org.holoeverywhere.widget.TextView;

import android.widget.ImageView;
import org.holoeverywhere.widget.ListView;

import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.ui.GalleryActivity;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepBullet;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepObject;
import com.dozuki.ifixit.guide_view.model.StepImage;
import com.dozuki.ifixit.guide_view.model.StepLine;
import com.ifixit.android.imagemanager.ImageManager;



public class GuideCreateStepEditFragment extends Fragment implements
		OnClickListener, OnLongClickListener {
	public static final int FetchImageKey = 1;
	public static final String ThumbPositionKey = "ThumbPositionKey";
	private static final String GuideEditKey = "GuideEditKey";
	GuideCreateStepObject mStepObject;
	EditText mStepTitle;
	ImageManager mImageManager;
	ImageView mLargeImage;
	ImageView mImageOne;
	ImageView mImageTwo;
	ImageView mImageThree;
	ImageView mMediaIcon;
	ListView mBulletList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageManager = ((MainApplication) getActivity().getApplication())
				.getImageManager();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.guide_create_step_edit_body,
				container, false);
		mStepTitle = (EditText) view.findViewById(R.id.step_edit_title_text);

		mLargeImage = (ImageView) view.findViewById(R.id.step_edit_large_image);
		mImageOne = (ImageView) view.findViewById(R.id.step_edit_thumb_1);
		mImageOne.setOnLongClickListener(this);
		mImageOne.setOnClickListener(this);
		mImageTwo = (ImageView) view.findViewById(R.id.step_edit_thumb_2);
		mImageTwo.setOnLongClickListener(this);
		mImageTwo.setOnClickListener(this);
		mImageThree = (ImageView) view.findViewById(R.id.step_edit_thumb_3);
		mImageThree.setOnLongClickListener(this);
		mImageThree.setOnClickListener(this);

		mMediaIcon = (ImageView) view.findViewById(R.id.step_edit_thumb_media);
		mBulletList = (ListView) view.findViewById(R.id.steps_portal_list);

		if (savedInstanceState != null) {
			mStepObject = (GuideCreateStepObject) savedInstanceState
					.getSerializable(GuideCreateStepEditFragment.GuideEditKey);
			for (StepImage img : mStepObject.getImages()) {
				setImage(img.getImageid(), img.getText());
			}
		}

		mBulletList.setAdapter(new BulletListAdapter(this.getActivity(),
				R.layout.guide_create_step_edit_list_item, mStepObject
						.getLines()));

		mStepTitle.setText(mStepObject.getTitle());
		mStepTitle.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Log.i("GuideCreateStepEditFragment", "GuideTitle changed to: "
						+ s.toString());
				mStepObject.setTitle(s.toString());
			}

		});
		return view;
	}

	public void setStepObject(GuideCreateStepObject stepObject) {
		mStepObject = stepObject;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable(
				GuideCreateStepEditFragment.GuideEditKey, mStepObject);
	}

	@Override
	public void onClick(View v) {
		String microURL = null;
		switch (v.getId()) {
		case R.id.step_edit_thumb_1:
			microURL = (String) mImageOne.getTag();
			break;
		case R.id.step_edit_thumb_2:
			microURL = (String) mImageTwo.getTag();
			break;
		case R.id.step_edit_thumb_3:
			microURL = (String) mImageThree.getTag();
			break;
		case R.id.step_edit_thumb_media:
			break;
		default:
			return;
		}
		if (microURL != null) {
			mImageManager.displayImage(microURL, getActivity(), mLargeImage);
			mLargeImage.invalidate();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case FetchImageKey:
			if (resultCode == Activity.RESULT_OK) {
				String mediaURL = data
						.getStringExtra(GuideCreateStepsEditActivity.MediaReturnKey);
				int mediaSlot = data.getIntExtra(
						GuideCreateStepsEditActivity.MediaSlotReturnKey, -1);
				Log.i("editstep", mediaSlot + "");
				setImage(mediaSlot, mediaURL);
			}
			break;
		}
	}

	private void setImage(int location, String url) {
		switch (location) {
		case 1:
			mImageManager.displayImage(url, getActivity(), mImageOne);
			mImageOne.setTag(url);
			mImageOne.invalidate();
			break;
		case 2:
			mImageManager.displayImage(url, getActivity(), mImageTwo);
			mImageTwo.setTag(url);
			mImageTwo.invalidate();
			break;
		case 3:
			mImageManager.displayImage(url, getActivity(), mImageThree);
			mImageThree.setTag(url);
			mImageThree.invalidate();
			break;
		default:
			return;
		}
		mImageManager.displayImage(url, getActivity(), mLargeImage);
		mLargeImage.invalidate();
		mStepObject.getImages().get(location - 1).setText(url);
	}

	@Override
	public boolean onLongClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.step_edit_thumb_1:
			intent = new Intent(getActivity(), GalleryActivity.class);
			intent.putExtra(ThumbPositionKey, 1);
			startActivityForResult(intent, FetchImageKey);
			break;
		case R.id.step_edit_thumb_2:
			intent = new Intent(getActivity(), GalleryActivity.class);
			intent.putExtra(ThumbPositionKey, 2);
			startActivityForResult(intent, FetchImageKey);
			break;
		case R.id.step_edit_thumb_3:
			intent = new Intent(getActivity(), GalleryActivity.class);
			intent.putExtra(ThumbPositionKey, 3);
			startActivityForResult(intent, FetchImageKey);
			break;
		case R.id.step_edit_thumb_media:
			break;
		}
		return true;
	}

	private class BulletListAdapter extends ArrayAdapter<StepLine> {

		private ArrayList<StepLine> items;
		private Context con;

		public BulletListAdapter(Context context, int textViewResourceId,
				ArrayList<StepLine> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			con = context;
		}

		@Override
		public int getCount() {
			return items.size() + 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) con
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.guide_create_step_edit_list_item, null);
			}

			if (position == items.size()) {
				ImageView newItem = (ImageView) v
						.findViewById(R.id.add_new_bullet);
				v.findViewById(R.id.guide_step_item_thumbnail).setVisibility(
						View.GONE);
				v.findViewById(R.id.step_title_textview).setVisibility(
						View.GONE);
				newItem.setVisibility(View.VISIBLE);
				newItem.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mStepObject.addLine(new StepLine("black", 0,
								"Test Step"));
						((GuideCreateStepsEditActivity) getActivity())
								.invalidateStepAdapter();
					}
				});
				return v;
			}
			final int mPos = position;
			FrameLayout iconFrame = (FrameLayout)v.findViewById(R.id.guide_step_item_frame);
			iconFrame.setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							final Dialog dialog = new Dialog(con);
							dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
							FragmentManager fm = getActivity()
									.getSupportFragmentManager();
							ChooseBulletDialog chooseBulletDialog = new ChooseBulletDialog();
							chooseBulletDialog.setStepIndex(mPos);
							chooseBulletDialog.show(fm,
									"fragment_choose_bullet");
						}

					});
			LayoutParams params = (LayoutParams)iconFrame.getLayoutParams();
			params.setMargins(25 * items.get(position).getLevel(), 0, 0, 0);
			iconFrame.setLayoutParams(params);
			EditText text = (EditText) v.findViewById(R.id.step_title_textview);
			text.setText(items.get(position).getText());
			ImageView icon = (ImageView) v
					.findViewById(R.id.guide_step_item_thumbnail);
			icon.setImageResource(getBulletResource(items.get(position).getColor()));
			return v;
		}

		public int getBulletResource(String color) {
			int iconRes;

			if (color.equals("black")) {
				iconRes = R.drawable.bullet_black;
			} else if (color.equals("orange")) {
				iconRes = R.drawable.bullet_orange;
			} else if (color.equals("blue")) {
				iconRes = R.drawable.bullet_blue;
			} else if (color.equals("purple")) {
				iconRes = R.drawable.bullet_purple;
			} else if (color.equals("red")) {
				iconRes = R.drawable.bullet_red;
			} else if (color.equals("teal")) {
				iconRes = R.drawable.bullet_teal;
			} else if (color.equals("white")) {
				iconRes = R.drawable.bullet_white;
			} else if (color.equals("yellow")) {
				iconRes = R.drawable.bullet_yellow;
			} else if (color.equals("icon_reminder")) {
				iconRes = R.drawable.ic_dialog_bullet_reminder_dark;
			} else if (color.equals("icon_caution")) {
				iconRes = R.drawable.ic_dialog_bullet_caution;
			} else if (color.equals("icon_note")) {
				iconRes = R.drawable.ic_dialog_bullet_note_dark;
			} else {
				iconRes = R.drawable.bullet_black;
			}

			return iconRes;
		}
	}
}
