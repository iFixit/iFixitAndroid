package com.dozuki.ifixit;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class GuideIntroViewFragment extends SherlockFragment {
   private static final String SAVED_GUIDE = "SAVED_GUIDE";

   private TextView mTitle;
   private TextView mIntro;
   private TextView mDifficulty;
   private TextView mAuthor;
   private TextView mTools;
   private TextView mParts;
   private ImageManager mImageManager;
   private Guide mGuide;
   private Typeface mBoldFont;

   public GuideIntroViewFragment() {

   }

   public GuideIntroViewFragment(Guide guide) {
      mGuide = guide;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (savedInstanceState != null && mGuide == null) {
         mGuide = (Guide)savedInstanceState.getSerializable(SAVED_GUIDE);
      }

      if (mImageManager == null) {
         mImageManager = ((MainApplication)getActivity().getApplication()).
          getImageManager();
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
      mBoldFont = Typeface.createFromAsset(getActivity().getAssets(),
       "fonts/Ubuntu-B.ttf");

      mTitle      = (TextView)view.findViewById(R.id.guide_title);
      mIntro      = (TextView)view.findViewById(R.id.guide_intro_text);
      mDifficulty = (TextView)view.findViewById(R.id.guide_difficulty);
      mAuthor     = (TextView)view.findViewById(R.id.guide_author);
      mTools      = (TextView)view.findViewById(R.id.guide_tools);
      mParts      = (TextView)view.findViewById(R.id.guide_parts);

      MovementMethod method = LinkMovementMethod.getInstance();

      mIntro.setMovementMethod(method);
      mTools.setMovementMethod(method);
      mParts.setMovementMethod(method);

      mTitle.setTypeface(mBoldFont);

      if (mGuide != null) {
         setGuide();
      }

      return view;
   }

   public void setGuide() {
      if (mGuide.mSubject.length() != 0) {
         mTitle.setText(Html.fromHtml(mGuide.getSubject()));
      } else {
         mTitle.setText(Html.fromHtml(mGuide.getTitle()));
      }
      mIntro.setText(JSONHelper.correctLinkPaths(Html.fromHtml(
       mGuide.getIntroduction())));

      if (!mGuide.getDifficulty().equals("false")) {
         mDifficulty.setText(getActivity().getString(R.string.difficulty) + ": " +
          JSONHelper.correctLinkPaths(Html.fromHtml(mGuide.getDifficulty())));
      }

      mAuthor.setText(getActivity().getString(R.string.author) + ": " +
       JSONHelper.correctLinkPaths(Html.fromHtml(mGuide.getAuthor())));
      
      if (mGuide.getNumTools() != 0) {
         mTools.setText(Html.fromHtml(mGuide.getToolsFormatted(
          getActivity().getString(R.string.requiredTools))));
      }

      if (mGuide.getNumParts() != 0) {
         mParts.setText(Html.fromHtml(mGuide.getPartsFormatted(
          getActivity().getString(R.string.requiredParts))));
      }
   }
}
