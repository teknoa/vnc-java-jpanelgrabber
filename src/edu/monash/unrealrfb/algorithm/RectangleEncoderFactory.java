package edu.monash.unrealrfb.algorithm;

import java.awt.Image;
import java.awt.image.PixelGrabber;

import edu.monash.unrealrfb.server.constants.rfb;

public class RectangleEncoderFactory {

	//
	// Static operations
	//

	public static int bestEncoding( int[] encodings )
	{
		for( int i = 0; i < encodings.length; i++ )
		{
			switch( encodings[i] )
			{
			case rfb.EncodingRaw:
			case rfb.EncodingRRE:
			case rfb.EncodingCoRRE:
			case rfb.EncodingHextile:
				return encodings[i];
			}
		}

		// List does not include a supported encoding
		return rfb.EncodingHextile;
	}

	public static Rectangle encode( int encoding, Image image, int x, int y, int w, int h )
	{

		// Grab pixels
		int pixels[] = new int[ w * h ];
		PixelGrabber grabber = new PixelGrabber( image, x, y, w, h, pixels, 0, w );
		try
		{
			grabber.grabPixels();
		}
		catch( InterruptedException e )
		{
		}
		return encode( encoding, pixels, x, y, w, x, y, w, h );
	}

	public static Rectangle encode( int encoding, int[] pixels, int scanline, int x, int y, int w, int h )
	{
		return encode( encoding, pixels, 0, 0, scanline, x, y, w, h );
	}

	public static Rectangle encode( int encoding, int[] pixels, int offsetX, int offsetY, int scanline, int x, int y, int w, int h )
	{
//         System.err.println("DEBUG[Rect] encode("+w+" x "+h+") pixels[0]="+pixels[0]);
         if(w==0)
         if(h==0)
           {
            Exception e = new Exception("w==h==0");
            e.printStackTrace();
           }
		switch( encoding )
		{
		case rfb.EncodingRaw:
			return new Raw( pixels, offsetX, offsetY, scanline, x, y, w, h );
		case rfb.EncodingRRE:
			return new RRE( pixels, offsetX, offsetY, scanline, x, y, w, h );
		case rfb.EncodingDEFLATE:
			return new GZIPRectangle(pixels, offsetX, offsetY, scanline, x, y, w, h);
		default:
			return null;
		}
	}

}