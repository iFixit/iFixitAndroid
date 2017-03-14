package com.dozuki.ifixit.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dozuki.ifixit.R;

public class LoadingFragment extends BaseFragment {
   public static String TEXT_KEY = "LOADING_TEXT_KEY";

   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      Bundle args = this.getArguments();
      String text;
      if (args != null) {
         text = args.getString(TEXT_KEY);
      } else {
         text = "Loading";
      }

      View v = inflater.inflate(R.layout.loading_fragment, container, false);

      ((TextView)v.findViewById(R.id.loading_text)).setText(text);

      return v;
   }
}
