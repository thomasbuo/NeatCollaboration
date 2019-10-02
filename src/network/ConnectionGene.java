package network;

import java.util.concurrent.atomic.AtomicInteger;

import data.Constants;

public class ConnectionGene implements Comparable<ConnectionGene> {

	private static final double WEIGHT_PROB = 0.9;
	private static final float LOWER_WEIGHT_BOUND = -1f, UPPER_WEIGHT_BOUND = 1f;

	private int innovationNumber;
	public static AtomicInteger ai = new AtomicInteger();

	private NodeGene input;
	private NodeGene output;

	private float weight;

	public ConnectionGene(NodeGene input, NodeGene output) {
		innovationNumber = ai.getAndIncrement();
		this.weight = LOWER_WEIGHT_BOUND + (UPPER_WEIGHT_BOUND - LOWER_WEIGHT_BOUND) * Constants.rand.nextFloat();
		this.input = input;
		this.output = output;
	}

	private ConnectionGene(NodeGene input, NodeGene output, int innovationNumber, float weight) {
		this.input = input;
		this.output = output;
		this.innovationNumber = innovationNumber;
		this.weight = weight;
	}

	public void mutate() {
		if (!mutateWeightUniform()) {
			mutateWeightRandom();
		}
	}

	public boolean mutateWeightUniform() {
		if (Constants.rand.nextDouble() < WEIGHT_PROB) {
			//TODO: mutate weight uniform
			return true;
		}
		return false;
	}

	public void mutateWeightRandom() {
		// todo
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

	@Override
	public int compareTo(ConnectionGene cg) {
		if (innovationNumber < cg.getInnovationNumber())
			return -1;
		if (innovationNumber > cg.getInnovationNumber())
			return 1;
		return 0;
	}
}
