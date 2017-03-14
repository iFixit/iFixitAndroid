package com.dozuki.ifixit.ui.guide.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Item;

import java.util.ArrayList;

public class PartsToolsAdapter extends BaseAdapter {

   private ArrayList<Item> mItems;
   private Context mContext;

   public PartsToolsAdapter(Context context, ArrayList<Item> items) {
      mItems = new ArrayList<Item>(items);
      mContext = context;
   }

   @Override
   public int getCount() {
      return mItems.size();
   }

   @Override
   public Object getItem(int position) {
      return mItems.get(position);
   }

   @Override
   public long getItemId(int position) {
      return position;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      View row;

      if (convertView == null) {
         LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         row = inflater.inflate(R.layout.guide_part_tool_row, null);
      } else {
         row = convertView;
      }

      Item item = (Item)getItem(position);
      final String link = item.getUrl();

      ((TextView)row.findViewById(R.id.item_name)).setText(Html.fromHtml(item.getTitle()));
      ((TextView)row.findViewById(R.id.item_quantity)).setText(item.getQuantity() + "");
      row.findViewById(R.id.item_link).setContentDescription(mContext.getString(R.string.link_to_item,
       item.getTitle()));

      row.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(link));
            mContext.startActivity(intent);
         }
      });

      return row;
   }
}
