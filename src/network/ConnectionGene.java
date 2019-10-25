package network;


import java.util.concurrent.atomic.AtomicInteger;

import data.Constants;

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

	public ConnectionGene(NodeGene input, NodeGene output) {
		innovationNumber = ai.getAndIncrement();
		this.input = input;
		this.output = output;
		this.active = true;
		mutateWeightRandom();
	}

	private ConnectionGene(NodeGene input, NodeGene output, int innovationNumber, float weight) {
		this.input = input;
		this.output = output;
		this.innovationNumber = innovationNumber;
		this.active = true;
		this.weight = weight;
	}

	public void mutate() {
		// Mutate weight
		if (!mutateWeightUniform()) {
			mutateWeightRandom();
		}
	}
	
	public boolean mutateWeightUniform() {
		if (Constants.rand.nextDouble() < WEIGHT_PROB) {
			float change = UNIFORM_WEIGHT_CHANGE * Constants.rand.nextFloat();
			this.weight += change * (Constants.rand.nextBoolean() ? -1 : 1);
			return true;
		}
		return false;
	}

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
