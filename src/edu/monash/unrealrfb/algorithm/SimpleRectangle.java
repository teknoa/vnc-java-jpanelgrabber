package edu.monash.unrealrfb.algorithm;

/**
* <br><br><center><table border="1" width="80%"><hr>
* <strong><a href="http://www.amherst.edu/~tliron/vncj">VNCj</a></strong>
* <p>
* Copyright (C) 2000-2002 by Tal Liron
* <p>
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public License
* as published by the Free Software Foundation; either version 2.1
* of the License, or (at your option) any later version.
* <p>
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* <a href="http://www.gnu.org/copyleft/lesser.html">GNU Lesser General Public License</a>
* for more details.
* <p>
* You should have received a copy of the <a href="http://www.gnu.org/copyleft/lesser.html">
* GNU Lesser General Public License</a> along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
* <hr></table></center>
**/

import java.io.DataOutput;
import java.io.IOException;

public abstract class SimpleRectangle implements Cloneable, Rectangle
{

	//
	// Attributes
	//

	public int x = 0;
	public int y = 0;
	public int w = 0;
	public int h = 0;
	public int count = 1;

	//
	// Construction
	//

	public SimpleRectangle( int x, int y, int w, int h )
	{
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	//
	// Operations
	//

	/* (non-Javadoc)
	 * @see edu.monash.unrealrfb.algorithm.Rectangle#writeData(java.io.DataOutput)
	 */
	@Override
	public void writeData( DataOutput output ) throws IOException
	{
		output.writeShort( x );
		output.writeShort( y );
		output.writeShort( w );
		output.writeShort( h );
	}

	/* (non-Javadoc)
	 * @see edu.monash.unrealrfb.algorithm.Rectangle#getDataSize()
	 */
	@Override
	public int getDataSize(){
		return 8;
	}
	
	/* (non-Javadoc)
	 * @see edu.monash.unrealrfb.algorithm.Rectangle#transform(int, int)
	 */
	@Override
	public void transform( int transformX, int transformY )
	{
		x += transformX;
		y += transformY;
	}

	//
	// Object
	//

	public String toString()
	{
		return String.valueOf( x ) + "," + y + "," + w + "," + h;
	}

	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException( "Rect not cloneable" );
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Private

	protected static int[] copyPixels( int[] pixels, int scanline, int x, int y, int w, int h )
	{
		int size = w * h;
		int[] ourPixels = new int[ size ];
		int jump = scanline - w;
		int s = 0;
		int p = y * scanline + x;
		for( int i = 0; i < size; i++, s++, p++ )
		{
			if( s == w )
			{
				s = 0;
				p += jump;
			}
			ourPixels[i] = pixels[p];
		}

		return ourPixels;
	}

	protected static void writePixel( DataOutput output, int pixel ) throws IOException
	{
			output.writeByte( pixel & 0xFF );
			output.writeByte( ( pixel >> 8 ) & 0xFF );
			output.writeByte( ( pixel >> 16 ) & 0xFF );
			output.writeByte( ( pixel >> 24 ) & 0xFF );
	}

	protected static int getBackground( int pixels[], int scanline, int x, int y, int w, int h )
	{
		return pixels[ y * scanline + x ];

		/*int runningX, runningY, k;
		int counts[] = new int[256];

		int maxcount = 0;
		int maxclr = 0;

		if( bitsPerPixel == 16 )
			return pixels[0];
		else if( bitsPerPixel == 32 )
			return pixels[0];

		// For 8-bit
		return pixels[0];

		for( runningX = 0; runningX < 256; runningX++ )
			counts[runningX] = 0;

		for( runningY = 0; runningY < pixels.length; runningY++ )
		{
			k = pixels[runningY];
			if( k >= counts.length )
			{
				return 0;
			}
			counts[k]++;
			if( counts[k] > maxcount )
			{
				maxcount = counts[k];
				maxclr = pixels[runningY];
			}
		}

		return maxclr;*/
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return w;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return h;
	}
	
	
}
