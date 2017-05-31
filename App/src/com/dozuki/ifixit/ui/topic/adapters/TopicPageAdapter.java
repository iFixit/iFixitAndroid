package com.dozuki.ifixit.ui.topic.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.dozuki.ifixit.App;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.dozuki.Site;
import com.dozuki.ifixit.model.topic.TopicLeaf;
import com.dozuki.ifixit.ui.WebViewFragment;
import com.dozuki.ifixit.ui.guide.view.NoGuidesFragment;
import com.dozuki.ifixit.ui.topic.TopicGuideListFragment;
import com.dozuki.ifixit.ui.topic.TopicInfoFragment;
import com.dozuki.ifixit.ui.topic.TopicRelatedWikisFragment;

import java.util.HashMap;
import java.util.Map;


public class TopicPageAdapter extends FragmentStatePagerAdapter {
   private Context mContext;
   private TopicLeaf mTopic;
   private Map<Integer, String> mPageLabelMap;
   private Site mSite;

   private static final int GUIDES_TAB = 0;
   private static final int MORE_INFO_TAB = 1;
   private static final int ANSWERS_TAB = 2;
   private static final int RELATED_WIKIS_TAB = 3;
   private static final String CURRENT_PAGE = "CURRENT_PAGE";
   private static final String CURRENT_TOPIC_LEAF = "CURRENT_TOPIC_LEAF";
   private static final String CURRENT_TOPIC_NODE = "CURRENT_TOPIC_NODE";
   private int mSelectedTab;

   public TopicPageAdapter(FragmentManager fm, Context context, TopicLeaf topic) {
      super(fm);
      mContext = context;
      mTopic = topic;
      mSite = App.get().getSite();
      mPageLabelMap = new HashMap<Integer, String>();
   }

   @Override
   public int getCount() {
      int base = 2;

      if (mSite.mAnswers) {
         base++;
      }

      if (mTopic.getRelatedWikis().size() > 0) {
         base++;
      }

      return base;
   }

   @Override
   public CharSequence getPageTitle(int position) {
      switch (position) {
         case GUIDES_TAB:
            return mContext.getString(R.string.guides);

         case MORE_INFO_TAB:
            return mContext.getString(R.string.info);

         case RELATED_WIKIS_TAB:
         case ANSWERS_TAB:
            if (mSite.mAnswers && position == ANSWERS_TAB) {
               return mContext.getString(R.string.answers);
            } else {
               return mContext.getString(R.string.related_pages);
            }
      }
      return "";
   }

   @Override
   public Fragment getItem(int position) {
      String label = "/category/" + mTopic.getName();
      Fragment selectedFragment;
      Bundle args = new Bundle();

      switch (position) {
         case GUIDES_TAB:
            if (mTopic.getGuides().size() == 0) {
               selectedFragment = new NoGuidesFragment();
            } else {
               selectedFragment = new TopicGuideListFragment();
               args.putSerializable(TopicGuideListFragment.TOPIC_LEAF_KEY, mTopic);
               selectedFragment.setArguments(args);
            }
            mSelectedTab = GUIDES_TAB;
            label += "/guides";
            break;
         case MORE_INFO_TAB:
            selectedFragment = new TopicInfoFragment();
            args.putSerializable(TopicInfoFragment.TOPIC_KEY, mTopic);
            selectedFragment.setArguments(args);
            label += "/info";

            break;
         case ANSWERS_TAB:
         case RELATED_WIKIS_TAB:
            if (mSite.mAnswers && position == ANSWERS_TAB) {
               WebViewFragment webView = new WebViewFragment();
               args.putString(WebViewFragment.URL_KEY, mTopic.getSolutionsUrl());

               webView.setArguments(args);

               label += "/answers";

               selectedFragment = webView;

            } else {
               selectedFragment = new TopicRelatedWikisFragment();
               args.putSerializable(TopicRelatedWikisFragment.TOPIC_LEAF_KEY, mTopic);
               selectedFragment.setArguments(args);
               label += "/related_pages";
            }

            mSelectedTab = position;
            break;
         default:
            return null;
      }

      mPageLabelMap.put(position, label);

      return selectedFragment;
   }

   @Override
   public void setPrimaryItem(ViewGroup container, int position, Object object) {
      super.setPrimaryItem(container, position, object);
      mSelectedTab = position;
   }

   public String getFragmentScreenLabel(int key) {
      return mPageLabelMap.get(key);
   }

   @Override
   public void destroyItem(View container, int position, Object object) {
      super.destroyItem(container, position, object);

      mPageLabelMap.remove(position);
   }
}
