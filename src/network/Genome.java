package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import data.Constants;

public class Genome {

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

		betterParentConnections.removeAll(betterParentMatchings.keySet().stream()
				.map(i -> betterParentMatchings.get(i)).collect(Collectors.toList()));
		worseParentConnections.removeAll(worseParentMatchings.keySet().stream()
				.map(i -> betterParentMatchings.get(i)).collect(Collectors.toList()));

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

		return null;
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
