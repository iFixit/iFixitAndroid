package com.dozuki.ifixit.guide_create.ui;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepBullet.BulletTypes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class ChooseBulletDialog extends SherlockDialogFragment implements
		OnClickListener {

	private int mStepIndex;
	
	public interface BulletDialogListener {
		void onFinishBulletDialog(int index, String color);
	}

	public ChooseBulletDialog() {
		// Empty constructor required for DialogFragment
	}
	
	public void setStepIndex(int indx)
	{
		mStepIndex = indx;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setStyle(STYLE_NO_TITLE, 0);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.guide_create_steps_bullet_popup,
				container);
		view.findViewById(R.id.bullet_dialog_color_black).setOnClickListener(
				this);
		view.findViewById(R.id.bullet_dialog_color_red)
				.setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_color_orange).setOnClickListener(
				this);
		view.findViewById(R.id.bullet_dialog_color_yellow).setOnClickListener(
				this);
		view.findViewById(R.id.bullet_dialog_color_white).setOnClickListener(
				this);
		view.findViewById(R.id.bullet_dialog_color_blue).setOnClickListener(
				this);
		view.findViewById(R.id.bullet_dialog_color_purple).setOnClickListener(
				this);
		view.findViewById(R.id.bullet_dialog_color_teal).setOnClickListener(
				this);
		view.findViewById(R.id.bullet_dialog_caution).setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_note).setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_reminder).setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_indent).setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_unindent).setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_rearrange)
				.setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_cancel).setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View v) {
		BulletDialogListener activity = (BulletDialogListener) getActivity();
		switch (v.getId()) {
		case R.id.bullet_dialog_color_black:
			activity.onFinishBulletDialog(mStepIndex,"black");
			break;
		case R.id.bullet_dialog_color_red:
			activity.onFinishBulletDialog(mStepIndex,"red");
			break;

		case R.id.bullet_dialog_color_orange:
			activity.onFinishBulletDialog(mStepIndex,"orange");
			break;
		case R.id.bullet_dialog_color_yellow:
			activity.onFinishBulletDialog(mStepIndex,"yellow");
			break;
		case R.id.bullet_dialog_color_white:
			activity.onFinishBulletDialog(mStepIndex,"white");

			break;
		case R.id.bullet_dialog_color_blue:
			activity.onFinishBulletDialog(mStepIndex,"blue");
			break;
		case R.id.bullet_dialog_color_purple:
			activity.onFinishBulletDialog(mStepIndex,"purple");
			break;
		case R.id.bullet_dialog_color_teal:
			activity.onFinishBulletDialog(mStepIndex,"teal");
			break;
		case R.id.bullet_dialog_caution:
			activity.onFinishBulletDialog(mStepIndex,"icon_caution");
			break;
		case R.id.bullet_dialog_note:
			activity.onFinishBulletDialog(mStepIndex,"icon_note");
			break;
		case R.id.bullet_dialog_reminder:
			activity.onFinishBulletDialog(mStepIndex,"icon_reminder");
			break;
		case R.id.bullet_dialog_indent:
			activity.onFinishBulletDialog(mStepIndex,"action_indent");
			break;
		case R.id.bullet_dialog_unindent:
			activity.onFinishBulletDialog(mStepIndex,"action_unindent");
			break;
		case R.id.bullet_dialog_rearrange:
			activity.onFinishBulletDialog(mStepIndex,"action_reorder");
			break;
		case R.id.bullet_dialog_cancel:
			break;
		}
		this.dismiss();
	}
}