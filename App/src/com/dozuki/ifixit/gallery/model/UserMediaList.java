package com.dozuki.ifixit.gallery.model;

import java.io.Serializable;
import java.util.ArrayList;


public interface UserMediaList extends Serializable {

   public ArrayList<MediaInfo> getItems();

   public void addItem(MediaInfo userImageInfo);

}
