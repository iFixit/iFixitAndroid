package com.dozuki.ifixit.ui.wiki;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.Wiki;
import com.dozuki.ifixit.ui.BaseMenuDrawerActivity;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.UrlImageGetter;
import com.dozuki.ifixit.util.Utils;
import com.dozuki.ifixit.util.WikiHtmlTagHandler;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.squareup.otto.Subscribe;

public class WikiViewActivity extends BaseMenuDrawerActivity {
   private static final String WIKI_TITLE_KEY = "WIKI_TITLE_KEY";

   private static final float HEADER_SIZE = 1.3f;
   private Wiki mWiki;

   public static Intent viewByTitle(Context context, String title) {
      Intent intent = new Intent(context, WikiViewActivity.class);
      intent.putExtra(WIKI_TITLE_KEY, title);
      return intent;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      super.setDrawerContent(R.layout.wiki_main);

      String wikiTitle = "";

      if (savedInstanceState != null) {
         wikiTitle = savedInstanceState.getString(WIKI_TITLE_KEY);
      } else {
         Bundle extras = getIntent().getExtras();
         wikiTitle = extras.getString(WIKI_TITLE_KEY);
      }

      if (mWiki != null) {
         initializeUI(mWiki);
      } else {
         fetchWiki(wikiTitle);
      }
   }

   private void initializeUI(Wiki wiki) {
      hideLoading();

      mWiki = wiki;

      App.sendScreenView("/wiki/view/" + mWiki.title);
      getSupportActionBar().setTitle(mWiki.displayTitle);

      TextView wikiTitle = (TextView)findViewById(R.id.wiki_title);

      TextView wikiText = (TextView)findViewById(R.id.wiki_content);
      wikiTitle.setText(mWiki.displayTitle);
      Html.ImageGetter imgGetter = new UrlImageGetter(wikiText, this);
      String html = Utils.cleanWikiHtml(mWiki.contentsRendered);
      Spanned htmlParsed;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
         htmlParsed = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY, imgGetter, new WikiHtmlTagHandler());
      } else {
         htmlParsed = Html.fromHtml(mWiki.contentsRendered, imgGetter, new WikiHtmlTagHandler());
      }

      Object[] spans = htmlParsed.getSpans(0, htmlParsed.length(), Object.class);

      for (int i = 0; i < spans.length; i++) {
         int start = htmlParsed.getSpanStart(spans[i]);
         int end = htmlParsed.getSpanEnd(spans[i]);

         if (spans[i] instanceof RelativeSizeSpan) {
            RelativeSizeSpan header = new RelativeSizeSpan(HEADER_SIZE);
            ((SpannableStringBuilder) htmlParsed).setSpan(header, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         } else if (spans[i] instanceof ImageSpan) {
            Drawable drawable = ((ImageSpan) spans[i]).getDrawable();
            ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
            ((SpannableStringBuilder) htmlParsed).removeSpan(spans[i]);
            char[] result = new char[end-start];
            ((SpannableStringBuilder) htmlParsed).getChars(start, end, result, 0);
            ((SpannableStringBuilder) htmlParsed).delete(start, end);
            ((SpannableStringBuilder) htmlParsed).append(result[0]);
            ((SpannableStringBuilder) htmlParsed).setSpan(imageSpan, htmlParsed.length() - 1, htmlParsed.length(),
             Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         }
      }

      wikiText.setText(Utils.correctLinkPaths(htmlParsed));

      wikiText.setMovementMethod(LinkMovementMethod.getInstance());

   }

   @Subscribe
   public void onWiki(ApiEvent.ViewWiki event) {
      if (!event.hasError()) {
         if (mWiki == null) {
            Wiki wiki = event.getResult();
            initializeUI(wiki);
         }
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   private void fetchWiki(String title) {
      showLoading(R.id.loading_container);
      Api.call(this, ApiCall.wiki(title));
   }
}
