package edu.monash.unrealrfb.algorithm;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

import edu.monash.unrealrfb.server.constants.rfb;

public class GZIPRectangle extends SimpleRectangle{

	byte[] compressedbytearray;
	
	public GZIPRectangle( int[] pixels, int offsetX, int offsetY, int scanline, int x, int y, int w, int h )
	{
		super( x, y, w, h );
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			for(int i = 0; i < pixels.length; i++)
				dos.writeInt(pixels[i]);
			dos.flush();
			byte[] uncompressedbytearray = bos.toByteArray();
			ByteArrayOutputStream compressedOS = new ByteArrayOutputStream();
			DeflaterOutputStream deflaterOS = new DeflaterOutputStream(compressedOS);
			deflaterOS.write(uncompressedbytearray);
			deflaterOS.finish();
			deflaterOS.close();
			compressedbytearray = compressedOS.toByteArray();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void writeData(DataOutput output) throws IOException {
		output.write(compressedbytearray);
	}

	@Override
	public int getDataSize() {
		System.out.println("GZIPRectangle compressed data size:"+compressedbytearray.length);
		
		return compressedbytearray.length;
	}
	
	
	@Override
	public int getEncodingType() {
		// TODO Auto-generated method stub
		return rfb.EncodingDEFLATE;
	}

}
