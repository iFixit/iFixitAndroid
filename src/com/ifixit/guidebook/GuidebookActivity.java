package com.ifixit.guidebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class GuidebookActivity extends Activity {
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      Intent intent = new Intent(this, GuideView.class);
      intent.putExtra("guideid", 1552);
      startActivity(intent);
   }
}
