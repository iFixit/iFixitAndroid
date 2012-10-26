package com.dozuki.ifixit.dozuki.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.dozuki.ifixit.dozuki.model.Site;

import java.util.ArrayList;

public class SiteListAdapter extends BaseAdapter {
    private ArrayList<Site> mSites;

    public SiteListAdapter(ArrayList<Site> sites) {
       mSites = sites;
    }

    public ArrayList<Site> getSiteList() {
       return mSites;
    }

    public int getCount() {
       return mSites.size();
    }

    public Object getItem(int position) {
       return mSites.get(position);
    }

    public long getItemId(int position) {
       return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
       SiteRowView siteView = (SiteRowView)convertView;

       if (convertView == null) 
          siteView = new SiteRowView(parent.getContext());

       siteView.setSite(mSites.get(position));

       return siteView;
    }
}
