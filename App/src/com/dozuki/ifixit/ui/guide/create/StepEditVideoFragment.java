package com.dozuki.ifixit.ui.guide.create;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.StepVideo;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Fragment;

public class StepEditVideoFragment extends Fragment {


   private Activity mContext;

   private ImageView mPoster;
   private StepVideo mVideo;

   /////////////////////////////////////////////////////
   // LIFECYCLE
   /////////////////////////////////////////////////////

   @Override
   public void onCreate(Bundle savedInstanceState) {
      mContext = (Activity) getActivity();

      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      View v = inflater.inflate(R.layout.guide_create_step_edit_video, container, false);

      mPoster = (ImageView) v.findViewById(R.id.step_edit_video_poster);

      return v;
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



   /////////////////////////////////////////////////////
   // NOTIFICATION LISTENERS
   /////////////////////////////////////////////////////


   /////////////////////////////////////////////////////
   // HELPERS
   /////////////////////////////////////////////////////


   public void setVideo(StepVideo video) {
      mVideo = video;
      mVideo.getThumbnail();
   }
}