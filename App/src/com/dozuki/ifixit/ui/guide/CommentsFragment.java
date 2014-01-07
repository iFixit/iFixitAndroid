package com.dozuki.ifixit.ui.guide;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Comment;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.ui.BaseDialogFragment;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class CommentsFragment extends BaseDialogFragment {

   private static final String COMMENTS_KEY = "COMMENTS_KEY";
   private static final String TITLE_KEY = "TITLE_FIELD";
   private static final String GUIDEID_KEY = "GUIDEID_KEY";
   private static final String STEPID_KEY = "STEPID_KEY";

   private ArrayList<Comment> mComments;
   private int mGuideid;
   private int mStepid;
   private CommentsAdapter mAdapter;

   public static CommentsFragment newInstance(ArrayList<Comment> comments, String title, int guideid, int stepid) {
      Bundle args = new Bundle();
      args.putSerializable(COMMENTS_KEY, comments);
      args.putString(TITLE_KEY, title);
      args.putInt(GUIDEID_KEY, guideid);
      args.putInt(STEPID_KEY, stepid);
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
         mComments = (ArrayList<Comment>) savedInstanceState.getSerializable(COMMENTS_KEY);
         mGuideid = savedInstanceState.getInt(GUIDEID_KEY);
         mStepid = savedInstanceState.getInt(STEPID_KEY);
         title = savedInstanceState.getString(TITLE_KEY);
      } else if (args != null) {
         mComments = (ArrayList<Comment>) args.getSerializable(COMMENTS_KEY);
         mGuideid = args.getInt(GUIDEID_KEY);
         mStepid = args.getInt(STEPID_KEY);
         title = args.getString(TITLE_KEY);
      } else {
         title = getString(R.string.comments);
      }

      final EditText editText = (EditText) view.findViewById(R.id.add_comment_field);

      ImageButton addComment = (ImageButton) view.findViewById(R.id.add_comment_button);
      addComment.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String commentText = String.valueOf(editText.getText());

            if (commentText.length() > 0) {
               Api.call(getActivity(), ApiCall.postNewGuideComment(commentText, mGuideid, mStepid));
            }
         }
      });

      ListView list = (ListView) view.findViewById(android.R.id.list);
      list.setEmptyView(view.findViewById(android.R.id.empty));

      mAdapter = new CommentsAdapter(getActivity(), mComments);
      list.setAdapter(mAdapter);

      getDialog().setTitle(title);
      setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_DialogWhenLarge);
      return view;
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(COMMENTS_KEY, mComments);
      state.putInt(GUIDEID_KEY, mGuideid);
      state.putInt(STEPID_KEY, mStepid);
   }

   @Subscribe
   public void onCommentAdd(ApiEvent.AddComment event) {
      if (!event.hasError()) {
         Guide guide = event.getResult();
         mComments.clear();
         if (mStepid == -1) {
            mComments.addAll(guide.getComments());
         } else {
            mComments.addAll(guide.getStepById(mStepid).getComments());
         }

         mAdapter.setComments(mComments);
         mAdapter.notifyDataSetChanged();
      } else {

      }
   }
}
