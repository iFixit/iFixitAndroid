package com.ifixit.guidebook;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuideIntroView extends LinearLayout {
   private Context mContext;
   private TextView mTitle;
   private TextView mIntro;
   private TextView mDifficulty;
   private TextView mAuthor;
   private TextView mDate;
   private ImageView mImage;
   private ImageManager mImageManager;

   public GuideIntroView(Context context, Guide guide, ImageManager imageManager) {
      super(context);      
      this.mContext = context;
      mImageManager = imageManager;

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);

      inflater.inflate(R.layout.guide_intro, this, true);        

      mTitle = (TextView) findViewById(R.id.guide_title);
      mTitle.setText(Html.fromHtml(guide.getTitle().toString()));
      
      mIntro = (TextView) findViewById(R.id.guide_intro_text);
      mIntro.setText(Html.fromHtml(guide.getIntroduction().toString()));

      mDifficulty = (TextView) findViewById(R.id.guide_difficulty);
      mDifficulty.setText("Difficulty: " + Html.fromHtml(guide.getDifficulty().toString()));

      mAuthor = (TextView) findViewById(R.id.guide_author);
      mAuthor.setText("Author: " + Html.fromHtml(guide.getAuthor().toString()));

      mImage = (ImageView)findViewById(R.id.intro_image);
      
   }

   public ImageView getImageView() {
      return mImage;
   }

}
