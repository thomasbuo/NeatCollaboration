package test;

import java.util.ArrayList;
import java.util.Collections;

import data.ActivationFunction;
import data.BreedMethod;
import data.KillMethod;
import data.MutateMethod;
import data.NEATHeuristic;
import network.Core;
import network.Genome;

//import network.Core;

public class Tester {

	public static void main(String[] args) {
		Core core = new Core();
		core.initialize(2, 1, ActivationFunction.LINEAR, ActivationFunction.SIGMOID,
				KillMethod.BOTTOM_HALF, BreedMethod.PERCENTILE, MutateMethod.PRESERVE_OLD,
				new NEATHeuristic() {
					ArrayList<ArrayList<Float>> inputs = new ArrayList<>();
					ArrayList<Float> outputs = new ArrayList<>();
					public NEATHeuristic initialize() {
						//OR FUNCTION
						ArrayList<Float> in = new ArrayList<>();
						in.add(0f);
						in.add(0f);
						outputs.add(0f);
						inputs.add(in);
						in = new ArrayList<>();
						in.add(0f);
						in.add(1f);
						outputs.add(1f);
						inputs.add(in);
						in = new ArrayList<>();
						in.add(1f);
						in.add(0f);
						outputs.add(1f);
						inputs.add(in);
						in = new ArrayList<>();
						in.add(1f);
						in.add(1f);
						outputs.add(1f);
						inputs.add(in);
						return this;
					}
					
					@Override
					public float computeFitness(Genome genome) {
						float errorSum = 0;
						for (int i = 0; i < inputs.size(); i++) {
							errorSum += (float)Math.abs(genome.computeOutput(inputs.get(i)).get(0) - outputs.get(i));
						}
						return 4 - errorSum;
					}
					@Override
					public boolean checkStoppingCriteria(Core core) {
						Genome best = Collections.max(core.getGenomes());
//						System.out.println(best);
						return best.getFitness() >= 3.8;
					}
		}.initialize(), 100, 3);
	}
	
}
