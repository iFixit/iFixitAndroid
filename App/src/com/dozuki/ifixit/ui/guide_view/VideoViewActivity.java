package com.dozuki.ifixit.ui.guide_view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;
import com.actionbarsherlock.view.Window;
import com.dozuki.ifixit.R;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.ProgressDialog;

public class VideoViewActivity extends Activity {

   protected static final String VIDEO_URL = "VIDEO_URL";
   private String mVideoUrl;
   private VideoView mVideoView;
   private ProgressDialog mProgressDialog;
   private Context mContext;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      mContext = this;
      requestWindowFeature((int) Window.FEATURE_NO_TITLE);

      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

      setContentView(R.layout.video_view);

      mVideoView = (VideoView) findViewById(R.id.video_view);

      Bundle extras = getIntent().getExtras();
      mVideoUrl = (String) extras.get(VIDEO_URL);

      MediaController mc = new MediaController(this);
      mVideoView.setMediaController(mc);

      mVideoView.setVideoURI(Uri.parse(mVideoUrl));

      mProgressDialog = ProgressDialog.show(mContext, 
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
            AlertDialog.Builder restartDialog = new AlertDialog.Builder(mContext);
            restartDialog.setTitle("Restart Video");
            restartDialog
               .setMessage("Play this video again?")
               .setCancelable(true)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                     // Reset the video player and restart the clip
                     mMediaPlayer.seekTo(0);
                     mMediaPlayer.start();
                  }})
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
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
