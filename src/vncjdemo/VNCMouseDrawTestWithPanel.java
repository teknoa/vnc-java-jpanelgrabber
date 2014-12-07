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

public class VNCMouseDrawTestWithPanel extends VNCPanel{

	boolean mouseClicked = false;
	private boolean clearscreen;

	MouseEvent lastMouseEvent;

	private static JFrame frame;

	public VNCMouseDrawTestWithPanel() {
		super();
		MyMouseAdapter myMouseAdapter = new MyMouseAdapter();
		addMouseListener(myMouseAdapter);
		addMouseMotionListener(myMouseAdapter);
	}

	
	@Override
	protected void paintComponent(Graphics g) {

			Graphics2D g2d = (Graphics2D)g;
			g2d.setBackground(new Color(255,128,1));
			if(clearscreen)
				g2d.clearRect(0, 0, getWidth(), getHeight());
			clearscreen = false;
			g2d.setColor(Color.red);
			if(lastMouseEvent != null)
				g2d.fillRect(lastMouseEvent.getX()-5, lastMouseEvent.getY()-5, 10,10);
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
		frame.getContentPane().add(new VNCMouseDrawTestWithPanel(), BorderLayout.CENTER);
		frame.setVisible(true);
		
	}
}
