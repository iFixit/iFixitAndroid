package com.dozuki.ifixit.view.ui;

import java.io.Serializable;

public class LocalImage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String imgId;
	String path;

	public LocalImage(String id, String p) {
		imgId = id;
		path = p;
	}

	public LocalImage(String p) {
		path = p;
		imgId = null;
	}
}