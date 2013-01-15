package com.dozuki.ifixit.guide_create.ui;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.DialogFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.guide_create.model.GuideCreateStepBullet.BulletTypes;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;

public class ChooseBulletDialog extends DialogFragment implements
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
		view.findViewById(R.id.bullet_dialog_color_green).setOnClickListener(
				this);
		view.findViewById(R.id.bullet_dialog_color_blue).setOnClickListener(
				this);
		view.findViewById(R.id.bullet_dialog_color_purple).setOnClickListener(
				this);
		//view.findViewById(R.id.ic_dialog_bullet_pink).setOnClickListener(
			//	this);
		view.findViewById(R.id.bullet_dialog_caution).setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_note).setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_reminder).setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_indent).setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_unindent).setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_rearrange)
				.setOnClickListener(this);
		view.findViewById(R.id.bullet_dialog_cancel).setOnClickListener(this);
		
		LayoutParams params = getDialog().getWindow().getAttributes();
      params.width = LayoutParams.WRAP_CONTENT;
      getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
      getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
		return view;
	}

	@Override
	public void onClick(View v) {
		BulletDialogListener frag = (BulletDialogListener) getTargetFragment();
		switch (v.getId()) {
		case R.id.bullet_dialog_color_black:
			frag.onFinishBulletDialog(mStepIndex,"black");
			break;
		case R.id.bullet_dialog_color_red:
			frag.onFinishBulletDialog(mStepIndex,"red");
			break;

		case R.id.bullet_dialog_color_orange:
			frag.onFinishBulletDialog(mStepIndex,"orange");
			break;
		case R.id.bullet_dialog_color_yellow:
			frag.onFinishBulletDialog(mStepIndex,"yellow");
			break;
		//case R.id.bullet_dialog_color_white:
		//	frag.onFinishBulletDialog(mStepIndex,"black");

		//	break;
		case R.id.bullet_dialog_color_blue:
			frag.onFinishBulletDialog(mStepIndex,"blue");
			break;
		case R.id.bullet_dialog_color_purple:
			frag.onFinishBulletDialog(mStepIndex,"purple");
			break;
		case R.id.bullet_dialog_color_green:
			frag.onFinishBulletDialog(mStepIndex,"teal");
			break;
		case R.id.bullet_dialog_caution:
			frag.onFinishBulletDialog(mStepIndex,"icon_caution");
			break;
		case R.id.bullet_dialog_note:
			frag.onFinishBulletDialog(mStepIndex,"icon_note");
			break;
		case R.id.bullet_dialog_reminder:
			frag.onFinishBulletDialog(mStepIndex,"icon_reminder");
			break;
		case R.id.bullet_dialog_indent:
			frag.onFinishBulletDialog(mStepIndex,"action_indent");
			break;
		case R.id.bullet_dialog_unindent:
			frag.onFinishBulletDialog(mStepIndex,"action_unindent");
			break;
		case R.id.bullet_dialog_rearrange:
			frag.onFinishBulletDialog(mStepIndex,"action_reorder");
			break;
		case R.id.bullet_dialog_cancel:
			break;
		}
		this.dismiss();
	}
}