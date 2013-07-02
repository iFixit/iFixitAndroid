package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;

public class LoadingFragment extends SherlockFragment {
   private String mLoadingText;

   public LoadingFragment() { }

   public LoadingFragment(String text) {
      mLoadingText = (text.length() == 0) ? getString(R.string.loading) : text;
   }

   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {

      View v = inflater.inflate(R.layout.loading_fragment, container, false);

      if (mLoadingText != null) {
         ((TextView)v.findViewById(R.id.loading_text)).setText(mLoadingText);
      }

      return v;
   }
}
