package com.ifixit.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

public class DeviceViewFragment extends Fragment {
   private String mDevice;
   private TextView mDeviceText;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.device_view_fragment, container, false);

      mDeviceText = (TextView)view.findViewById(R.id.deviceName);

      return view;
   }

   public void setDevice(String device) {
      mDevice = device;

      mDeviceText.setText(mDevice);
   }
}

