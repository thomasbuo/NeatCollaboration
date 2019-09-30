package data;

import java.util.Random;

public class Constants {

	public static Random rand;
	
	public static void initialize(int seed) {
		rand = new Random(seed);
	}
	
	
}
