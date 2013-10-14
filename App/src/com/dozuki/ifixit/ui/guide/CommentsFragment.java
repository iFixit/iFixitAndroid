package com.dozuki.ifixit.ui.guide;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.ui.BaseDialogFragment;

import java.util.ArrayList;

public class CommentsFragment extends BaseDialogFragment {

   private static final String COMMENTS_KEY = "COMMENTS_KEY";
   private static final String TITLE_KEY = "TITLE_FIELD";
   private ArrayList<Comment> mComments;

   public static CommentsFragment newInstance(ArrayList<Comment> comments, String title) {
      Bundle args = new Bundle();
      args.putSerializable(COMMENTS_KEY, comments);
      args.putString(TITLE_KEY, title);
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
      String title;

      if (savedInstanceState != null) {
         mComments = (ArrayList<Comment>)savedInstanceState.getSerializable(COMMENTS_KEY);
         title = savedInstanceState.getString(TITLE_KEY);
      } else if (args != null) {
         mComments = (ArrayList<Comment>)args.getSerializable(COMMENTS_KEY);
         title = args.getString(TITLE_KEY);
      } else {
         title = getString(R.string.comments);
      }

      final EditText editText = (EditText)view.findViewById(R.id.add_comment_field);

      ImageButton addComment = (ImageButton)view.findViewById(R.id.add_comment_button);
      addComment.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String commentText = String.valueOf(editText.getText());
            Log.d("CommentsFragment", "send comment: " + commentText);
         }
      });

      ListView list = (ListView)view.findViewById(android.R.id.list);
      list.setEmptyView(view.findViewById(android.R.id.empty));

      CommentsAdapter adapter = new CommentsAdapter(getActivity(), mComments);
      list.setAdapter(adapter);

      getDialog().setTitle(title);
      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(COMMENTS_KEY, mComments);
   }
}
