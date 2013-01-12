package com.dozuki.ifixit.gallery.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockFragment;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.gallery.model.UserEmbedList;
import com.dozuki.ifixit.gallery.model.UserVideoList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

public class EmbedMediaFragment extends Fragment implements
		MediaFragment {
	private GridView mGridView;
	private MediaAdapter mGalleryAdapter;
	public TextView noEmbedText;
	private static UserEmbedList mEmbedList;
	private int mSelectForReturn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mEmbedList = new UserEmbedList();
		mGalleryAdapter = new MediaAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.gallery_view, container, false);

		noEmbedText = ((TextView) view.findViewById(R.id.no_images_text));
		noEmbedText.setText(R.string.no_embeds_text);
		if (mEmbedList.getEmbeds().size() < 1) {
			noEmbedText.setVisibility(View.VISIBLE);
		} else {
			noEmbedText.setVisibility(View.GONE);
		}

		mGridView = (GridView) view.findViewById(R.id.gridview);
		// mGridView.setOnScrollListener(new GalleryOnScrollListener());

		mGridView.setAdapter(mGalleryAdapter);
		// mGridView.setOnItemClickListener(this);
		// mGridView.setOnItemLongClickListener(this);

		// mButtons = (RelativeLayout) view.findViewById(R.id.button_holder);
		// mLoginText = ((TextView) view.findViewById(R.id.login_text));

		// if (mSelectedList.contains(true)) {
		// setDeleteMode();
		// }
		return view;
	}

	private class MediaAdapter extends BaseAdapter {
		@Override
		public long getItemId(int id) {
			return id;
		}

		@Override
		public int getCount() {
			return mEmbedList.getEmbeds().size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			return null;
		}
	}

	@Override
	public void launchCamera() {
		// TODO Auto-generated method stub

	}

	@Override
	public void launchGallery() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearMediaList() {
		// TODO Auto-generated method stub

	}

	@Override
	public void retrieveUserMedia() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setForReturn(int mMediaQuickReturn) {
		mSelectForReturn = mMediaQuickReturn;
	}

}
