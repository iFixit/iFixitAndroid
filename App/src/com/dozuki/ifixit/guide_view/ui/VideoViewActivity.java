package com.dozuki.ifixit.guide_view.ui;

import com.actionbarsherlock.view.Window;
import com.dozuki.ifixit.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoViewActivity extends Activity {

	protected static final String VIDEO_URL = "VIDEO_URL";
	private String mVideoUrl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature((int) Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.video_view);
		Bundle extras = getIntent().getExtras();
		mVideoUrl = (String) extras.get(VIDEO_URL);
		VideoView videoView = (VideoView) findViewById(R.id.video_view);
		MediaController mc = new MediaController(this);
		videoView.setMediaController(mc);

		Uri uri = Uri.parse(mVideoUrl);

		videoView.setVideoURI(uri);

		videoView.requestFocus();
		videoView.start();

	}

}
