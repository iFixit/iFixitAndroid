package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;

public class LoadingFragment extends SherlockFragment {
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      return inflater.inflate(R.layout.loading_fragment, container, false);
   }
}
