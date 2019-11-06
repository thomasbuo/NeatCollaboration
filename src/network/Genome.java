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

/**
 * A single network represented by a set of {@link ConnectionGene}s used for breeding and mutating
 * 
 * @author Siemen Geurts, Thomas van den Broek
 *
 */
public class Genome implements Comparable<Genome> {

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

	/**
	 * Constructor method used for breeding a genome
	 * 
	 * @param core The core from which all variables and information are gained
	 * @param connections The {@link ConnectionGene}s present in this bred genome
	 * @param nodes The {@link NodeGene}s used in this bred genome
	 * @param nodeInputs Map for each {@link NodeGene} which {@link ConnectionGene}s have it as output
	 */
	public Genome(Core core, ArrayList<ConnectionGene> connections, ArrayList<NodeGene> nodes,
			HashMap<NodeGene, ArrayList<ConnectionGene>> nodeInputs) {
		this.core = core;
		this.connections = connections;
		this.nodes = nodes;
		this.nodeInputs = nodeInputs;
	}

	/**
	 * General constructor for creating a genome
	 * 
	 * @param core The core from which all variables and information are gained
	 */
	public Genome(Core core) {
		this.core = core;
		this.connections = new ArrayList<>();
		this.nodes = new ArrayList<>();
		this.nodeInputs = new HashMap<>();
	}

	/**
	 * Breed this genome with another to create a child genome of matching, disjoint and excess {@link ConnectionGene}s
	 * 
	 * @param p The other genome parent
	 * @return The child genome that was bred from this and the given parent
	 */
	public Genome breed(Genome p) {
		//Check whether one genome has a greater fitness, and call breed accordingly
		if (p.fitness > fitness) {
			return breed(p, this, false);
		}
		if (p.fitness < fitness) {
			return breed(this, p, false);
		}
		return breed(this, p, true);
	}

	/**
	 * Breed two genomes with each other to create a child genome of matching, disjoint and excess {@link ConnectionGene}s
	 * 
	 * @param betterParent The parent genome with the higher fitness (if applicable)
	 * @param worseParent The parent genome with the lower fitness (if applicable)
	 * @param randomize Will randomize disjoint and excess {@link ConnectionGene}s if applicable. True if no parent is fitter than the other, else false.
	 * @return The child genome that was bred from the given parents
	 */
	private Genome breed(Genome betterParent, Genome worseParent, boolean randomize) {
		ArrayList<ConnectionGene> betterParentConnections = new ArrayList<>();
		ArrayList<ConnectionGene> worseParentConnections = new ArrayList<>();

		for (ConnectionGene cg : betterParent.getConnections()) {
			betterParentConnections.add(cg.copy());
		}
		for (ConnectionGene cg : worseParent.getConnections()) {
			worseParentConnections.add(cg.copy());
		}

		//Store for both better parent and worse parent matching genes, they are both required for randomized weight selection
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

		//Randomize the weights of the matching ConnectionGenes and generate the child's connections and nodeInputs.
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

		//All ConnectionGenes - matchings = disjoint and excess ConnectionGenes
		betterParentConnections.removeAll(betterParentMatchings.keySet().stream().map(i -> betterParentMatchings.get(i))
				.collect(Collectors.toList()));
		worseParentConnections.removeAll(worseParentMatchings.keySet().stream().map(i -> betterParentMatchings.get(i))
				.collect(Collectors.toList()));

		if (randomize) {
			//If randomized, select for each disjoint and excess ConnectionGene whether to add it to the child or not
			for (ConnectionGene cg : betterParentConnections) {
				if (Constants.rand.nextBoolean())
					childConnections.add(cg);
			}
			for (ConnectionGene cg : worseParentConnections) {
				if (Constants.rand.nextBoolean())
					childConnections.add(cg);
			}
		} else {
			//If not, add all better parent excess and disjoint ConnectionGenes to the child
			childConnections.addAll(betterParentConnections);
		}

		ArrayList<NodeGene> nodes = new ArrayList<>();
		for (NodeGene ng : nodeInputs.keySet())
			nodes.add(ng);
		
		//Generate a child genome and return it
		return new Genome(core, childConnections, nodes, nodeInputs);
	}

