package vncjdemo;

import gnu.rfb.server.DefaultRFBAuthenticator;
import gnu.rfb.server.RFBAuthenticator;
import gnu.rfb.server.RFBHost;
import gnu.vnc.pixels.VNCPixels;

import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class VNCPanel extends JPanel
implements ComponentListener{

	private RFBHost rfbHost;

	private VNCPixels vncPixels;

	BufferedImage bi;

	private int[] pixels;


	private int mergedpixels[];

	private int[] rawpixelarrays;

	
	public VNCPanel() {
		addComponentListener(this);
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				startServer();
			}
		});
	}
	
	public void startServer() {
		if(rfbHost != null)
			return;
		System.out.println( "starting server");
		int display = 0;
		String serverClassName = "gnu.vnc.pixels.VNCPixels";
		String displayName = "Drawing Test";

		RFBAuthenticator authenticator = new DefaultRFBAuthenticator("password");

		System.out.println( displayName );

		vncPixels = new VNCPixels(displayName, getWidth(), getHeight());
		initBuffers(getWidth(), getHeight());
//		mergedpixels = new int[getWidth() * getHeight()];
//		for(int i = 0; i < getWidth() * getHeight(); i++)
//			mergedpixels[i] = 0x00ff8811; 
//		
//		vncPixels.setPixelArray(mergedpixels, getWidth(), getHeight());

		// RFB host
		rfbHost = new RFBHost( display, displayName, vncPixels, authenticator );

		System.out.println( "  VNC display " + display );
		System.out.println( "  Web server on port " + ( 5900 + display ) );
		System.out.println( "  Class: " + serverClassName );

	}
	
	void initBuffers(int width, int height) {
		
		if(width == 0)
			width = 30;
		if(height == 0)
			height = 30;
		System.out.println("initbuffers: size "+width * height +" "+width+" "+height);
		bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int num = width * height * bi.getRaster().getNumBands();
		rawpixelarrays = new int[num];
		pixels = bi.getRaster().getPixels(0, 0, width, height, rawpixelarrays);
		mergedpixels = new int[width * height];
		vncPixels.setPixelArray(mergedpixels, width, height);
	}
	
	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		if(bi == null) {
			initBuffers(getWidth(), getHeight());
		}
		Graphics big = bi.getGraphics();
		
		super.paint(big);
		
		g.drawImage(bi, 0, 0, null);
		
		System.out.println("copying: size "+getWidth() * getHeight() +" "+getWidth()+" "+getHeight());
		pixels = bi.getRaster().getPixels(0, 0, getWidth(), getHeight(), rawpixelarrays);
		for(int i = 0; i < getWidth() * getHeight(); i++) {
			int b = 0;
			b |= pixels[i];
			mergedpixels[i] = 0;
			mergedpixels[i] |= pixels[3*i+2]; 
			mergedpixels[i] |= pixels[3*i+1] << 8; 
			mergedpixels[i] |= pixels[3*i] << 16;
		}


		vncPixels.getQueue().takeSnapshot(vncPixels);

	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		bi = null;
//		startServer();
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		
	}

	/**
	 * component listener
	 * 
	 */
	
}
