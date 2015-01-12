package edu.monash.unrealrfb.algorithm;

import java.io.DataOutput;
import java.io.IOException;

public interface Rectangle {

	public int getEncodingType();
	
	public void writeData(DataOutput output) throws IOException;

	public int getDataSize();

	public void transform(int transformX, int transformY);

	public int getWidth();
	
	public int getHeight();
	
}