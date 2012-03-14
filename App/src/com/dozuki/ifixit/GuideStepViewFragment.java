package com.dozuki.ifixit;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class GuideStepViewFragment extends SherlockFragment {
   protected static final String IMAGE_FILE_PATH = "IMAGE_FILE_PATH";

   private TextView mTitle;
   private ThumbnailView mThumbs;
   private LoaderImage mMainImage;
   private GuideStep mStep;
   private ImageManager mImageManager;
   private StepTextArrayAdapter mTextAdapter;
   private ListView mLineList;
   private Typeface mFont;
   private ImageSizes mImageSizes;

   public GuideStepViewFragment() {

   }

   public GuideStepViewFragment(ImageManager im, GuideStep step) {
      mStep = step;
      mImageManager = im;
   }

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      if (mImageManager == null) {
         mImageManager = ((MainApplication)getActivity().getApplication()).
          getImageManager();
      }

      mImageSizes = ((MainApplication)getActivity().getApplication()).
       getImageSizes();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
	 Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.guide_step, container, false);
      mFont = Typeface.createFromAsset(getActivity().getAssets(),
       "fonts/Ubuntu-B.ttf");

      mLineList = (ListView)view.findViewById(R.id.step_text_list);
      mTitle = (TextView)view.findViewById(R.id.step_title);
      mTitle.setTypeface(mFont);
      mTitle.setTextColor(ColorStateList.valueOf(Color.WHITE));

      mMainImage = (LoaderImage)view.findViewById(R.id.main_image);
      mMainImage.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            String url = (String)v.getTag();
            String filePath = mImageManager.getFilePath(url +
             mImageSizes.getMain());

            // Make sure that the image has been loaded
            if (filePath != null) {
               Intent intent = new Intent(getActivity(), FullImageView.class);
               intent.putExtra(IMAGE_FILE_PATH, filePath);

               startActivity(intent);
            }
         }
      });

      mThumbs = (ThumbnailView)view.findViewById(R.id.thumbnails);
      mThumbs.setMainImage(mMainImage);

      if (mStep != null) {
         setStep();
      }

      return view;
   }

   public int getScreenHeight() {
      Display display = getActivity().getWindowManager().getDefaultDisplay();
      return display.getHeight();
   }

   public int getScreenWidth() {
      Display display = getActivity().getWindowManager().getDefaultDisplay();
      return display.getWidth();
   }

   public void setStep() {
      if (mStep.getTitle().length() == 0) {
         mTitle.setText(getActivity().getString(R.string.step) + " " +
          mStep.getStepNum());
      } else {
         mTitle.setText(mStep.getTitle());
      }

      mTextAdapter = new StepTextArrayAdapter(getActivity(),
       R.id.step_text_list, mStep.getLines());
      mLineList.setAdapter(mTextAdapter);

      mThumbs.setImageSizes(mImageSizes);
      mThumbs.setThumbs(mStep.mImages, mImageManager, getActivity());

      // Might be a problem if there are no images for a step...
      mThumbs.setCurrentThumb(mStep.mImages.get(0).getText());
   }

   public void setImageManager(ImageManager im) {
	   mImageManager = im;
   }

   public class StepTextArrayAdapter extends ArrayAdapter<StepLine> {
      private ArrayList<StepLine> mLines;
      private Context mContext;

      public StepTextArrayAdapter(Context context, int viewResourceId,
       ArrayList<StepLine> lines) {
         super(context, viewResourceId, lines);

         mLines = lines;
         mContext = context;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         GuideStepLineView stepLine = (GuideStepLineView)convertView;

         if (stepLine == null) {
            stepLine = new GuideStepLineView(mContext);
         }

         stepLine.setLine(mLines.get(position));
         return stepLine;
      }
   }
}
