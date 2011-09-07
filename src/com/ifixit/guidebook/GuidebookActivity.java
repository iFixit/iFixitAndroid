package com.ifixit.guidebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class GuidebookActivity extends Activity {
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      Intent intent = new Intent(this, GuideView.class);

      //intent.putExtra("guideid", 4531);

       intent.putExtra("guideid", 3550);
      // xbox RROD
      startActivity(intent);
   }
}
