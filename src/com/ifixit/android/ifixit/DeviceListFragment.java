package com.ifixit.android.ifixit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class DeviceListFragment extends ListFragment {
   public interface DeviceSelectedListener {
      public void onDeviceSelected(Device device);
   }

   private static final String CURRENT_DEVICE = "CURRENT_DEVICE";

   private DeviceSelectedListener deviceSelectedListener;
   private Device mDevice;
   private DeviceListAdapter mDeviceAdapter;
   private Context mContext;

   public DeviceListFragment() {

   }

   public DeviceListFragment(Device device) {
      mDevice = device;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (savedInstanceState != null) {
         mDevice = (Device)savedInstanceState.getSerializable(
          CURRENT_DEVICE);
      }

      mDeviceAdapter = new DeviceListAdapter(mContext);
      setDevice(mDevice);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(CURRENT_DEVICE, mDevice);
   }

   @Override
   public void onListItemClick(ListView l, View v, int position, long id) {
      deviceSelectedListener.onDeviceSelected(
       mDevice.getChildren().get(position));
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);

      try {
         deviceSelectedListener = (DeviceSelectedListener)activity;
         mContext = (Context)activity;
      } catch (ClassCastException e) {
         throw new ClassCastException(activity.toString() +
          " must implement DeviceSelectedListener");
      }
   }

   private void setDevice(Device device) {
      mDevice = device;
      mDeviceAdapter.setDevice(mDevice);
      setListAdapter(mDeviceAdapter);
   }
}
