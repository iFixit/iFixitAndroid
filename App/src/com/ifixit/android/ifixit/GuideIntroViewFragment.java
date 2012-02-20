package com.ifixit.android.ifixit;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GuideIntroViewFragment extends Fragment {
   private TextView mTitle;
   private TextView mIntro;
   private TextView mDifficulty;
   private TextView mAuthor;
   private TextView mTools;
   private TextView mParts;
   private TextView mNumSteps;
   private ImageManager mImageManager;
   private Guide mGuide;

   
   public GuideIntroViewFragment(ImageManager im, Guide guide) {
      mGuide = guide;
      mImageManager = im;
   }
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      
      View view = inflater.inflate(R.layout.guide_intro, container, false);        

      mTitle = (TextView)view.findViewById(R.id.guide_title);
      mIntro = (TextView)view.findViewById(R.id.guide_intro_text);
      mDifficulty = (TextView)view.findViewById(R.id.guide_difficulty);
      mAuthor = (TextView)view.findViewById(R.id.guide_author);
      //mImage = (LoaderImage)findViewById(R.id.intro_image);
      
      mTools = (TextView)view.findViewById(R.id.guide_tools);
      
      mParts = (TextView)view.findViewById(R.id.guide_parts);
      
      mNumSteps = (TextView)view.findViewById(R.id.num_steps);
      
      if (mGuide != null)
         setGuide();
      
      return view;
   }
   
   public void setGuide() {

      mTitle.setText(Html.fromHtml(mGuide.getTitle()));
      mIntro.setText(Html.fromHtml(mGuide.getIntroduction()));
      mDifficulty.setText(getActivity().getString(R.string.difficulty) + ": " +
       Html.fromHtml(mGuide.getDifficulty()));
      
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
