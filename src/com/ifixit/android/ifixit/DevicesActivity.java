package com.ifixit.android.ifixit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class DevicesActivity extends FragmentActivity implements
 DeviceListFragment.DeviceSelectedListener {
   private boolean mDualPane;
   private DeviceViewFragment mDeviceView;
   private Device mDevice;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.devices);
      mDeviceView = (DeviceViewFragment)getSupportFragmentManager()
       .findFragmentById(R.id.device_view_fragment);
      mDualPane = mDeviceView != null && mDeviceView.isInLayout();
   }

   // TODO update to pass in device object (maybe)
   @Override
   public void onDeviceSelected(Device device) {
      mDevice = device;

      if (mDualPane) {
         mDeviceView.setDevice(mDevice.getName());
      }
      else {
         Intent intent = new Intent(this, DeviceViewActivity.class);
         Bundle bundle = new Bundle();

         bundle.putString("device", mDevice.getName());
         intent.putExtras(bundle);
         startActivity(intent);
      }
   }
}
