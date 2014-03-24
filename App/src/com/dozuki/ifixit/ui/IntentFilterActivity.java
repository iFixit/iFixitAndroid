package com.dozuki.ifixit.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.ui.search.SearchActivity;
import com.dozuki.ifixit.ui.topic_view.TopicViewActivity;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiError;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.dozuki.ifixit.util.api.Api;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * This Activity handles Intent filtering for other Activities. All intent-filters
 * point at this Activity so it can perform site validation, figure out which
 * Activity can handle the URI and launches it.
 */
public class IntentFilterActivity extends BaseActivity {
   private static final String VIEW_URL = "VIEW_URL";
   private static final String URI_KEY = "URI_KEY";
   private static final String DOMAIN = "DOMAIN";
   private static final String INTENT_FILTERED = "INTENT_FILTERED";

   private String mDomain;
   private Uri mUri;
   private boolean mIntentFiltered;

   public static Intent viewUrl(Context context, String url) {
      Intent intent = new Intent(context, IntentFilterActivity.class);
      intent.putExtra(VIEW_URL, url);
      return intent;
   }

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      // TODO: Show loading.

      Intent intent = getIntent();

      if (savedState != null) {
         mDomain = savedState.getString(DOMAIN);
         mUri = savedState.getParcelable(URI_KEY);
         mIntentFiltered = savedState.getBoolean(INTENT_FILTERED);
      } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
         handleUriView(intent.getData());
      } else {
         Bundle extras = intent.getExtras();
         handleUriView(Uri.parse(extras.getString(VIEW_URL, "")));
      }
   }

   @Override
   public void onResume() {
      super.onResume();

      if (mIntentFiltered) {
         // Finish this empty Activity if the Intent has been dealt with.
         finish();
      }
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      outState.putString(DOMAIN, mDomain);
      outState.putParcelable(URI_KEY, mUri);
      outState.putBoolean(INTENT_FILTERED, mIntentFiltered);
   }

   private void handleUriView(Uri uri) {
      mUri = uri;
      List<String> segments = mUri.getPathSegments();

      Site currentSite = App.get().getSite();
      mDomain = uri.getHost();
      if (currentSite.hostMatches(mDomain)) {
         handlePathNavigation();
      } else if (App.isDozukiApp()) {
         // Only switch to the other site in the Dozuki app.
         // Set site to dozuki before API call.
         // TODO: Set the current site in the APICall rather than changing it
         // globally.
         App.get().setSite(Site.getSite("dozuki"));

         Api.call(this, ApiCall.sites());
      } else {
         displayNotFoundDialog();
         return;
      }
   }

   private void handlePathNavigation() {
      Intent intent = null;
      List<String> segments = mUri.getPathSegments();
      String prefix = segments.get(0);

      try {
         if (prefix.equalsIgnoreCase("guide") || prefix.equalsIgnoreCase("teardown")) {
            if (segments.get(1).equalsIgnoreCase("search")) {
               String query = segments.get(2);
               intent = SearchActivity.viewSearch(this, query);
            } else {
               int guideid = Integer.parseInt(segments.get(2).trim());
               intent = GuideViewActivity.viewGuideid(this, guideid);
            }
         } else if (prefix.equalsIgnoreCase("c") || prefix.equalsIgnoreCase("device")) {
            String topicName = segments.get(1);
            intent = TopicViewActivity.viewTopic(this, topicName);
         }
      } catch (Exception e) {
         Log.e("IntentFilterActivity", "Problem parsing Uri", e);

         App.getGaTracker().send(MapBuilder.createException(
          new StandardExceptionParser(this, null).getDescription(
           Thread.currentThread().getName(), e), false).build());

         displayNotFoundDialog();
      }

      if (intent != null) {
         /**
          * Don't call finish here. It results in some very very strange behavior
          * with DeadEvents in the Activity being launched. This flag indicates
          * that this Activity should be finished when it resumes.
          */
         mIntentFiltered = true;
         startActivity(intent);
      } else {
         displayNotFoundDialog();
      }
   }

   @Subscribe
   public void onSites(ApiEvent.Sites event) {
      if (!event.hasError()) {
         Site selectedSite = null;
         for (Site site : event.getResult()) {
            if (site.hostMatches(mDomain)) {
               selectedSite = site;
               break;
            }
         }

         if (selectedSite != null) {
            // Set the site and then fetch the guide.
            App.get().setSite(selectedSite);

            handlePathNavigation();
         } else {
            Exception e = new Exception();
            Log.e("GuideViewActivity", "Didn't find site!", e);

            App.getGaTracker().send(MapBuilder.createException(
             new StandardExceptionParser(this, null).getDescription(
             Thread.currentThread().getName(), e), false).build());

            displayNotFoundDialog();
         }
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   public void displayNotFoundDialog() {
      // The ApiEvent type doesn't matter.
      Api.getErrorDialog(this, new ApiEvent.ViewGuide().
       setCode(404).
       setError(ApiError.getByStatusCode(404))).show();
   }
}
