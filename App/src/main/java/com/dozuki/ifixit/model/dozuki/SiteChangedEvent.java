package com.dozuki.ifixit.model.dozuki;

import com.dozuki.ifixit.model.user.User;

public class SiteChangedEvent {
   public final Site mSite;
   public final User mUser;

   public SiteChangedEvent(Site site, User user) {
      mSite = site;
      mUser = user;
   }
}
