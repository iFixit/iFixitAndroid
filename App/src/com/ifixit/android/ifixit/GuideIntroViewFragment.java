package com.ifixit.android.ifixit;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GuideIntroViewFragment extends Fragment {
   private static final String SAVED_GUIDE = "SAVED_GUIDE";

   private TextView mTitle;
   private TextView mIntro;
   private TextView mDifficulty;
   private TextView mAuthor;
   private TextView mTools;
   private TextView mParts;
   private TextView mNumSteps;
   private ImageManager mImageManager;
   private Guide mGuide;
   private Typeface mBoldFont;
   private Typeface mRegularFont;

   public GuideIntroViewFragment() {

   }
   
   public GuideIntroViewFragment(ImageManager im, Guide guide) {
      mGuide = guide;
      mImageManager = im;
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
      mBoldFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Ubuntu-B.ttf");  
      mRegularFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Ubuntu-R.ttf");  


      mTitle      = (TextView)view.findViewById(R.id.guide_title);
      mIntro      = (TextView)view.findViewById(R.id.guide_intro_text);
      mDifficulty = (TextView)view.findViewById(R.id.guide_difficulty);
      mAuthor     = (TextView)view.findViewById(R.id.guide_author);
      mTools      = (TextView)view.findViewById(R.id.guide_tools);     
      mParts      = (TextView)view.findViewById(R.id.guide_parts);     
      mNumSteps   = (TextView)view.findViewById(R.id.num_steps);
   
   
      mTitle.setTypeface(mBoldFont);
      mIntro.setTypeface(mRegularFont);
      mDifficulty.setTypeface(mRegularFont);
      mAuthor.setTypeface(mRegularFont);
      mTools.setTypeface(mRegularFont);
      mParts.setTypeface(mRegularFont);
      mNumSteps.setTypeface(mRegularFont);
      
      if (mGuide != null)
         setGuide();
      
      return view;
   }
   
   public void setGuide() {
      mTitle.setText(Html.fromHtml(mGuide.getTitle()));
      mIntro.setText(Html.fromHtml(mGuide.getIntroduction()));
      if (!mGuide.getDifficulty().equals("false")) {
         mDifficulty.setText(getActivity().getString(R.string.difficulty) + ": " +
          Html.fromHtml(mGuide.getDifficulty()));
      }
      
      mAuthor.setText(getActivity().getString(R.string.author) + ": " +
       Html.fromHtml(mGuide.getAuthor()));
      
      if (mGuide.getNumTools() != 0) 
         mTools.setText(Html.fromHtml(mGuide.getToolsFormatted()));
      
      if (mGuide.getNumParts() != 0) 
         mParts.setText(Html.fromHtml(mGuide.getPartsFormatted()));

   }

   //public LoaderImage getImageView() {
   //   return mImage;
   //}
}
