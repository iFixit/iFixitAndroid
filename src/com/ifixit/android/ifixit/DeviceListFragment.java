package com.ifixit.android.ifixit;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DeviceListFragment extends ListFragment {
   public interface DeviceSelectedListener {
      public void onDeviceSelected(String device);
   }

   private DeviceSelectedListener deviceSelectedListener;
   private String[] devices;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      devices = getResources().getStringArray(R.array.devices);

      setListAdapter(ArrayAdapter.createFromResource(getActivity()
       .getApplicationContext(), R.array.devices,
       R.layout.device_list_fragment));
   }

   @Override
   public void onListItemClick(ListView l, View v, int position, long id) {
      deviceSelectedListener.onDeviceSelected(devices[position]);
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);

      try {
         deviceSelectedListener = (DeviceSelectedListener)activity;
      } catch (ClassCastException e) {
         throw new ClassCastException(activity.toString() +
          " must implement DeviceSelectedListener");
      }
   }
}
