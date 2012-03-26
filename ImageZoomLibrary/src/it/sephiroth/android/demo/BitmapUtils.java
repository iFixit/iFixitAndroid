package it.sephiroth.android.demo;

import android.graphics.Bitmap;

public class BitmapUtils {
	
	public static Bitmap resizeBitmap( Bitmap input, int destWidth, int destHeight )
	{
		int srcWidth = input.getWidth();
		int srcHeight = input.getHeight();
		boolean needsResize = false;
		float p;
		if ( srcWidth > destWidth || srcHeight > destHeight ) {
			needsResize = true;
			if ( srcWidth > srcHeight && srcWidth > destWidth ) {
				p = (float)destWidth / (float)srcWidth;
				destHeight = (int)( srcHeight * p );
			} else {
				p = (float)destHeight / (float)srcHeight;
				destWidth = (int)( srcWidth * p );
			}
		} else {
			destWidth = srcWidth;
			destHeight = srcHeight;
		}
		if ( needsResize ) {
			Bitmap output = Bitmap.createScaledBitmap( input, destWidth, destHeight, true );
			return output;
		} else {
			return input;
		}
	}
}
