package com.dozuki.ifixit;

import android.os.Bundle;

import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import android.widget.TextView;

public class LoadingFragment extends Fragment {
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      TextView textView = new TextView(getActivity());

      textView.setText("Loading...");

      return textView;
   }
}
