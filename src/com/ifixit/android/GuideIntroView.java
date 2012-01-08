package com.ifixit.android;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuideIntroView extends LinearLayout {
   private TextView mTitle;
   private TextView mIntro;
   private TextView mDifficulty;
   private TextView mAuthor;
   private TextView mTools;
   private TextView mParts;
   private TextView mNumSteps;

   private LoaderImage mImage;

   public GuideIntroView(Context context, Guide guide) {
      super(context);      

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.guide_intro, this, true);        

      mTitle = (TextView)findViewById(R.id.guide_title);
      mIntro = (TextView)findViewById(R.id.guide_intro_text);
      mDifficulty = (TextView)findViewById(R.id.guide_difficulty);
      mAuthor = (TextView)findViewById(R.id.guide_author);
      mImage = (LoaderImage)findViewById(R.id.intro_image);
      
      mTitle.setText(Html.fromHtml(guide.getTitle()));
      mIntro.setText(Html.fromHtml(guide.getIntroduction()));
      mDifficulty.setText(context.getString(R.string.difficulty) + ": " +
       Html.fromHtml(guide.getDifficulty()));

      mAuthor.setText(context.getString(R.string.author) + ": " +
       Html.fromHtml(guide.getAuthor()));
      
      mTools = (TextView)findViewById(R.id.guide_tools);
      if (guide.getNumTools() != 0) 
         mTools.setText(Html.fromHtml(guide.getToolsFormatted()));
      
      mParts = (TextView)findViewById(R.id.guide_parts);
      if (guide.getNumParts() != 0) 
         mParts.setText(Html.fromHtml(guide.getPartsFormatted()));
      
      mNumSteps = (TextView)findViewById(R.id.num_steps);
      mNumSteps.setText("Number of steps: " + guide.getNumSteps());
   }

   public LoaderImage getImageView() {
      return mImage;
   }
}
