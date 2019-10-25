package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import data.ActivationFunction;
import data.FitnessHeuristic;
import network.NodeGene.Layer;

public class Core {

	public HashMap<Integer, NodeGene> nodes = new HashMap<>();
	public HashMap<Integer, ConnectionGene> connections = new HashMap<>();
	public ArrayList<Genome> genomes = new ArrayList<>();
	
	public ActivationFunction activationFunctionHidden;
	public ActivationFunction activationFunctionOutput;
	
	public FitnessHeuristic heuristic;
	
	private int currentGeneration;
	
	public void initialize(int numInputs, int numOutputs, 
						   ActivationFunction afh, ActivationFunction afo,
						   FitnessHeuristic heuristic, int populationSize,
						   int maxGeneration) {
		
		
		for (int i = 0; i < numInputs; i++) {
			NodeGene n = new NodeGene(Layer.INPUT);
			nodes.put(n.getInnovationNumber(), n);
		}
		for (int i = 0; i < numOutputs; i++) {
			NodeGene n = new NodeGene(Layer.OUTPUT);
			nodes.put(n.getInnovationNumber(), n);
		}
		
		for (int i = 0; i < populationSize; i++) {
			Genome g = new Genome(this);
			genomes.add(g);
			g.addNodes(nodes.values().stream().collect(Collectors.toList()));
		}
		
		currentGeneration = 0;
		
		do {
			for (Genome g : genomes) {
				float fitness = heuristic.computeFitness(g);
				g.setFitness(fitness);
			}
			
			
		} while (currentGeneration < maxGeneration && !heuristic.checkStoppingCriteria(this));
	}
	 
}
