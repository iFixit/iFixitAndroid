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

      Spanned title = Html.fromHtml(mTopic.getTitle());
      ((TextView) v.findViewById(R.id.topic_info_title)).setText(title);
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
      String topicContent = mTopic.getContentRendered();

      // Remove anchor elements from html
      topicContent = topicContent.replaceAll("<a class=\\\"anchor\\\".+?<\\/a>", "");
      topicContent = topicContent.replaceAll("<span class=\\\"editLink headerLink\\\".+?<\\/span>", "");

      Spanned topicHtml = Html.fromHtml(topicContent,
       // Handle images in the wiki text
       new UrlImageGetter(mContent, getActivity()),
       // Handle list items, videos, and other html elements that Html.fromHtml does not handle and parse them into
       // styled android views
       new WikiHtmlTagHandler());

      topicHtml = Utils.correctLinkPaths(topicHtml);

      Object[] spans = topicHtml.getSpans(0, topicHtml.length(), Object.class);

      for (int i = 0; i < spans.length; i++) {
         int start = topicHtml.getSpanStart(spans[i]);
         int end = topicHtml.getSpanEnd(spans[i]);

         if (spans[i] instanceof RelativeSizeSpan) {
            RelativeSizeSpan header = new RelativeSizeSpan(HEADER_SIZE);
            ((SpannableStringBuilder) topicHtml).setSpan(header, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         } else if (spans[i] instanceof ImageSpan) {
            Drawable drawable = ((ImageSpan) spans[i]).getDrawable();
            ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
            ((SpannableStringBuilder) topicHtml).removeSpan(spans[i]);
            char[] result = new char[end-start];
            ((SpannableStringBuilder) topicHtml).getChars(start, end, result, 0);
            ((SpannableStringBuilder) topicHtml).delete(start, end);
            ((SpannableStringBuilder) topicHtml).append(result[0]);
            ((SpannableStringBuilder) topicHtml).setSpan(imageSpan, topicHtml.length() - 1, topicHtml.length(),
             Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         }
      }

      return topicHtml;
   }
}
