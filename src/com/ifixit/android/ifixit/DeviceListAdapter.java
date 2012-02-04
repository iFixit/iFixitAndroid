package com.ifixit.android.ifixit;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class DeviceListAdapter extends BaseAdapter {
   private Context mContext;
   private Device mDevice;

   public DeviceListAdapter(Context context) {
      mContext = context;
   }

   public void setDevice(Device device) {
      mDevice = device;
   }

   public int getCount() {
      return mDevice.getChildren().size();
   }

   public Object getItem(int position) {
      return mDevice.getChildren().get(position);
   }

   public long getItemId(int position) {
      return position;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      DeviceListRow deviceRow;

      if (convertView == null) {
         deviceRow = new DeviceListRow(mContext);
      }
      else {
         deviceRow = (DeviceListRow)convertView;
      }

      deviceRow.setDevice(mDevice.getChildren().get(position));

      return deviceRow;
   }
}
