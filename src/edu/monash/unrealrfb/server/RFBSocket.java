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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import edu.monash.unrealrfb.algorithm.Rectangle;
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
	OutputStream outputstream;
	
	private String protocolVersionMsg = "";
	private boolean shared = true;
	private int[] encodings = new int[0];
	private int preferredEncoding = rfb.EncodingRRE;
	private boolean isRunning = false;
	private boolean threadFinished = false;
	private boolean updateAvailable = true;

	private int version = 0x01;

	private short compressiontype = rfb.EncodingRRE;


	/**
	 * new constructor by Marcus Wolschon
	 */
	public RFBSocket( Socket socket, RFBServer server) throws IOException {
		this.socket = socket;
		this.server = server;
		// Streams
		input = new DataInputStream( new BufferedInputStream( socket.getInputStream() ) );
//		output = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream(), 16384 ) );
		outputstream = socket.getOutputStream();
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
		output = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream() ) );

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
	int packetid=0;
	public synchronized void writeFrameBufferUpdate( Rectangle rect ) throws IOException {

//		logger.debug("writing framebuffer update to output");

		int size = 0;
//		size += 4; //packetId
		size += 6; //version + compresseiontype
		size += 4; //2byte each for W/H of frame
		size +=2; //1byte each for clientid and panelid
		size += rect.getDataSize();
		byte[] outputbuf;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(size + 4);
		DataOutputStream dataout = new DataOutputStream(byteArrayOutputStream);
	
		logger.debug("package content size: " + size);
		dataout.writeInt(size);
		dataout.writeInt(version);
		dataout.writeShort(rect.getEncodingType());

		dataout.writeShort(rect.getWidth());
		dataout.writeShort(rect.getHeight());

		dataout.writeByte(server.getClientID());
		dataout.writeByte(server.getPanelID());

		rect.writeData( dataout );
		outputbuf = byteArrayOutputStream.toByteArray();
		logger.debug("flushing stream with bufferlength: "+outputbuf.length);
		
		System.out.print("first bytes of package: ");
		for(int i = 0; i < 16; i++)
			System.out.printf("%02x:",outputbuf[i]);
		System.out.println();
		
		System.out.print(" last bytes of package: ");
		for(int i = 0; i < 16; i++)
			System.out.printf("%02x:",outputbuf[outputbuf.length-16+i]);
		System.out.println();
		outputstream.write(outputbuf);
//		outputstream.flush();
//		logger.debug("writing frambuffer done..");
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
			if(output != null)
				output.close();
			if(input != null)
				input.close();
			if(socket != null)
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
			int i = 0;
			while( isRunning ) {
//				System.out.print("looping..");
				if(getUpdateIsAvailable()){
					// go ahead and send the updates
					System.out.println("frame available doing frame buffer update");

					doFrameBufferUpdate();
					
					int readUnsignedByte;
					do{
						System.out.print("...reading remotedisplay status: ");
						readUnsignedByte = input.readUnsignedByte();
						System.out.print("..result: " + readUnsignedByte);
						if(readUnsignedByte == 0) {
//							try{
								System.out.println(".......waiting for display ...");
//								Thread.currentThread().sleep(50);
//							}
//							catch(InterruptedException x){
//							}
						} else
							System.out.println(".......display ready...");
					}while(readUnsignedByte == 0);
				} else               
//				if(input.available() == 0){
					try{
						Thread.sleep(50);
//						System.out.print(".");
//						if(i++ % 120 == 0) System.out.println();
					}
					catch(InterruptedException x){
					}
//				}
//				else{

					
//				}


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



	//	public void setUpdateIsAvailable(boolean value) {
	//		updateAvailable = value;
	//	}

	public boolean getUpdateIsAvailable() {
		return server.hasFrames();
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
