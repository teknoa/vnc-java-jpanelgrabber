package vncjdemo;

import gnu.rfb.server.DefaultRFBAuthenticator;
import gnu.rfb.server.RFBAuthenticator;
import gnu.rfb.server.RFBHost;
import gnu.vnc.WebServer;
import gnu.vnc.pixels.VNCPixels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.net.Authenticator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class VNCMouseDraw extends JPanel{

	boolean mouseClicked = false;
	private boolean clearscreen;

	MouseEvent lastMouseEvent;

	private static JFrame frame;

	private RFBHost rfbHost;

	private VNCPixels vncPixels;

	BufferedImage bi;

	private int[] pixels;

	private static VNCMouseDraw vncMouseDraw;

	private int mergedpixels[];

	private int[] rawpixelarrays;

	public VNCMouseDraw() {



	}

	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		//		super.paintComponent(g);

//		if(mouseClicked) {
			mouseClicked = false;
			if(bi == null)
			{	
//				bi = createImage(getWidth(), getHeight());
				bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
				int num = getWidth() * getHeight() * bi.getRaster().getNumBands();
				rawpixelarrays = new int[num];
				
			}

			Graphics big = bi.getGraphics();
			Graphics2D g2d = (Graphics2D)big;
			g2d.setBackground(new Color(255,128,1));
			if(clearscreen)
				g2d.clearRect(0, 0, getWidth(), getHeight());
			clearscreen = false;
			g2d.setColor(Color.red);
			g2d.fillRect(lastMouseEvent.getX()-5, lastMouseEvent.getY()-5, 10,10);
			g.drawImage(bi, 0, 0, null);

			pixels = bi.getRaster().getPixels(0, 0, getWidth(), getHeight(), rawpixelarrays);
			for(int i = 0; i < getWidth() * getHeight(); i++) {
				int b = 0;
				b |= pixels[i];
				mergedpixels[i] = 0;
				mergedpixels[i] |= pixels[3*i+2]; 
				mergedpixels[i] |= pixels[3*i+1] << 8; 
				mergedpixels[i] |= pixels[3*i] << 16;
			}
//			vncPixels.setPixelArray(mergedpixels, getWidth(), getHeight());


			vncPixels.getQueue().takeSnapshot(vncPixels);


//		}
	}

	public BufferedImage createImage( int width, int height )
	{
		//System.err.println( "createImage("+width+","+height+")" );

		// Color model
		DirectColorModel colorModel = (DirectColorModel) getColorModel();

		// Sample model
		SampleModel sampleModel = new SinglePixelPackedSampleModel( DataBuffer.TYPE_INT, width, height, colorModel.getMasks() );

		// Raster
		WritableRaster raster = Raster.createWritableRaster( sampleModel, null );

		// Image
		return new BufferedImage( colorModel, raster, true, null );
	}

	
	void setupServer() {
		MyMouseAdapter ma = new MyMouseAdapter();
		addMouseListener(ma);
		addMouseMotionListener(ma);

		bi = createImage(getWidth(), getHeight());
		int num = getWidth() * getHeight() * bi.getRaster().getNumBands();
		int[] array = new int[num];
		pixels = bi.getRaster().getPixels(0, 0, getWidth(), getHeight(), array);


		int display = 0;
		String serverClassName = "gnu.vnc.pixels.VNCPixels";
		String displayName = "Drawing Test";

		RFBAuthenticator authenticator = new DefaultRFBAuthenticator("password");

		System.out.println( displayName );

		vncPixels = new VNCPixels(displayName, getWidth(), getHeight());
		mergedpixels = new int[getWidth() * getHeight()];
		for(int i = 0; i < getWidth() * getHeight(); i++)
			mergedpixels[i] = 0x00ff8811; 
		
		vncPixels.setPixelArray(mergedpixels, getWidth(), getHeight());

		// RFB host
		rfbHost = new RFBHost( display, displayName, vncPixels, authenticator );

		System.out.println( "  VNC display " + display );
		System.out.println( "  Web server on port " + ( 5900 + display ) );
		System.out.println( "  Class: " + serverClassName );
	}

	class MyMouseAdapter extends MouseAdapter {

		int prevx;
		int prevy;

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			super.mouseClicked(e);
			mouseClicked = true;
			clearscreen = e.getButton() == MouseEvent.BUTTON3 ? true : false;
			lastMouseEvent = e;
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			super.mouseDragged(e);
			lastMouseEvent = e;
			repaint();
		}



	}


	public static void main(String[] args) {
		frame = new JFrame("vnc drawing test");
		frame.setSize(600, 600);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		vncMouseDraw = new VNCMouseDraw();
		frame.getContentPane().add(vncMouseDraw, BorderLayout.CENTER);
		frame.setVisible(true);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				vncMouseDraw.setupServer();

			}
		});
	}
}
