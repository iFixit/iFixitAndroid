package com.dozuki.ifixit.ui.topic_view;

import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.google.analytics.tracking.android.EasyTracker;
import com.squareup.picasso.Picasso;
import org.xml.sax.XMLReader;

public class TopicInfoFragment extends SherlockFragment {

   private static final float HEADER_SIZE = 1.5f;
   private static final String IMAGE_SIZE = ".medium";
   private static final String TOPIC_KEY = "TOPIC_KEY";

   private TopicLeaf mTopic;

   /**
    * Required for restoring fragments
    */
   public TopicInfoFragment() {}

   public TopicInfoFragment(TopicLeaf topic) {
      mTopic = topic;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      EasyTracker.getInstance().setContext(getActivity());

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

      ((TextView) v.findViewById(R.id.topic_info_title)).setText(Html.fromHtml(mTopic.getTitle()));
      ((TextView) v.findViewById(R.id.topic_info_summary)).setText(mTopic.getDescription());
      TextView content = ((TextView) v.findViewById(R.id.topic_info_content));
      content.setMovementMethod(LinkMovementMethod.getInstance());
      content.setText(getStyledContent());

      String url = mTopic.getImage().getPath(IMAGE_SIZE);

      Picasso.with(getSherlockActivity())
       .load(url)
       .error(R.drawable.no_image)
       .into((ImageView) v.findViewById(R.id.topic_info_image));

      return v;
   }

   @Override
   public void onStart() {
      super.onStart();
      EasyTracker.getTracker().sendView(mTopic.getName() + " Info");
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      outState.putSerializable(TOPIC_KEY, mTopic);
      super.onSaveInstanceState(outState);
   }

   private Spanned getStyledContent() {
      // Remove anchor elements from html
      String topicContent = mTopic.getContentRendered().replaceAll("<a class=\\\"anchor\\\".+?<\\/a>", "");
      Spanned topicHtml = Html.fromHtml(topicContent, null,
       new Html.TagHandler() {
          private String parent = "";

          @Override
          public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
             if (tag.equals("ul") || tag.equals("ol")) {
                parent = tag;
             }

             if (tag.equals("li") && (parent.equals("ul") || parent.equals("ol"))) {
                output.append("\n");
             }
          }
       });

      Object[] spans = topicHtml.getSpans(0, topicHtml.length(), Object.class);

      for (int i = 0; i < spans.length; i++) {
         int start = topicHtml.getSpanStart(spans[i]);
         int end = topicHtml.getSpanEnd(spans[i]);
         Log.w("TopicInfoFragment", spans[i].toString());

         if (spans[i] instanceof RelativeSizeSpan) {
            RelativeSizeSpan header = new RelativeSizeSpan(HEADER_SIZE);
            ((SpannableStringBuilder) topicHtml).setSpan(header, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         }
      }

      return topicHtml;
   }
}
