package vncjdemo;

import edu.monash.unrealrfb.algorithm.RRE;

public class RRETest {

	public static void main(String[] args) {
		int[] pixarray = {
				0,0,1,1,0,0,0,1,
				0,0,1,1,1,0,1,1,
				0,1,1,0,0,0,1,1
		};
		RRE rre = new RRE(pixarray, 0, 0, 8, 0, 0, 8, 3);
		
		
	}
}
