package com.dozuki.ifixit.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.guide.GuideInfo;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.dozuki.ifixit.util.api.Api;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class FavoritesActivity extends BaseMenuDrawerActivity {
   private static final int LIMIT = 200;
   private int OFFSET = 0;
   private static final String GUIDES_KEY = "GUIDES_KEY";

   private ArrayList<GuideInfo> mGuides = new ArrayList<GuideInfo>();
   private GridView mGridView;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setTitle(getString(R.string.favorites));

      setContentView(R.layout.favorites);

      if (savedInstanceState != null) {
         mGuides = (ArrayList<GuideInfo>)savedInstanceState.getSerializable(GUIDES_KEY);
         initGridView();
      } else {
         showLoading(R.id.favorites_loading);
         Api.call(this, Api.getUserFavorites(LIMIT, OFFSET));
      }

      MainApplication.getGaTracker().set(Fields.SCREEN_NAME, "/user/guides/favorites");
      MainApplication.getGaTracker().send(MapBuilder.createAppView().build());
   }

   private void initGridView() {
      mGridView = (GridView) findViewById(R.id.guide_grid);
      mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> arg0, View view, int position,
          long id) {
            GuideInfo guide = mGuides.get(position);
            Intent intent = new Intent(FavoritesActivity.this, GuideViewActivity.class);

            intent.putExtra(GuideViewActivity.GUIDEID, guide.mGuideid);
            startActivity(intent);
         }
      });

      mGridView.setAdapter(new GuideListAdapter(this, mGuides, false));
      mGridView.setEmptyView(findViewById(R.id.favorites_empty_view));
   }

   @Subscribe
   public void onGuides(ApiEvent.UserFavorites event) {
      hideLoading();

      if (!event.hasError()) {
         if (mGuides != null) {
            mGuides.addAll(event.getResult());
         } else {
            mGuides = new ArrayList<GuideInfo>(event.getResult());
         }

         initGridView();
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   @Override
   public void onSaveInstanceState(Bundle state) {
      super.onSaveInstanceState(state);

      state.putSerializable(GUIDES_KEY, mGuides);
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }
}
