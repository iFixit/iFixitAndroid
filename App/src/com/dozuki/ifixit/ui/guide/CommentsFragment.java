package com.dozuki.ifixit.ui.guide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.ui.BaseDialogFragment;

import java.util.ArrayList;

public class CommentsFragment extends BaseDialogFragment {

   private static final String COMMENTS_KEY = "COMMENTS_KEY";
   private ArrayList<Comment> mComments;

   public static CommentsFragment newInstance(ArrayList<Comment> comments) {
      Bundle args = new Bundle();
      args.putSerializable(COMMENTS_KEY, comments);
      CommentsFragment frag = new CommentsFragment();
      frag.setArguments(args);
      return frag;
   }

   public CommentsFragment() {
      mComments = new ArrayList<Comment>();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.guide_step_comments, container, false);
      Bundle args = getArguments();

      if (savedInstanceState != null) {
         mComments = (ArrayList<Comment>)savedInstanceState.getSerializable(COMMENTS_KEY);
      } else if (args != null) {
         mComments = (ArrayList<Comment>)args.getSerializable(COMMENTS_KEY);
      }

      ListView list = (ListView)view.findViewById(android.R.id.list);
      list.setEmptyView(view.findViewById(android.R.id.empty));

      CommentsAdapter adapter = new CommentsAdapter(getActivity(), mComments);
      list.setAdapter(adapter);

      getDialog().setTitle(getString(R.string.step_comments));
      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(COMMENTS_KEY, mComments);
   }
}