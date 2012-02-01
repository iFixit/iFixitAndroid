package com.ifixit.android.ifixit;

import java.util.ArrayList;
import org.apache.http.client.ResponseHandler;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class DeviceListFragment extends ListFragment {
   public interface DeviceSelectedListener {
      public void onDeviceSelected(Device device);
   }

   private static final String DEVICES_API_URL =
    "http://www.ifixit.com/api/0.1/areas/";
   private static final String RESPONSE = "RESPONSE";

   private DeviceSelectedListener deviceSelectedListener;
   private ArrayList<Device> mDevices;
   private DeviceListAdapter mDeviceAdapter;
   private Context mContext;

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

      // Make empty list for now
      mDevices = new ArrayList<Device>();

      // TODO: conditionalize this based on savedInstanceState
      getDeviceHierarchy();
      mDeviceAdapter = new DeviceListAdapter(mContext);
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

   public void setDevices(ArrayList<Device> devices) {
      mDevices = devices;
      mDeviceAdapter.setDevices(mDevices);
      setListAdapter(mDeviceAdapter);
   }
}
