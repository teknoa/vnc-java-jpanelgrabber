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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.monash.unrealrfb.algorithm.Colour;
import edu.monash.unrealrfb.algorithm.Rect;
import edu.monash.unrealrfb.server.constants.rfb;
import edu.monash.unrealrfb.server.interfaces.RFBClient;
import edu.monash.unrealrfb.server.interfaces.RFBServer;
import gnu.logging.VLogger;

public class RFBSocket implements RFBClient, Runnable {
	//
	// Construction
	//

	///////////////////////////////////////////////////////////////////////////////////////
	// Private

	Logger logger = Logger.getLogger(RFBSocket.class);
	
	private Socket socket;
	private RFBServer server = null;
	private DataInputStream input;
	private DataOutputStream output;

	private String protocolVersionMsg = "";
	private boolean shared = true;
	private int[] encodings = new int[0];
	private int preferredEncoding = rfb.EncodingRRE;
	private boolean isRunning = false;
	private boolean threadFinished = false;
	private Vector updateQueue=new Vector();
	private boolean updateAvailable = true;


	/**
	 * new constructor by Marcus Wolschon
	 */
	public RFBSocket( Socket socket, RFBServer server) throws IOException {
		this.socket = socket;
		this.server = server;
		// Streams
		input = new DataInputStream( new BufferedInputStream( socket.getInputStream() ) );
		output = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream(), 16384 ) );

		// Start socket listener thread
		new Thread( this ).start();
	}

	/**
	 * new constructor by Marcus Wolschon
	 */
	public RFBSocket( Socket socket, RFBServer server, boolean syncronous ) throws IOException {
		this.socket = socket;
		this.server = server;

		// Streams
		input = new DataInputStream( new BufferedInputStream( socket.getInputStream() ) );
		output = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream(), 16384 ) );

		// Start socket listener thread
		if(syncronous) {
			run();
		}
		else {
			new Thread( this ).start();
		}
	}



	//
	// RFBClient
	//

	// Attributes

	public synchronized String getProtocolVersionMsg() {
		return protocolVersionMsg;
	}

	public synchronized boolean getShared() {
		return shared;
	}

	public synchronized int getPreferredEncoding() {
		return preferredEncoding;
	}

	public synchronized void setPreferredEncoding( int encoding ) {
		if( encodings.length > 0 ) {
			for( int i = 0; i < encodings.length; i++ ) {
				if( encoding == encodings[i] ) {
					// Encoding is supported
					preferredEncoding = encoding;
					return;
				}
			}
		}
		else {
			// No list
			preferredEncoding = encoding;
		}
	}

	public synchronized int[] getEncodings() {
		return encodings;
	}

	// Messages from server to client

	public synchronized void writeFrameBufferUpdate( Rect rects[] ) throws IOException {

		logger.debug("writing framebuffer update to output");

		int i;

		output.writeShort(rects[0].w);
		output.writeShort(rects[0].h);
		
		output.writeByte(server.getClientID());
		output.writeByte(server.getPanelID());
		
//		for( i = 0; i < rects.length; i++ ) {
			rects[0].writeData( output );
//		}

		output.flush();
	}


	public synchronized void writeBell() throws IOException {
		//		writeServerMessageType( rfb.Bell );
	}

	public synchronized void writeServerCutText( String text ) throws IOException {
		//		writeServerMessageType( rfb.ServerCutText );
		output.writeByte( 0 );  // padding
		output.writeShort( 0 ); // padding
		output.writeInt( text.length() );
		output.writeBytes( text );
		output.writeByte( 0 );
		output.flush();
	}

	// Operations

	public synchronized void close(){
		isRunning = false;
		// Block until the thread has exited gracefully
		while(threadFinished == false){
			try{
				Thread.currentThread().sleep(20);
			}
			catch(InterruptedException x){
			}
		}
		try{
			output.close();
			input.close();
			socket.close();
		}
		catch(IOException e){
			VLogger.getLogger().log("Got and exception shutting down RFBSocket ",e);
		}
		finally{
			output=null;
			input=null;
			socket=null;
		}
	}

	//
	// Runnable
	//

	public void run() {
		isRunning =true;
		try {
			//                 System.err.println("DEBUG[RFBSocket] run() calling writeProtocolVersionMsg()");
			// Handshaking
//			writeProtocolVersionMsg();
			//                 System.err.println("DEBUG[RFBSocket] run() calling readProtocolVersionMsg()");
//			readProtocolVersionMsg();
			//                 System.err.println("DEBUG[RFBSocket] run() calling writeAuthScheme()");
			//if(((DefaultRFBAuthenticator)authenticator).authenticate(input,output)==false){
			//			readClientInit();
			//                 System.err.println("DEBUG[RFBSocket] run() calling initServer()");
			//			initServer();
			//                 System.err.println("DEBUG[RFBSocket] run() calling writeServerInit()");
			//                 System.err.println("DEBUG[RFBSocket] run() message loop");

			logger.debug("RFBClient message loop");
			while( isRunning ) {
				
				if(getUpdateIsAvailable()){
					// go ahead and send the updates
//					System.out.println("doing frame buffer update");
					
					doFrameBufferUpdate();
				}                    
				if(input.available() == 0){
					try{
						Thread.currentThread().sleep(10);
					}
					catch(InterruptedException x){
					}
				}
				else{
					
					int readUnsignedByte;
					while((readUnsignedByte = input.readUnsignedByte()) == 0){
						try{
							System.out.println("waiting for display ...");
							Thread.currentThread().sleep(10);
						}
						catch(InterruptedException x){
						}
					}}
				}
		
			
		}
		catch( IOException x ) {
			System.out.println("Got an IOException, drop the client");
		}
		catch(Throwable t){
			t.printStackTrace();
		}

		threadFinished = true;
		close();
	}


	private void initServer() throws IOException {
		server.setClientProtocolVersionMsg( this, protocolVersionMsg );
	}

	// Handshaking

	private synchronized void writeProtocolVersionMsg() throws IOException {
		output.writeBytes( rfb.ProtocolVersionMsg );
		output.flush();
	}

	private synchronized void readProtocolVersionMsg() throws IOException {
		byte[] b = new byte[12];
		input.readFully( b );
		protocolVersionMsg = new String( b );
	}



	private synchronized void doFrameBufferUpdate() throws IOException{

		System.out.println("RFBSocket is doing an update");
		server.frameBufferUpdateRequest( this, true, 0,0,0,0 );
		System.out.println("RFBSocket is done");

	}

	private synchronized void readKeyEvent() throws IOException {
		boolean down = ( input.readUnsignedByte() == 1 );
		input.readUnsignedShort(); // padding
		int key = input.readInt();

		// Delegate to server
		server.keyEvent( this, down, key );
	}

	private synchronized void readPointerEvent() throws IOException {
		int buttonMask = input.readUnsignedByte();
		int x = input.readUnsignedShort();
		int y = input.readUnsignedShort();

		// Delegate to server
		server.pointerEvent( this, buttonMask, x, y );
	}

	private synchronized void readClientCutText() throws IOException {
		input.readUnsignedByte();  // padding
		input.readUnsignedShort(); // padding
		int length = input.readInt();
		byte[] bytes = new byte[ length ];
		input.readFully( bytes );
		String text = new String( bytes );

		// Delegate to server
		server.clientCutText( this, text );
	}
	public InetAddress getInetAddress(){
		return(socket.getInetAddress());
	}



	public void setUpdateIsAvailable(boolean value) {
		updateAvailable = value;
	}

	public boolean getUpdateIsAvailable() {
		return(updateAvailable);
	}

	private class UpdateRequest{
		boolean incremental;
		int x;
		int y;
		int w;
		int h;
		public UpdateRequest(boolean incremental, int x, int y, int w, int h){
			this.incremental=incremental;
			this.x=x;
			this.y=y;
			this.w=w;
			this.h=h;
		}

		public boolean equals(Object obj){
			UpdateRequest u2 = (UpdateRequest)obj;
			return(x==u2.x && y==u2.y && w==u2.w && h==u2.h);
		}

	}

}
