package data;

import network.Core;
import network.Genome;

public abstract class NEATHeuristic {

	public abstract float computeFitness(Genome genome);
	
	public abstract boolean checkStoppingCriteria(Core core);
	
}
