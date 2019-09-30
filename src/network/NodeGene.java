package network;

import java.util.concurrent.atomic.AtomicInteger;

public class NodeGene {

	private int innovationNumber;
	public static AtomicInteger ai = new AtomicInteger();
	
	public NodeGene() {
		innovationNumber = ai.getAndIncrement();
	}
	
	public int getInnovationNumber() {
		return innovationNumber;
	}
	
	@Override
	public String toString() { 
		return "NodeGene " + innovationNumber;
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
