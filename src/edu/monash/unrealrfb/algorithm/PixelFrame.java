package edu.monash.unrealrfb.algorithm;

public class PixelFrame {

	public int width;
	public int height;
	
	public int[] pixelarray;

	public PixelFrame(int width, int height, int[] pixelarray) {
		this.width = width;
		this.height = height;
		this.pixelarray = new int[pixelarray.length];
		for(int i = 0; i < pixelarray.length; i++)
			this.pixelarray[i] = pixelarray[i];
	}
	
	public void resize(int width, int height) {
		if(width * height > pixelarray.length){
			int[] new_array = new int[width * height];
			for(int i = 0; i < pixelarray.length; i++)
				new_array[i] = pixelarray[i];
			pixelarray = new_array;
		}
		this.width = width;
		this.height = height;
	}
}
