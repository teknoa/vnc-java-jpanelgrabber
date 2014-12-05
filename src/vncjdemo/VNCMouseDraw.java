package vncjdemo;

import gnu.rfb.server.DefaultRFBAuthenticator;
import gnu.rfb.server.RFBAuthenticator;
import gnu.rfb.server.RFBHost;
import gnu.vnc.WebServer;
import gnu.vnc.pixels.VNCPixels;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.Authenticator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class VNCMouseDraw extends JPanel{

	boolean mouseClicked = false;

	MouseEvent lastMouseEvent;

	private static JFrame frame;

	private RFBHost rfbHost;

	private VNCPixels vncPixels;

	BufferedImage bi;

	private int[] pixels;

	private static VNCMouseDraw vncMouseDraw;

	public VNCMouseDraw() {



	}

	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		//		super.paintComponent(g);

		if(mouseClicked) {
			mouseClicked = false;
			
			bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			
			Graphics big = bi.getGraphics();
			
			big.drawRect(lastMouseEvent.getX(), lastMouseEvent.getY(), 10,10);

			g.drawImage(bi, 0, 0, null);
			
			
			int num = getWidth() * getHeight() * bi.getRaster().getNumBands();
			int[] array = new int[num];
			pixels = bi.getRaster().getPixels(0, 0, getWidth(), getHeight(), array);
			vncPixels.setPixelArray(pixels, getWidth(), getHeight());

			
			vncPixels.getQueue().takeSnapshot(vncPixels);
			
			
		}
	}

	void setupServer() {
		MyMouseAdapter ma = new MyMouseAdapter();
		addMouseListener(ma);
		addMouseMotionListener(ma);

		bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		int num = getWidth() * getHeight() * bi.getRaster().getNumBands();
		int[] array = new int[num];
		pixels = bi.getRaster().getPixels(0, 0, getWidth(), getHeight(), array);


		int display = 0;
		String serverClassName = "gnu.vnc.pixels.VNCPixels";
		String displayName = "Drawing Test";

		RFBAuthenticator authenticator = new DefaultRFBAuthenticator("password");

		System.out.println( displayName );

		vncPixels = new VNCPixels(displayName, getWidth(), getHeight());
		vncPixels.setPixelArray(pixels, getWidth(), getHeight());

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
			lastMouseEvent = e;
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			super.mouseDragged(e);
		}



	}


	public static void main(String[] args) {
		frame = new JFrame("vnc drawing test");
		frame.setSize(800, 600);
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
