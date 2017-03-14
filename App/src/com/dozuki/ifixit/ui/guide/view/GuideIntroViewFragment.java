package com.dozuki.ifixit.ui.guide.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.ui.BaseFragment;
import com.dozuki.ifixit.util.Utils;
import com.dozuki.ifixit.util.WikiHtmlTagHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuideIntroViewFragment extends BaseFragment {
   private static final String SAVED_GUIDE = "SAVED_GUIDE";
   public static final String GUIDE_KEY = "GUIDE_KEY";

   private Guide mGuide;

   public GuideIntroViewFragment() {}

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      Bundle bundle = getArguments();

      if (savedInstanceState != null && mGuide == null) {
         mGuide = (Guide) savedInstanceState.getSerializable(SAVED_GUIDE);
      } else if (bundle != null) {
         mGuide = (Guide)bundle.getSerializable(GUIDE_KEY);
      }
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(SAVED_GUIDE, mGuide);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.guide_intro, container, false);

      TextView mTitle = (TextView) view.findViewById(R.id.guide_title);
      TextView mIntro = (TextView) view.findViewById(R.id.guide_intro_text);
      TextView mDifficulty = (TextView) view.findViewById(R.id.guide_difficulty);
      TextView mAuthor = (TextView) view.findViewById(R.id.guide_author);

      MovementMethod method = LinkMovementMethod.getInstance();

      mIntro.setMovementMethod(method);

      if (mGuide != null) {
         mTitle.setText(mGuide.getTitle());

         String introductionText = mGuide.getIntroductionRendered();

         Pattern p = Pattern.compile("<iframe.*?src=\"([^\"]*)\"");
         Matcher m = p.matcher(introductionText);

         if (m.find()) {
            int count = m.groupCount();
            if (count > 0) {
               for (int i = 1; i <= count; i++) {
                  final String iframeSrc = m.group(i); // Group 0 denotes the full pattern,
                  final Activity activity = getActivity();
                  final WebView webView = new WebView(activity);
                  WebSettings settings = webView.getSettings();
                  settings.setJavaScriptEnabled(true);

                  webView.setOnTouchListener(new View.OnTouchListener() {
                     private final static int TOUCH_RELEASED = 0;
                     private final static int TOUCH_TOUCHED = 1;
                     private final static int TOUCH_DRAGGING = 2;
                     private final static int TOUCH_UNDEFINED = 3;
                     private int previousState = TOUCH_RELEASED;

                     @Override
                     public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                           case MotionEvent.ACTION_DOWN:
                              if (previousState == TOUCH_RELEASED) previousState = TOUCH_TOUCHED;
                              else previousState = TOUCH_UNDEFINED;
                              break;
                           case MotionEvent.ACTION_UP:
                              if (previousState != TOUCH_DRAGGING) {
                                 previousState = TOUCH_RELEASED;

                                 Intent intent = new Intent(Intent.ACTION_VIEW);
                                 intent.setData(Uri.parse(iframeSrc));
                                 startActivity(intent);
                              } else if (previousState == TOUCH_DRAGGING) {
                                 previousState = TOUCH_RELEASED;
                              } else previousState = TOUCH_UNDEFINED;
                              break;
                           case MotionEvent.ACTION_MOVE:
                              if (previousState == TOUCH_TOUCHED || previousState == TOUCH_DRAGGING) previousState = TOUCH_DRAGGING;
                              else previousState = TOUCH_UNDEFINED;
                              break;
                           default:
                              previousState = TOUCH_UNDEFINED;
                        }

                        return false;
                     }
                  });

                  webView.setWebChromeClient(new WebChromeClient() {
                     public void onProgressChanged(WebView view, int progress) {
                        // Activities and WebViews measure progress with different scales.
                        // The progress meter will automatically disappear when we reach 100%
                        activity.setProgress(progress * 1000);
                     }
                  });
                  webView.setWebViewClient(new WebViewClient() {
                     public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                        return true;
                     }
                  });

                  webView.loadUrl(iframeSrc);
                  webView.setClickable(false);

                  final LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.guide_intro_container);
                  linearLayout.addView(webView);

                  // Setting LayoutParams must happen after the webview is added to the parent view otherwise it'll
                  // NPE.
                  LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) webView.getLayoutParams();
                  params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                  params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                  webView.setLayoutParams(params);
                  ViewTreeObserver vto = webView.getViewTreeObserver();

                  if (vto != null) {
                     vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {

                           LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) webView.getLayoutParams();
                           params.height = (int) (webView.getWidth() * (0.5625f));
                           webView.setLayoutParams(params);

                           ViewTreeObserver obs = webView.getViewTreeObserver();
                           if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                              obs.removeGlobalOnLayoutListener(this);
                           } else {
                              obs.removeOnGlobalLayoutListener(this);
                           }
                        }
                     });
                  }
               }
            }
         }

         if (introductionText.length() > 0) {
            mIntro.setText(Utils.correctLinkPaths(Html.fromHtml(introductionText, null, new WikiHtmlTagHandler())));
         } else {
            mIntro.setText(introductionText);
         }

         // Authors and Difficulty are not relevant on teardowns.
         if (mGuide.getType().equalsIgnoreCase("teardown")) {
            mDifficulty.setVisibility(View.GONE);
            mAuthor.setVisibility(View.GONE);
         } else {
            if (!mGuide.getDifficulty().equals("false")) {
               mDifficulty.setText(getActivity().getString(R.string.difficulty) +
                ": " + Utils.correctLinkPaths(Html.fromHtml(mGuide.getDifficulty())));
            }

            mAuthor.setText(getActivity().getString(R.string.author) + ": " +
             Utils.correctLinkPaths(Html.fromHtml(mGuide.getAuthor())));

         }
      }
      return view;
   }
}
