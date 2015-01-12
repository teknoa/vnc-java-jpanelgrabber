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
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import edu.monash.unrealrfb.algorithm.Colour;
import edu.monash.unrealrfb.algorithm.PixelFrame;
import edu.monash.unrealrfb.algorithm.Rectangle;
import edu.monash.unrealrfb.server.constants.rfb;
import edu.monash.unrealrfb.server.interfaces.RFBClient;
import edu.monash.unrealrfb.server.interfaces.RFBServer;

public class JPanelRFBServer implements RFBServer, PixelsOwner
{
	Logger logger = Logger.getLogger(JPanelRFBServer.class);
	
	PixelFrame pixelframe;
	
	
	RFBSocket socket;
	
	byte clientID = 1;
	
	static byte globalPanelID = 0;
	
	byte panelID;
	
	/**
	 * VNCEvents to forward events to
	 */
	private VNCEvents events = null;

	public void setVNCEventsHandler(VNCEvents events)
	{
		this.events = events;
	}

	public VNCEvents getVNCEventsHandler()
	{
		return events;
	}



	//
	// Construction
	//

	public JPanelRFBServer( String name, String hostname, int portnumber)
	{
		this.name = name;

		this.panelID = globalPanelID++;
		
		queue = new FrameBuffer(rfb.EncodingDEFLATE);

		Socket inetsocket;
		try {
			System.out.println("creating socket for " + hostname +":"+portnumber);
			inetsocket = new Socket(hostname, portnumber);
			socket = new RFBSocket(inetsocket, this);
			logger.debug("started RFB socket");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	

	public byte getClientID() {
		return clientID;
	}

	public void setClientID(byte clientID) {
		this.clientID = clientID;
	}

	public byte getPanelID() {
		return panelID;
	}

	public void setPanelID(byte panelID) {
		this.panelID = panelID;
	}

	/**
	 * externalize queue to manually add entries
	 * @return
	 */
	public FrameBuffer getQueue()
	{
		return(queue);
	}

	//
	// Operations
	//

	public void dispose()
	{
	}

	//
	// RFBServer
	//
	public void close() {
		if(socket != null)
		socket.close();
	}
	
	
	// Attributes

	public String getDesktopName( )
	{
		return name;
	}

	@Override
	public void addPixelFrame(PixelFrame frame) {
		getQueue().push(frame);
	}

	@Override
	public void addPixels(int[] pixels, int width, int height) {
		
		addPixelFrame(new PixelFrame(width,height,pixels));
		
	}

	
	
	// Messages from client to server

	@Override
	public boolean hasFrames() {
		// TODO Auto-generated method stub
		return !getQueue().isEmpty();
	}

	public void setClientProtocolVersionMsg( RFBClient client, String protocolVersionMsg ) throws IOException
	{
	}


	public void setEncodings( RFBClient client, int[] encodings ) throws IOException
	{
		client.setPreferredEncoding( rfb.EncodingRRE );
	}

	public void fixColourMapEntries( RFBClient client, int firstColour, Colour[] colourMap ) throws IOException
	{
	}

	public void frameBufferUpdateRequest( RFBClient client, boolean incremental, int x, int y, int w, int h ) throws IOException
	{
		Rectangle popEncoded = queue.popEncoded();
		if(popEncoded != null)
			client.writeFrameBufferUpdate(popEncoded );
//		else {
//			if(socket != null)
//				socket.setUpdateIsAvailable(false);
//		}
	}

	public void keyEvent( RFBClient client, boolean down, int key ) throws IOException
	{
		//         System.err.println("DEBUG[VNCPixels] keyEvent");
		if( events!=null)
		{
			events.translateKeyEvent(client, down, key);
		}
		else
		{ 
//			updateAll();
		}
	}

	public void pointerEvent( RFBClient client, int buttonMask, int x, int y ) throws IOException
	{
		//         System.err.println("DEBUG[VNCPixels] pointerEvent");
		if( events!=null) {
			events.translatePointerEvent(client, buttonMask, x, y);
		}
		else
		{
//			updateAll();
		}
	}

	public void clientCutText( RFBClient client, String text ) throws IOException
	{
	}

	//
	// PixelsOwner
	//

	public PixelFrame getPixelFrame()
	{
		return pixelframe;
	}

	public void setPixelFrame(PixelFrame pixelframe)
	{
		this.pixelframe = pixelframe;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Private

	private String name;
	protected FrameBuffer queue;
}

