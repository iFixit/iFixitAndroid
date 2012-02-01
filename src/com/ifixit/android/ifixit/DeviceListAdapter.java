package com.ifixit.android.ifixit;

import java.util.ArrayList;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class DeviceListAdapter extends BaseAdapter {
   private Context mContext;
   private ArrayList<Device> mDevices;

   public DeviceListAdapter(Context context) {
      mContext = context;
   }

   public void setDevices(ArrayList<Device> devices) {
      mDevices = devices;
   }

   public int getCount() {
      return mDevices.size();
   }

   public Object getItem(int position) {
      return mDevices.get(position);
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

      deviceRow.setDevice(mDevices.get(position));

      return deviceRow;
   }
}
