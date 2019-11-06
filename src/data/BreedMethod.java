package data;

import java.util.ArrayList;

import network.Core;
import network.Genome;
import network.Species;

public enum BreedMethod {
	
	PERCENTILE {
		float sum = 0;
		@Override
		public void breed(Core c) {
			ArrayList<Genome> genomes = new ArrayList<>();
			while (genomes.size() + c.getGenomes().size() < c.getPopulationSize()) {
				Species s = c.getSpecies().get(Constants.rand.nextInt(c.getSpecies().size()));
				Genome parent1 = selectParent(s);
				Genome parent2 = null;
				if (Constants.rand.nextFloat() < INTERSPECIES) {
					Species newS = s;
					while (newS == s) {
						newS = c.getSpecies().get(Constants.rand.nextInt(c.getSpecies().size()));
					}
					parent2 = selectParent(newS);
				} else {
					parent2 = selectParent(s);
				}
				genomes.add(parent1.breed(parent2));
			}
		}
		
		private Genome selectParent(Species s) {
			ArrayList<Genome> genomes = s.getGenomes();
			genomes.forEach(g -> sum += g.getFitness());
			float randFitness = Constants.rand.nextFloat() * sum;
			float currentSum = 0;
			for (Genome g : genomes) {
				currentSum += g.getFitness();
				if (currentSum >= randFitness) {
					sum -= g.getFitness();
					return g;	
				}
			}
			return null;
		}
	};

	public static final float INTERSPECIES = 0.001f;
	
	public abstract void breed(Core c);
	
}
