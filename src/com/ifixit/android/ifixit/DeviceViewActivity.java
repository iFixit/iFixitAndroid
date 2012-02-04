package com.ifixit.android.ifixit;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class DeviceViewActivity extends FragmentActivity {
   public static final String DEVICE_KEY = "DEVICE";

   private Device mDevice;
   private DeviceViewFragment mDeviceView;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (getResources().getConfiguration().orientation ==
       Configuration.ORIENTATION_LANDSCAPE) {
         finish();
         return;
      }

      setContentView(R.layout.device_view);
      mDeviceView = (DeviceViewFragment)getSupportFragmentManager()
       .findFragmentById(R.id.device_view_fragment);
      mDevice = (Device)getIntent().getSerializableExtra(DEVICE_KEY);

      mDeviceView.setDevice(mDevice);
   }
}
