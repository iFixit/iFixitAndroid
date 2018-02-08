package com.dozuki.ifixit.ui.guide.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.util.api.ApiSyncAdapter;

public class VideoViewActivity extends Activity {
   private static final String VIDEO_URL = "VIDEO_URL";
   private static final String IS_OFFLINE = "IS_OFFLINE";

   private VideoView mVideoView;
   private ProgressDialog mProgressDialog;

   public static Intent viewVideo(Context context, String url, boolean offline) {
      Intent intent = new Intent(context, VideoViewActivity.class);
      intent.putExtra(VIDEO_URL, url);
      intent.putExtra(IS_OFFLINE, offline);
      return intent;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      Bundle extras = getIntent().getExtras();
      String videoUrl = extras.getString(VIDEO_URL);
      boolean isOffline = extras.getBoolean(IS_OFFLINE);

      requestWindowFeature(Window.FEATURE_NO_TITLE);

      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

      setContentView(R.layout.video_view);

      mVideoView = (VideoView) findViewById(R.id.video_view);

      MediaController mc = new MediaController(this);
      mVideoView.setMediaController(mc);

      if (isOffline) {
         mVideoView.setVideoPath(ApiSyncAdapter.getOfflineMediaPath(videoUrl));
      } else {
         mVideoView.setVideoURI(Uri.parse(videoUrl));
      }

      mProgressDialog = ProgressDialog.show(this,
         getString(R.string.video_activity_progress_title),
         getString(R.string.video_activity_progress_body), true);

      mVideoView.setOnPreparedListener(new OnPreparedListener() {
         public void onPrepared(MediaPlayer mp) {
      
            if (mProgressDialog != null)
               mProgressDialog.dismiss();
            
            mVideoView.requestFocus();            
            mp.start();
            
         }
      });                  

      mVideoView.setOnCompletionListener(new OnCompletionListener() {
         MediaPlayer mMediaPlayer;
         
         @Override
         public void onCompletion(MediaPlayer mp) {
            mMediaPlayer = mp;
            AlertDialog.Builder restartDialog = new AlertDialog.Builder(VideoViewActivity.this);
            restartDialog.setTitle(getString(R.string.restart_video));
            restartDialog
               .setMessage(getString(R.string.restart_video_message))
               .setCancelable(true)
               .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                     // Reset the video player and restart the clip
                     mMediaPlayer.seekTo(0);
                     mMediaPlayer.start();
                  }})
               .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog,int id) {
                     // close the dialog box and go back to the guide step
                     dialog.cancel();
                     finish();
                  }
               });
            
            restartDialog.create().show();
          }
      });
   }

}
