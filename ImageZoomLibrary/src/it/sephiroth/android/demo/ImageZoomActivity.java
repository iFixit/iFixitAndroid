package it.sephiroth.android.demo;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import java.io.IOException;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class ImageZoomActivity extends Activity {
	
	private ImageViewTouch	mImageView;
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView( R.layout.main );
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
		selectRandomImage();
	}
	
	@Override
	public void onContentChanged()
	{
		super.onContentChanged();
		mImageView = (ImageViewTouch)findViewById( R.id.imageView1 );
	}
	
	/**
	 * pick a random image from your library
	 * and display it
	 */
	public void selectRandomImage()
	{
		Cursor c = getContentResolver().query( Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null );
		if ( c != null ) {
			int count = c.getCount();
			int position = (int)( Math.random() * count );
			if ( c.moveToPosition( position ) ) {
				long id = c.getLong( c.getColumnIndex( Images.Media._ID ) );
				int orientation = c.getInt( c.getColumnIndex( Images.Media.ORIENTATION ) );
				
				Uri imageUri = Uri.parse( Images.Media.EXTERNAL_CONTENT_URI + "/" + id );
				Bitmap bitmap;
				try {
					bitmap = ImageLoader.loadFromUri( this, imageUri.toString(), 1024, 1024 );
					mImageView.setImageBitmapReset( bitmap, orientation, true );
				}
				catch ( IOException e ) {
					Toast.makeText( this, e.toString(), Toast.LENGTH_LONG ).show();
				}
			}
			c.close();
			c = null;
			return;
		}
	}
	
}