	/**
	 * Calculates the distance between this and a given genome, used for speciation
	 * 
	 * @param genome The genome to be compared with
	 * @return The distance between this and the given genome
	 */
	public float computeDistance(Genome genome) {

		ArrayList<ConnectionGene> connectionsGenome1 = getConnections();
		ArrayList<ConnectionGene> connectionsGenome2 = genome.getConnections();

		int maxInnovation = 0;
		Genome maxGenome = null;
		Genome minGenome = null;

		//Find the genome with the newest ConnectionGene, store it in maxGenome
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

		//Store for both the genome with the newest and the other genome the ConnectionGenes, stored with innovation number
		HashMap<Integer, ConnectionGene> maxGenomeMatchings = new HashMap<>();
		HashMap<Integer, ConnectionGene> minGenomeMatchings = new HashMap<>();
		int matchingIndex = 0;
		if (maxGenome.getConnections().isEmpty()) {
			
		}
		for (ConnectionGene maxgc : maxGenome.getConnections()) {
			if ((matchingIndex = minGenome.getConnections().indexOf(maxgc)) >= 0) {
				maxGenomeMatchings.put(maxgc.getInnovationNumber(), maxgc);
				ConnectionGene mingc = minGenome.getConnections().get(matchingIndex);
				minGenomeMatchings.put(mingc.getInnovationNumber(), mingc);
			}
		}

		//Find all non-matching ConnectionGenes
		List<ConnectionGene> nonMatchingsConnections1 = minGenome.getConnections().stream()
				.collect(Collectors.toList());
		nonMatchingsConnections1.removeAll(maxGenomeMatchings.values());

		Collections.sort(maxGenome.getConnections());

		//Determine the excessCount
		int excessCount = 0;
		for (int i = maxGenome.getConnections().size() - 1; i > 0; i--) {
			int min = maxGenome.getConnections().get(i - 1).getInnovationNumber();
			int max = maxGenome.getConnections().get(i).getInnovationNumber();
			if (minGenome.getConnections().stream()
					.anyMatch(c -> c.getInnovationNumber() > min && c.getInnovationNumber() < max)) {
				excessCount = maxGenome.getConnections().size() - 1 - i;
			}
		}

		//Determine the disjointCount
		int disjointCount = (minGenome.getConnections().size() - maxGenomeMatchings.size())
				+ (maxGenome.getConnections().size() - maxGenomeMatchings.size() - excessCount);

		//Find the weight average of the genome with the newest ConnectionGene
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
	
	/**
	 * Mutates this genome by calling {@link #mutateNodeGene}, {@link #mutateConnectionGene} and mutating each
	 * {@link ConnectionGene}'s weight.
	 */
	public void mutate() {
		mutateNodeGene();
		for (ConnectionGene cg : connections) {
			cg.mutate();
		}
		mutateConnectionGene();
	}

	/**
	 * Mutates this genome in the sense that a {@link NodeGene} should be added, and to the {@link Core} if applicable
	 */
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

	/**
	 * Mutates this genome in the sense that a {@link ConnectionGene} is added to the network. Random
	 * nodes are also selected with which to connect.
	 */
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
			ConnectionGene cgCopy = cg.copy();
			connections.add(cgCopy);
			
			// Check new network for loops. If it contains loops, skip creation of connection
			if (new Tarjan(this).containsLoop()) {
				connections.remove(cgCopy);
				return;
			}
			core.connections.put(cg.getInnovationNumber(), cg);
			nodeInputs.get(end).add(cgCopy);
		} else {
			// Select first connection in the list (it's random due to entrySet's random
			// ordering)
			ConnectionGene cgCopy = unusedConnections.get(0).copy();
			connections.add(cgCopy);
			
			// Check new network for loops. If it contains loops, skip creation of connection
			if (new Tarjan(this).containsLoop()) {
				connections.remove(cgCopy);
				return;
			}
			nodeInputs.get(cgCopy.getOutput()).add(cgCopy);
		}
	}

	/**
	 * Setup method for finding this genome's output given an input.
	 * 
	 * @param input The set of values to be used in the input layer
	 * @return The value contained in the {@link NodeGene}s in the output layer
	 */
	public ArrayList<Float> computeOutput(ArrayList<Float> input) {
		nodeValues = new HashMap<>();

		//Set the values of the input nodes to the given input values
		for (int i = 0; i < input.size(); i++) {
			nodeValues.put(core.nodes.get(i), input.get(i));
		}
		//For each output node, find the output and return it
		List<NodeGene> outputNodes = nodes.stream().filter(n -> n.getLayer() == Layer.OUTPUT).collect(Collectors.toList());
		ArrayList<Float> outputList = new ArrayList<>();
		for (NodeGene ng : outputNodes) {
			computeNodeOutput(ng);
			outputList.add(nodeValues.get(ng));
		}
		return outputList;
	}

	/**
	 * Recursive method used for setting a given {@link NodeGene}'s output and storing it within the genome for later use
	 * 
	 * @param ng The node to be evaluated
	 */
	private void computeNodeOutput(NodeGene ng) {
		float sum = 0f;
		//Loop through all ConnectionGenes which have the current node as output
		if (nodeInputs.get(ng) == null) {
			nodeValues.put(ng, 0f);
			return;
		}
		for (ConnectionGene cg : nodeInputs.get(ng)) {
			//Check if they have already been given a value and if so, use the current ConnectionGene's weight to add to the sum.
			//If not, recursive call to find that node's value.
			if (!nodeValues.containsKey(cg.getInput())) {
				computeNodeOutput(cg.getInput());
			}
			sum+= nodeValues.get(cg.getInput()) * cg.getWeight();
		}
		//Apply activation function to the node depending on whether it is within the hidden layer or output layer
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
		this.nodes.addAll(nodes);
	}

	@Override
	public String toString() {
		return connections.toString();
	}

	@Override
	public int compareTo(Genome g) {
		if (fitness > g.fitness)
			return 1;
		if (fitness < g.fitness)
			return -1;
		return 0;
	}
}
