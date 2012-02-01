package com.ifixit.android.ifixit;

import java.util.ArrayList;

import org.apache.http.client.ResponseHandler;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DeviceListFragment extends ListFragment {
   public interface DeviceSelectedListener {
      public void onDeviceSelected(String device);
   }

   private static final String DEVICES_API_URL =
    "http://www.ifixit.com/api/0.1/areas/";
   private static final String RESPONSE = "RESPONSE";

   private DeviceSelectedListener deviceSelectedListener;
   private String[] deviceStrings;
   private ArrayList<Device> mDevices;

   private final Handler mDevicesHandler = new Handler() {
      public void handleMessage(Message message) {
         String response = message.getData().getString(RESPONSE);
         ArrayList<Device> devices = GuideJSONHelper.parseDevices(response);

         if (devices != null) {
            setDevices(devices);
         }
         else {
            Log.e("iFixit", "Devices is null (response: " + response + ")");
         }
      }
   };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // TODO: conditionalize this based on savedInstanceState
      getDeviceHierarchy();

      deviceStrings = getResources().getStringArray(R.array.devices);

      setListAdapter(ArrayAdapter.createFromResource(getActivity()
       .getApplicationContext(), R.array.devices,
       R.layout.device_list_fragment));
   }

   @Override
   public void onListItemClick(ListView l, View v, int position, long id) {
      deviceSelectedListener.onDeviceSelected(deviceStrings[position]);
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

   private void setDevices(ArrayList<Device> devices) {
      mDevices = devices;
   }
}
