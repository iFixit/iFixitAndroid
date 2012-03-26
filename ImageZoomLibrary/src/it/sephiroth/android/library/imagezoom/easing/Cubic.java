package it.sephiroth.android.library.imagezoom.easing;

public class Cubic {
	
	public static float easeOut( float time, float start, float end, float duration )
	{
		return end * ( ( time = time / duration - 1 ) * time * time + 1 ) + start;
	}
}
