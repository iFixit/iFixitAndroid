package com.dozuki.ifixit.ui.topic;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.model.topic.TopicNode;
import com.dozuki.ifixit.ui.BaseActivity;
import com.dozuki.ifixit.ui.guide.view.FullImageViewActivity;
import com.dozuki.ifixit.ui.guide.view.GuideViewActivity;
import com.dozuki.ifixit.ui.topic.adapters.TopicPageAdapter;
import com.dozuki.ifixit.util.ImageSizes;
import com.dozuki.ifixit.util.api.Api;
import com.dozuki.ifixit.util.api.ApiCall;
import com.dozuki.ifixit.util.api.ApiEvent;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class TopicViewActivity extends BaseActivity {
   public static final String TOPIC_KEY = "TOPIC";
   private static final String TOPIC_VIEW_TAG = "TOPIC_VIEW_TAG";

   private TopicViewFragment mTopicView;
   private AppBarLayout mAppBar;
   private TopicNode mTopicNode;
   private TopicLeaf mTopic;
   private ImageView mBackdropView;
   private ViewPager mPager;
   private TabLayout mTabs;
   private TopicPageAdapter mPageAdapter;
   private CollapsingToolbarLayout mCollapsingToolbar;

   public static Intent viewTopic(Context context, String topicName) {
      Intent intent = new Intent(context, TopicViewActivity.class);
      intent.putExtra(GuideViewActivity.TOPIC_NAME_KEY, topicName);
      return intent;
   }

   @Override
   public void onCreate(Bundle savedState) {
      super.onCreate(savedState);

      setTheme(R.style.Theme_Base_TransparentActionBar);

      setContentView(R.layout.topic_view);

      mContentFrame = (FrameLayout) findViewById(R.id.content_frame);

      mAppBar = (AppBarLayout) findViewById(R.id.appbar);
      mToolbar = (Toolbar) findViewById(R.id.toolbar);
      mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

      mToolbar.setTitleTextColor(getResources().getColor(R.color.white));

      setSupportActionBar(mToolbar);

      mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
         @Override
         public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            final Drawable upArrow;

            // Appbar is collapsed
            if (verticalOffset == -mCollapsingToolbar.getHeight() + mToolbar.getHeight()) {
               upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            } else {
               upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp);
            }

            getSupportActionBar().setHomeAsUpIndicator(upArrow);
         }
      });

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);

      //showLoading(R.id.loading_container);

      mBackdropView = (ImageView) findViewById(R.id.backdrop);
      mPager = (ViewPager) findViewById(R.id.topic_viewpager);
      mTabs = (TabLayout) findViewById(R.id.tabLayout);
      mTabs.setTabGravity(TabLayout.GRAVITY_FILL);
      mTabs.setVisibility(View.VISIBLE);
      mTopicNode = (TopicNode) getIntent().getSerializableExtra(TOPIC_KEY);

      if (mTopicNode != null) {
         String topicName = mTopicNode.getDisplayName();
         mCollapsingToolbar.setTitle(topicName);
         mCollapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.white));
         App.sendScreenView("/category/" + mTopicNode.getName());
         Api.call(this, ApiCall.topic(topicName));
      }
   }

   private void loadTopicImage() {
      String url = mTopic.getImage().getPath(ImageSizes.topicMain);
      Picasso
       .with(this)
       .load(url)
       .error(R.drawable.no_image)
       .into(mBackdropView);

      mBackdropView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            String url1 = (String) v.getTag();

            if (url1 == null || (url1.equals("") || url1.startsWith("."))) {
               return;
            }

            startActivity(FullImageViewActivity.viewImage(getBaseContext(), url1, false));
         }
      });
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         // Respond to the action bar's Up/Home button
         case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
      }
      return super.onOptionsItemSelected(item);
   }

   @Subscribe
   public void onTopic(ApiEvent.Topic event) {
      //hideLoading();
      if (!event.hasError()) {
         mTopic = event.getResult();
         mPageAdapter = new TopicPageAdapter(getSupportFragmentManager(), this, mTopic);
         mPager.setAdapter(mPageAdapter);
         mTabs.setupWithViewPager(mPager);

         loadTopicImage();
      } else {
         Api.getErrorDialog(this, event).show();
      }
   }

   @Override
   public void showLoading(int container) {
      findViewById(container).setVisibility(View.VISIBLE);
      super.showLoading(container);
   }

   @Override
   public void hideLoading() {
      super.hideLoading();
      findViewById(R.id.loading_container).setVisibility(View.GONE);
   }
}
