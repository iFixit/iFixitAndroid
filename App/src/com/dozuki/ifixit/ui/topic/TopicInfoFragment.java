package com.dozuki.ifixit.ui.topic;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.ui.guide.view.FullImageViewActivity;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.PicassoImageGetter;
import com.dozuki.ifixit.util.UrlImageGetter;
import com.dozuki.ifixit.util.Utils;
import com.dozuki.ifixit.util.WikiHtmlTagHandler;
import com.squareup.picasso.Picasso;

public class TopicInfoFragment extends BaseFragment {

   private static final float HEADER_SIZE = 1.3f;
   public static final String TOPIC_KEY = "TOPIC_KEY";

   private TopicLeaf mTopic;
   private TextView mContent;
   private ImageView mBackdrop;

   /**
    * Required for restoring fragments
    */
   public TopicInfoFragment() {}


   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      Bundle b = getArguments();

      if (savedInstanceState != null) {
         mTopic = (TopicLeaf) savedInstanceState.getSerializable(TOPIC_KEY);
      } else if (b != null) {
         mTopic = (TopicLeaf) b.getSerializable(TOPIC_KEY);
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View v = inflater.inflate(R.layout.topic_info, container, false);

      ((TextView) v.findViewById(R.id.topic_info_summary)).setText(mTopic.getDescription());
      mBackdrop = (ImageView)v.findViewById(R.id.backdrop);

      if (mBackdrop != null) {
         String url = mTopic.getImage().getPath(ImageSizes.topicMain);
         Picasso
          .with(getContext())
          .load(url)
          .error(R.drawable.no_image)
          .into(mBackdrop);

         mBackdrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String url1 = (String) v.getTag();

               if (url1 == null || (url1.equals("") || url1.startsWith("."))) {
                  return;
               }

               startActivity(FullImageViewActivity.viewImage(getContext(), url1, false));
            }
         });
      }

      mContent = ((TextView) v.findViewById(R.id.topic_info_content));
      mContent.setMovementMethod(LinkMovementMethod.getInstance());
      mContent.setText(getStyledContent());
      return v;
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putSerializable(TOPIC_KEY, mTopic);
   }

   private Spanned getStyledContent() {
      PicassoImageGetter imgGetter = new PicassoImageGetter(mContent, getResources());
      return mTopic.getContentSpanned(imgGetter);
   }
}
