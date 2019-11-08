package network;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a single node used by each {@link Genome} within a {@link Core}
 * 
 * @author Siemen Geurts, Thomas van den Broek
 *
 */
public class NodeGene {

	public enum Layer {
		INPUT, HIDDEN, OUTPUT
	}
	
	private int innovationNumber;
	public static AtomicInteger ai = new AtomicInteger();
	private Layer layer;
	
	public NodeGene(Layer layer) {
		innovationNumber = ai.getAndIncrement();
		this.layer = layer;
	}
	
	public int getInnovationNumber() {
		return innovationNumber;
	}
	
	public Layer getLayer() {
		return layer;
	}
	
	@Override
	public String toString() { 
		return "NodeGene " + innovationNumber + " " + layer;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof NodeGene) {
			NodeGene ng = (NodeGene) o;
			return ng.innovationNumber == innovationNumber;
		}
		return false;
	}
	
}
