package com.ifixit.android.ifixit;

import java.util.ArrayList;
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

   private DeviceSelectedListener deviceSelectedListener;
   private ArrayList<Device> mDevices;
   private DeviceListAdapter mDeviceAdapter;
   private Context mContext;

   public DeviceListFragment() {

   }

   public DeviceListFragment(ArrayList<Device> devices) {
      mDevices = devices;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // TODO: conditionalize this based on savedInstanceState
      mDeviceAdapter = new DeviceListAdapter(mContext);
      if (mDevices != null) {
         setDevices(mDevices);
      }
   }

   @Override
   public void onListItemClick(ListView l, View v, int position, long id) {
      deviceSelectedListener.onDeviceSelected(mDevices.get(position));
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

   /**
    * Avoid calling this...
    * It's really only used for the first fragment that is already created and
    * just needs to be updated. The rest are updated through the constructor
    */
   public void setDevices(ArrayList<Device> devices) {
      mDevices = devices;
      mDeviceAdapter.setDevices(mDevices);
      setListAdapter(mDeviceAdapter);
   }
}
