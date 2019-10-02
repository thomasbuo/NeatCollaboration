package network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import data.Constants;

public class Genome {

	public static final float EXCESS_WEIGHT = 1;
	public static final float DISJOINT_WEIGHT = 1;
	public static final float MATCHING_WEIGHT = 0.4f;
	
	private ArrayList<ConnectionGene> connections;
	private float fitness;

	public Genome(ArrayList<ConnectionGene> connections) {
		this.connections = connections;
	}

	public Genome() {
		connections = new ArrayList<>();
	}

	public Genome breed(Genome p) {
		if (p.fitness > fitness) {
			return breed(p, this, false);
		}
		if (p.fitness < fitness) {
			return breed(this, p, false);
		}
		return breed(this, p, true);
	}

	private Genome breed(Genome betterParent, Genome worseParent, boolean randomize) {
		ArrayList<ConnectionGene> betterParentConnections = new ArrayList<>();
		ArrayList<ConnectionGene> worseParentConnections = new ArrayList<>();

		for (ConnectionGene cg : betterParent.getConnections()) {
			betterParentConnections.add(cg.copy());
		}
		for (ConnectionGene cg : worseParent.getConnections()) {
			worseParentConnections.add(cg.copy());
		}

		HashMap<Integer, ConnectionGene> betterParentMatchings = new HashMap<>();
		HashMap<Integer, ConnectionGene> worseParentMatchings = new HashMap<>();
		int matchingIndex = 0;
		for (ConnectionGene bpc : betterParentConnections) {
			if ((matchingIndex = worseParentConnections.indexOf(bpc)) >= 0) {
				betterParentMatchings.put(bpc.getInnovationNumber(), bpc);
				ConnectionGene wpc = worseParentConnections.get(matchingIndex);
				worseParentMatchings.put(wpc.getInnovationNumber(), wpc);
			}
		}

		ArrayList<ConnectionGene> childConnections = new ArrayList<>();
		for (int kcg : betterParentMatchings.keySet()) {
			ConnectionGene childCg = betterParentMatchings.get(kcg).copy();
			if (Constants.rand.nextBoolean()) {
				childCg.setWeight(betterParentMatchings.get(kcg).getWeight());
			} else {
				childCg.setWeight(worseParentMatchings.get(kcg).getWeight());
			}
			childConnections.add(childCg);
		}

		betterParentConnections.removeAll(betterParentMatchings.keySet().stream().map(i -> betterParentMatchings.get(i))
				.collect(Collectors.toList()));
		worseParentConnections.removeAll(worseParentMatchings.keySet().stream().map(i -> betterParentMatchings.get(i))
				.collect(Collectors.toList()));

		if (randomize) {
			for (ConnectionGene cg : betterParentConnections) {
				if (Constants.rand.nextBoolean())
					childConnections.add(cg);
			}
			for (ConnectionGene cg : worseParentConnections) {
				if (Constants.rand.nextBoolean())
					childConnections.add(cg);
			}
		} else {
			childConnections.addAll(betterParentConnections);
		}

		return new Genome(childConnections);
	}

	public float computeDistance(Genome genome) {
		
		ArrayList<ConnectionGene> connectionsGenome1 = getConnections();
		ArrayList<ConnectionGene> connectionsGenome2 = genome.getConnections();
		
		int maxInnovation = 0;
		Genome maxGenome = null;
		Genome minGenome = null;
		
		for (ConnectionGene cg : connectionsGenome1) {
			if (cg.getInnovationNumber() > maxInnovation) {
				maxInnovation = cg.getInnovationNumber();
				maxGenome = this;
				minGenome = genome;
			}
		}
		for (ConnectionGene cg : connectionsGenome2) {
			if (cg.getInnovationNumber() > maxInnovation) {
				maxInnovation = cg.getInnovationNumber();
				maxGenome = genome;
				minGenome = this;
			}
		}
		
		HashMap<Integer, ConnectionGene> maxGenomeMatchings = new HashMap<>();
		HashMap<Integer, ConnectionGene> minGenomeMatchings = new HashMap<>();
		int matchingIndex = 0;
		for (ConnectionGene maxgc : maxGenome.getConnections()) {
			if ((matchingIndex = minGenome.getConnections().indexOf(maxgc)) >= 0) {
				maxGenomeMatchings.put(maxgc.getInnovationNumber(), maxgc);
				ConnectionGene mingc = minGenome.getConnections().get(matchingIndex);
				minGenomeMatchings.put(mingc.getInnovationNumber(), mingc);
			}
		}
		
		List<ConnectionGene> nonMatchingsConnections1 = minGenome.getConnections().stream().collect(Collectors.toList());
		nonMatchingsConnections1.removeAll(maxGenomeMatchings.values());
		
		Collections.sort(maxGenome.getConnections());
		
		int excessCount = 0;
		for (int i = maxGenome.getConnections().size() - 1; i > 0; i--) {
			int min = maxGenome.getConnections().get(i - 1).getInnovationNumber();
			int max = maxGenome.getConnections().get(i).getInnovationNumber();
			if (minGenome.getConnections().stream().anyMatch(c -> c.getInnovationNumber() > min && c.getInnovationNumber() < max)) {
				excessCount = maxGenome.getConnections().size() - 1 - i;
			}
		}
		
		int disjointCount = (minGenome.getConnections().size() - maxGenomeMatchings.size())
							+ (maxGenome.getConnections().size() - maxGenomeMatchings.size() - excessCount);
		
		float weightAverage = 0;
		if (!maxGenomeMatchings.isEmpty()) {
			float sum = 0;
			for (Integer innovationKey : maxGenomeMatchings.keySet()) {
				sum+= Math.abs(maxGenomeMatchings.get(innovationKey).getWeight() - minGenomeMatchings.get(innovationKey).getWeight());
			}
			weightAverage = sum / maxGenomeMatchings.size();
		}
		
		return excessCount * EXCESS_WEIGHT + disjointCount * DISJOINT_WEIGHT + weightAverage * MATCHING_WEIGHT;
	}

	public float getFitness() {
		return fitness;
	}

	public void setFitness(float fitness) {
		this.fitness = fitness;
	}

	public ArrayList<ConnectionGene> getConnections() {
		return connections;
	}

	@Override
	public String toString() {
		return connections.toString();
	}
}
