package com.dozuki.ifixit.ui.guide_view;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.dozuki.ifixit.R;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;

public class LoadingFragment extends Fragment {
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      return inflater.inflate(R.layout.loading_fragment, container, false);
   }
}
