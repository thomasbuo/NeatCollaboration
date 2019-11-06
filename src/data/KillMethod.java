package data;

import java.util.ArrayList;
import java.util.Collections;

import network.Core;
import network.Genome;

public enum KillMethod {

	BOTTOM_HALF {
		@Override
		public void kill(Core c) {
			ArrayList<Genome> genomes = c.getGenomes();
			Collections.sort(genomes);
			int size = genomes.size();
			for (int i = 0; i < size / 2; i++) {
				genomes.remove(0);
			}
		}
	},
	RANDOM_UNIFORM {
		@Override
		public void kill(Core c) {
			ArrayList<Genome> genomes = c.getGenomes();
			int size = genomes.size();
			for (int i = 0; i < size / 2; i++) {
				genomes.remove(Constants.rand.nextInt(genomes.size()));
			}
		}
	},
	PERCENTILE {
		float sum = 0;
		@Override
		public void kill(Core c) {
			ArrayList<Genome> genomes = c.getGenomes();
			genomes.forEach(g -> sum += g.getFitness());
			int size = genomes.size();
			for (int i = 0; i < size / 2; i++) {
				float randFitness = Constants.rand.nextFloat() * sum;
				float currentSum = 0;
				for (Genome g : genomes) {
					currentSum += g.getFitness();
					if (currentSum >= randFitness) {
						sum -= g.getFitness();
						genomes.remove(g);
						break;
					}
				}
			}
		}
	};
	
	public abstract void kill(Core c); 
	
}
