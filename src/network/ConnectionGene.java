package network;


import java.util.concurrent.atomic.AtomicInteger;

import data.Constants;

/**
 * 
 * Represents a directed connection (input to output) between two {@link NodeGene}s with a certain weight. Can be active or inactive.
 * 
 * @author Siemen Geurts, Thomas van den Broek
 *
 */
public class ConnectionGene implements Comparable<ConnectionGene> {
	
	private static final float WEIGHT_PROB = 0.9f;
	private static final float UNIFORM_WEIGHT_CHANGE = 0.1f;
	private static final float LOWER_WEIGHT_BOUND = -1f, UPPER_WEIGHT_BOUND = 1f;

	private int innovationNumber;
	public static AtomicInteger ai = new AtomicInteger();

	private NodeGene input;
	private NodeGene output;

	private boolean active;
	
	private float weight;

	/**
	 * Base constructor method for creating a connection between two nodes
	 * 
	 * @param input The input node (start point)
	 * @param output The output node (end point)
	 */
	public ConnectionGene(NodeGene input, NodeGene output) {
		innovationNumber = ai.getAndIncrement();
		this.input = input;
		this.output = output;
		this.active = true;
		mutateWeightRandom();
	}

	/**
	 * Constructor method used for copying another connection
	 * 
	 * @param input The original connection's input
	 * @param output The original connection's output
	 * @param innovationNumber The innovation number of the original connection
	 * @param weight The weight of the original connection
	 */
	private ConnectionGene(NodeGene input, NodeGene output, int innovationNumber, float weight) {
		this.input = input;
		this.output = output;
		this.innovationNumber = innovationNumber;
		this.active = true;
		this.weight = weight;
	}
	
	/**
	 * Mutate this connection by mutating its weight
	 */
	public void mutate() {
		// Mutate weight
		if (!mutateWeightUniform()) {
			mutateWeightRandom();
		}
	}
	
	/**
	 * Mutate weight based on some increment or decrement
	 * 
	 * @return Whether the connection weight was actually changed
	 */
	public boolean mutateWeightUniform() {
		if (Constants.rand.nextDouble() < WEIGHT_PROB) {
			float change = UNIFORM_WEIGHT_CHANGE * Constants.rand.nextFloat();
			this.weight += change * (Constants.rand.nextBoolean() ? -1 : 1);
			return true;
		}
		return false;
	}

	/**
	 * Mutate weight entirely randomly between a lower and upper bound
	 */
	public void mutateWeightRandom() {
		this.weight = LOWER_WEIGHT_BOUND + (UPPER_WEIGHT_BOUND - LOWER_WEIGHT_BOUND) * Constants.rand.nextFloat();
	}

	public int getInnovationNumber() {
		return innovationNumber;
	}

	public NodeGene getInput() {
		return input;
	}

	public NodeGene getOutput() {
		return output;
	}

	public float getWeight() {
		return weight;
	}
	
	public void setWeight(float weight) {
		this.weight = weight;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public ConnectionGene copy() {
		return new ConnectionGene(input, output, innovationNumber, weight);
	}

	@Override
	public String toString() {
		return "ConnectionGene " + innovationNumber + " [" + input.toString() + " - " + output.toString() + " | "
				+ weight + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ConnectionGene) {
			ConnectionGene cg = (ConnectionGene) o;
			return cg.innovationNumber == innovationNumber;
		}
		return false;
	}
	
	public boolean equals2(NodeGene inputNode, NodeGene outputNode) {
		return inputNode.equals(input) && outputNode.equals(output);
	}

	@Override
	public int compareTo(ConnectionGene cg) {
		if (innovationNumber < cg.getInnovationNumber())
			return -1;
		if (innovationNumber > cg.getInnovationNumber())
			return 1;
		return 0;
	}
}
