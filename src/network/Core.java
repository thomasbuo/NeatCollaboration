package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;

import data.ActivationFunction;
import data.BreedMethod;
import data.Constants;
import data.KillMethod;
import data.MutateMethod;
import data.NEATHeuristic;
import network.NodeGene.Layer;

public class Core {

	protected HashMap<Integer, NodeGene> nodes = new HashMap<>();
	protected HashMap<Integer, ConnectionGene> connections = new HashMap<>();
	protected ArrayList<Genome> genomes = new ArrayList<>();
	protected ArrayList<Species> species = new ArrayList<>();
	
	protected ActivationFunction activationFunctionHidden;
	protected ActivationFunction activationFunctionOutput;
	
	protected NEATHeuristic heuristic;
	
	private int populationSize;
	
	private int currentGeneration;
	
	public void initialize(int numInputs, int numOutputs, 
						   ActivationFunction afh, ActivationFunction afo,
						   KillMethod killMethod, BreedMethod breedMethod, MutateMethod mutateMethod,
						   NEATHeuristic heuristic, int populationSize,
						   int maxGeneration) {
		Constants.initialize();
		this.activationFunctionHidden = afh;
		this.activationFunctionOutput = afo;
		this.populationSize = populationSize;
		
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
			System.out.println("Calculating Fitness");
			for (Genome g : genomes) {
				System.out.println(g);
				float fitness = heuristic.computeFitness(g);
				g.setFitness(fitness);
			}
			System.out.println("Fitness Calculated");

			System.out.println("Calculating Species");
			new Speciation(this);
			System.out.println("Species Calculated");
			
			System.out.println("Kill");
			killMethod.kill(this);
			System.out.println("Finished Killing");
			Species s = null;
			for (Iterator<Species> iterator = species.iterator(); iterator.hasNext();) {
				s = iterator.next();
				if (s.getGenomes().isEmpty()) {
					iterator.remove();
				}
			}
			System.out.println("Breed");
			breedMethod.breed(this);
			System.out.println("Mutate");
			mutateMethod.mutate(this);
			System.out.println("Finished Mutating");
			
		} while (currentGeneration < maxGeneration && !heuristic.checkStoppingCriteria(this));
	}
	
	public ArrayList<Genome> getGenomes() {
		return genomes;
	}
	
	public ArrayList<Species> getSpecies() {
		return species;
	}
	
	public int getPopulationSize() {
		return populationSize;
	}
	 
}
