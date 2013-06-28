package com.dozuki.ifixit.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.dozuki.ifixit.model.guide.GuideInfo;

import java.util.ArrayList;

public class GuideListAdapter extends BaseAdapter {
   private final Context mContext;
   private ArrayList<GuideInfo> mGuides;

   public GuideListAdapter(Context context, ArrayList<GuideInfo> guides) {
      mContext = context;
      setGuides(guides);
   }

   public void addGuides(ArrayList<GuideInfo> guides) {
      mGuides.addAll(guides);
   }

   public void setGuides(ArrayList<GuideInfo> guides) {
      mGuides = new ArrayList<GuideInfo>(guides);
   }

   public int getCount() {
      return mGuides.size();
   }

   public Object getItem(int position) {
      return mGuides.get(position);
   }

   public long getItemId(int position) {
      return position;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      GuideItemView itemView;

      if (convertView == null) {
         itemView = new GuideItemView(mContext);
      } else {
         itemView = (GuideItemView) convertView;
      }

      itemView.setGuideItem(mGuides.get(position));

      return itemView;
   }
}
