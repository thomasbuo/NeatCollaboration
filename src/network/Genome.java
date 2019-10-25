package network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import data.Constants;
import network.NodeGene.Layer;

public class Genome {

	public static final float NODE_PROB = 0.03f;
	public static final float CONNECTION_PROB = 0.05f;

	public static final float EXCESS_WEIGHT = 1;
	public static final float DISJOINT_WEIGHT = 1;
	public static final float MATCHING_WEIGHT = 0.4f;

	private ArrayList<NodeGene> nodes;
	private ArrayList<ConnectionGene> connections;
	private float fitness;
	private Core core;

	private HashMap<NodeGene, ArrayList<ConnectionGene>> nodeInputs;
	private HashMap<NodeGene, Float> nodeValues;

	public Genome(Core core, ArrayList<ConnectionGene> connections, ArrayList<NodeGene> nodes,
			HashMap<NodeGene, ArrayList<ConnectionGene>> nodeInputs) {
		this.core = core;
		this.connections = connections;
		this.nodes = nodes;
		this.nodeInputs = nodeInputs;
	}

	public Genome(Core core) {
		this.core = core;
		this.connections = new ArrayList<>();
		this.nodes = new ArrayList<>();
		this.nodeInputs = new HashMap<>();
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

		HashMap<NodeGene, ArrayList<ConnectionGene>> nodeInputs = new HashMap<>();
		ArrayList<ConnectionGene> childConnections = new ArrayList<>();
		for (int kcg : betterParentMatchings.keySet()) {
			ConnectionGene childCg = betterParentMatchings.get(kcg).copy();
			if (Constants.rand.nextBoolean()) {
				childCg.setWeight(betterParentMatchings.get(kcg).getWeight());
			} else {
				childCg.setWeight(worseParentMatchings.get(kcg).getWeight());
			}
			childConnections.add(childCg);

			NodeGene output = childCg.getOutput();
			if (!nodeInputs.containsKey(output)) {
				nodeInputs.put(output, new ArrayList<>());
			}
			nodeInputs.get(output).add(childCg);
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

		ArrayList<NodeGene> nodes = new ArrayList<>();
		for (NodeGene ng : nodeInputs.keySet())
			nodes.add(ng);

		return new Genome(core, childConnections, nodes, nodeInputs);
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

		List<ConnectionGene> nonMatchingsConnections1 = minGenome.getConnections().stream()
				.collect(Collectors.toList());
		nonMatchingsConnections1.removeAll(maxGenomeMatchings.values());

		Collections.sort(maxGenome.getConnections());

		int excessCount = 0;
		for (int i = maxGenome.getConnections().size() - 1; i > 0; i--) {
			int min = maxGenome.getConnections().get(i - 1).getInnovationNumber();
			int max = maxGenome.getConnections().get(i).getInnovationNumber();
			if (minGenome.getConnections().stream()
					.anyMatch(c -> c.getInnovationNumber() > min && c.getInnovationNumber() < max)) {
				excessCount = maxGenome.getConnections().size() - 1 - i;
			}
		}

		int disjointCount = (minGenome.getConnections().size() - maxGenomeMatchings.size())
				+ (maxGenome.getConnections().size() - maxGenomeMatchings.size() - excessCount);

		float weightAverage = 0;
		if (!maxGenomeMatchings.isEmpty()) {
			float sum = 0;
			for (Integer innovationKey : maxGenomeMatchings.keySet()) {
				sum += Math.abs(maxGenomeMatchings.get(innovationKey).getWeight()
						- minGenomeMatchings.get(innovationKey).getWeight());
			}
			weightAverage = sum / maxGenomeMatchings.size();
		}

		return excessCount * EXCESS_WEIGHT + disjointCount * DISJOINT_WEIGHT + weightAverage * MATCHING_WEIGHT;
	}

	public void mutateNodeGene() {
		// Determine whether node should be added
		if (Constants.rand.nextFloat() >= NODE_PROB)
			return;

		ArrayList<NodeGene> unusedNodes = new ArrayList<>();
		List<NodeGene> coreNodes = core.nodes.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());

		// Collect all nodes NOT in this genome, but in core
		for (NodeGene ng : coreNodes) {
			if (!nodes.contains(ng)) {
				unusedNodes.add(ng);
			}
		}
		NodeGene ng = null;
		if (unusedNodes.isEmpty()) {
			// Create a new node and add it to the core list
			ng = new NodeGene(Layer.HIDDEN);
			core.nodes.put(ng.getInnovationNumber(), ng);
			nodes.add(ng);
		} else {
			// Select first node in the list (it's random due to entrySet's random ordering)
			nodes.add(ng = unusedNodes.get(0));
		}

		ConnectionGene cg = connections.get(Constants.rand.nextInt(connections.size()));
		cg.setActive(false);

		ConnectionGene cg1 = new ConnectionGene(cg.getInput(), ng);
		nodeInputs.put(ng, new ArrayList<>());
		nodeInputs.get(ng).add(cg1);
		cg1.setWeight(1);
		ConnectionGene cg2 = new ConnectionGene(ng, cg.getOutput());
		nodeInputs.get(cg.getOutput()).add(cg2);
		cg2.setWeight(cg.getWeight());

		connections.add(cg1);
		connections.add(cg2);
		core.connections.put(cg1.getInnovationNumber(), cg1);
		core.connections.put(cg2.getInnovationNumber(), cg2);
	}

