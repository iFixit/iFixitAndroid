package com.ifixit.android.ifixit;

import java.util.ArrayList;
import org.apache.http.client.ResponseHandler;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class DevicesActivity extends FragmentActivity implements
 DeviceListFragment.DeviceSelectedListener {
   private static final String DEVICES_API_URL =
    "http://www.ifixit.com/api/0.1/areas/";
   private static final String RESPONSE = "RESPONSE";
   private static final String ROOT_DEVICE = "ROOT_DEVICE";

   private boolean mDualPane;
   private DeviceViewFragment mDeviceView;
   private Device mDevice;
   private boolean mFirstFragment = true;

   private final Handler mDevicesHandler = new Handler() {
      public void handleMessage(Message message) {
         String response = message.getData().getString(RESPONSE);
         ArrayList<Device> devices = GuideJSONHelper.parseDevices(response);

         if (devices != null) {
            mFirstFragment = true;
            mDevice = new Device("ROOT");
            mDevice.addAllDevices(devices);
            onDeviceSelected(mDevice);
         }
         else {
            Log.e("iFixit", "Devices is null (response: " + response + ")");
         }
      }
   };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.devices);
      mDeviceView = (DeviceViewFragment)getSupportFragmentManager()
       .findFragmentById(R.id.device_view_fragment);
      mDualPane = mDeviceView != null && mDeviceView.isInLayout();

      if (savedInstanceState != null) {
         mDevice = (Device)savedInstanceState.getSerializable(ROOT_DEVICE);
         //onDeviceSelected(device);
      } else {
         getDeviceHierarchy();
      }
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(ROOT_DEVICE, mDevice);
   }

   @Override
   public void onDeviceSelected(Device device) {
      if (device.isLeaf()) {
         if (mDualPane) {
            mDeviceView.setDevice(device);
         }
         else {
            Intent intent = new Intent(this, DeviceViewActivity.class);
            Bundle bundle = new Bundle();

            bundle.putSerializable(DeviceViewActivity.DEVICE_KEY, device);
            intent.putExtras(bundle);
            startActivity(intent);
         }
      } else {
         FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         DeviceListFragment newFragment = new DeviceListFragment(device);
         
         ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
          R.anim.slide_in_left, R.anim.slide_out_right);
         ft.replace(R.id.device_list_fragment, newFragment);

         if (!mFirstFragment) {
            ft.addToBackStack(null);
         }

         mFirstFragment = false;

         ft.commit();
      }
   }

   private void getDeviceHierarchy() {
      final ResponseHandler<String> responseHandler =
       HTTPRequestHelper.getResponseHandlerInstance(mDevicesHandler);

      new Thread() {
         public void run() {
            HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);

            helper.performGet(DEVICES_API_URL);
         }
      }.start();
   }
}
