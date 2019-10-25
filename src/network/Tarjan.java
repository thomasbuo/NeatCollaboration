package network;

import java.util.ArrayList;
import java.util.Stack;
import java.util.stream.Collectors;

public class Tarjan {

	private int index;
	private Stack<NodeGene> stack;
	private Genome g;
	private ArrayList<ArrayList<NodeGene>> SSCs;
	
	public boolean containsLoop(Genome g) {
		SSCs = new ArrayList<>();
		this.g = g;
		index = 0;
		stack = new Stack<>();
		g.getNodes().forEach(n -> n.reset());
		for (NodeGene v : g.getNodes()) {
			if (v.getIndex() == null) {
				strongConnect(v);
			}
		}
		return SSCs.size() < g.getNodes().size();
	}
	
	private void strongConnect(NodeGene v) {
		v.setIndex(index);
		v.setLowlink(index);
		index++;
		stack.push(v);
		v.setOnStack(true);
		
		for (ConnectionGene cg : g.getConnections().stream().filter(c -> c.getInput().equals(v)).collect(Collectors.toList())) {
			NodeGene w = cg.getOutput();
			if (w.getIndex() == null) {
				strongConnect(cg.getOutput());
				v.setLowlink(Math.min(v.getLowlink(), w.getLowlink()));
			} else if (w.isOnStack()) {
				v.setLowlink(Math.min(v.getLowlink(), w.getLowlink()));
			}
		}
		
		if (v.getLowlink() == v.getIndex()) {
			ArrayList<NodeGene> SSC = new ArrayList<>();
			NodeGene w = null;
			do {
				w = stack.pop();
				w.setOnStack(false);
				SSC.add(w);
			} while(!w.equals(v));
			SSCs.add(SSC);
		}
	}
	
}