	public void mutateConnectionGene() {
		// Determine whether connectionGene should be added
		if (Constants.rand.nextFloat() >= CONNECTION_PROB)
			return;

		ArrayList<ConnectionGene> unusedConnections = new ArrayList<>();
		List<ConnectionGene> coreConnections = core.connections.entrySet().stream().map(e -> e.getValue())
				.collect(Collectors.toList());

		// Collect all connections NOT in this genome, but in core
		for (ConnectionGene cg : coreConnections) {
			if (!connections.contains(cg)) {
				unusedConnections.add(cg);
			}
		}

		if (unusedConnections.isEmpty()) {
			// Create a new connection and add it to the core list
			List<NodeGene> startNodes = nodes.stream()
					.filter(n -> n.getLayer() == Layer.HIDDEN || n.getLayer() == Layer.INPUT)
					.collect(Collectors.toList());
			List<NodeGene> endNodes = nodes.stream()
					.filter(n -> n.getLayer() == Layer.HIDDEN || n.getLayer() == Layer.OUTPUT)
					.collect(Collectors.toList());

			NodeGene start = startNodes.get(Constants.rand.nextInt(startNodes.size()));
			NodeGene end = null;
			for (NodeGene ng : endNodes) {
				try {
					coreConnections.stream().filter(c -> c.equals2(start, ng)).findFirst().get();
				} catch (NoSuchElementException e) {
					end = ng;
					break;
				}
			}
			ConnectionGene cg = new ConnectionGene(start, end);
			core.connections.put(cg.getInnovationNumber(), cg);
			ConnectionGene cgCopy = cg.copy();
			connections.add(cgCopy);
			nodeInputs.get(end).add(cgCopy);
		} else {
			// Select first connection in the list (it's random due to entrySet's random
			// ordering)
			ConnectionGene cgCopy = unusedConnections.get(0).copy();
			connections.add(cgCopy);
			nodeInputs.get(cgCopy.getOutput()).add(cgCopy);
		}
	}

	public ArrayList<Float> computeOutput(ArrayList<Float> input) {
		nodeValues = new HashMap<>();

		for (int i = 0; i < input.size(); i++) {
			nodeValues.put(core.nodes.get(i), input.get(i));
		}
		
		List<NodeGene> outputNodes = nodes.stream().filter(n -> n.getLayer() == Layer.OUTPUT).collect(Collectors.toList());

		ArrayList<Float> outputList = new ArrayList<>();
		for (NodeGene ng : outputNodes) {
			computeNodeOutput(ng);
			outputList.add(nodeValues.get(ng));
		}
		return outputList;
	}

	public void computeNodeOutput(NodeGene ng) {
		float sum = 0f;
		for (ConnectionGene cg : nodeInputs.get(ng)) {
			if (!nodeValues.containsKey(cg.getInput())) {
				computeNodeOutput(cg.getInput());
			}
			sum+= nodeValues.get(cg.getInput()) * cg.getWeight();
		}
		if (ng.getLayer() == Layer.HIDDEN) {
			sum = core.activationFunctionHidden.applyActivation(sum);
		} else {
			sum = core.activationFunctionOutput.applyActivation(sum);
		}
		nodeValues.put(ng, sum);
	}
	
	public Core getCore() {
		return core;
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

	public void addConnections(ArrayList<ConnectionGene> connections) {
		this.connections.addAll(connections);
	}

	public void addConnection(ConnectionGene connection) {
		connections.add(connection);
	}

	public ArrayList<NodeGene> getNodes() {
		return nodes;
	}
	
	public void addNode(NodeGene node) {
		nodes.add(node);
	}
	
	public void addNodes(Collection<NodeGene> nodes) {
		nodes.addAll(nodes);
	}

	@Override
	public String toString() {
		return connections.toString();
	}
}
