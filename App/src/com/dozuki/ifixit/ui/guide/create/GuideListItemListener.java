package com.dozuki.ifixit.ui.guide.create;

import com.dozuki.ifixit.model.guide.GuideInfo;

public interface GuideListItemListener {
   void onEditItemClicked(GuideInfo guide);
   void onPublishItemClicked(GuideInfo guide);
   void onViewItemClicked(GuideInfo guide);
   void onDeleteItemClicked(GuideInfo guide);
   void onItemLongClick(GuideInfo guide);
}
