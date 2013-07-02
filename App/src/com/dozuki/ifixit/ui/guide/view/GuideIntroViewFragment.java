package com.dozuki.ifixit.ui.guide.view;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.util.Utils;

public class GuideIntroViewFragment extends SherlockFragment {
   private static final String SAVED_GUIDE = "SAVED_GUIDE";

   private TextView mTitle;
   private TextView mIntro;
   private TextView mDifficulty;
   private TextView mAuthor;
   private Guide mGuide;

   public GuideIntroViewFragment() { }

   public GuideIntroViewFragment(Guide guide) {
      mGuide = guide;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (savedInstanceState != null && mGuide == null) {
         mGuide = (Guide)savedInstanceState.getSerializable(SAVED_GUIDE);
      }
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      state.putSerializable(SAVED_GUIDE, mGuide);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.guide_intro, container, false);

      mTitle = (TextView)view.findViewById(R.id.guide_title);
      mIntro = (TextView)view.findViewById(R.id.guide_intro_text);
      mDifficulty = (TextView)view.findViewById(R.id.guide_difficulty);
      mAuthor = (TextView)view.findViewById(R.id.guide_author);

      MovementMethod method = LinkMovementMethod.getInstance();

      mIntro.setMovementMethod(method);

      if (mGuide != null) {
         setGuide();
      }

      return view;
   }

   public void setGuide() {
      mTitle.setText(Html.fromHtml(mGuide.getTitle()));
      mIntro.setText(Utils.correctLinkPaths(Html.fromHtml(mGuide
       .getIntroductionRendered())));

      if (!mGuide.getDifficulty().equals("false")) {
         mDifficulty.setText(getActivity().getString(R.string.difficulty) +
          ": " + Utils.correctLinkPaths(Html.fromHtml(mGuide.getDifficulty())));
      }

      mAuthor.setText(getActivity().getString(R.string.author) + ": " +
       Utils.correctLinkPaths(Html.fromHtml(mGuide.getAuthor())));
   }
}
