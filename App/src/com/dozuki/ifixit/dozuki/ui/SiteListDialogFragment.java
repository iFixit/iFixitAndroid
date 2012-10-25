package com.dozuki.ifixit.dozuki.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.WazaBe.HoloEverywhere.app.Dialog;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.dozuki.model.Site;
import com.dozuki.ifixit.view.ui.TopicsActivity;

import java.util.ArrayList;

public class SiteListDialogFragment extends DialogFragment {
    
   private ListView mSiteListView;
   private SiteListAdapter mSiteListAdapter;
   private static ArrayList<Site> mSites;
    
   static SiteListDialogFragment newInstance(ArrayList<Site> sites) {
      SiteListDialogFragment f = new SiteListDialogFragment();
      mSites = sites;
      return f;
   }
    
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      getDialog().setTitle(R.string.site_list_title);
      
      View v = inflater.inflate(R.layout.site_dialog_list, container, false);
        
      mSiteListView = (ListView)v.findViewById(R.id.siteListView);
      
      mSiteListAdapter = new SiteListAdapter(mSites);
      mSiteListView.setAdapter(mSiteListAdapter);

      mSiteListView.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> arg0, View view, int position,
          long id) {
            MainApplication application = 
             (MainApplication)getActivity().getApplication();
            Intent intent = new Intent(getActivity().getBaseContext(), 
             TopicsActivity.class);
            application.setSite(mSiteListAdapter.getSiteList().get(position));
             startActivity(intent);
         }
      });

      return v;
   }
}

