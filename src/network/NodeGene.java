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
	
	//_________________________________________________________________//
	// The following fields are for finding loops in a Genome
	
	private Integer index, lowlink;
	private boolean onStack;
	
	//_________________________________________________________________//
	
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
	
	//_________________________________________________________________//
	// The following methods are for detecting loops in a Genome
	
	public Integer getIndex() {
		return index;
	}
	
	public void setIndex(Integer index) {
		this.index = index;
	}
	
	public Integer getLowlink() {
		return lowlink;
	}
	
	public void setLowlink(Integer lowlink) {
		this.lowlink = lowlink;
	}
	
	public boolean isOnStack() {
		return onStack;
	}
	
	public void setOnStack(boolean onStack) {
		this.onStack = onStack;
	}
	
	public void reset() {
		this.index = null;
		this.lowlink = null;
		this.onStack = false;
	}
	
	//_________________________________________________________________//
	
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
