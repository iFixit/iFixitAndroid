package com.ifixit.android.ifixit;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DeviceListRow extends LinearLayout {
   private TextView mDeviceName;
   private Device mDevice;

   public DeviceListRow(Context context) {
      super(context);      

      LayoutInflater inflater = (LayoutInflater)context.getSystemService(
       Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.device_list_row, this, true);        

      mDeviceName = (TextView)findViewById(R.id.device_title);
   }

   public void setDevice(Device device) {
      mDevice = device;
      mDeviceName.setText(mDevice.getName());
   }
}
