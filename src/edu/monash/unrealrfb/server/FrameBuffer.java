package edu.monash.unrealrfb.server;

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

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import edu.monash.unrealrfb.algorithm.PixelFrame;
import edu.monash.unrealrfb.algorithm.Rectangle;
import edu.monash.unrealrfb.algorithm.RectangleEncoderFactory;

public class FrameBuffer {

	Logger logger = Logger.getLogger(FrameBuffer.class);
	
	Queue<PixelFrame> frames;

	int encoding;

	public FrameBuffer( int encoding ){
		frames = new LinkedBlockingQueue<PixelFrame>();
		this.encoding = encoding;
	}

	//
	// Operations9
	//

	public void push(int[] pixelarray, int width, int height)  {

		push(new PixelFrame(width, height, pixelarray));
	}

	public void push(PixelFrame frame) {    
		if(frames.size() > 10)
			frames.poll();
		frames.add(frame);
		
//		logger.debug("num of frames in cache " + frames.size());
	}

	public Rectangle popEncoded() throws IOException {
		// Pop
		PixelFrame frame = frames.poll();
		if(frame == null)
			return null;

		// Encode rectangles
		Rectangle rects;
		rects = RectangleEncoderFactory.encode( encoding, frame.pixelarray, frame.width, 0, 0, frame.width, frame.height );

		if( rects == null ) {
			throw new IOException("rects == 0, encoding an empty raw rect would cause blue screen on the official VNC-client (not a Windows(tm)-BlueScreen(tm)).");
		}

		return rects;
	}

	public boolean isEmpty(){
		return frames.isEmpty();
	}
}
