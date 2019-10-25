package network;

import java.util.ArrayList;

import data.Constants;

public class Species {

	private ArrayList<Genome> genomes;
	private Genome leaderGenome;
	
	public Species(Genome leaderGenome) {
		genomes = new ArrayList<>();
		genomes.add(leaderGenome);
		this.leaderGenome = leaderGenome;
	}
	
	public void addGenome(Genome genome) {
		genomes.add(genome);
	}
	
	public boolean kill(Genome g) {
		//TODO: kill species in global 
		if (g.equals(leaderGenome)) {
			setRandomLeader();
		}
		return genomes.remove(g);
	}
	
	public ArrayList<Genome> killRandom(float fraction) {
		int countToRemove = (int)(genomes.size() * (1 - fraction));
		int size = genomes.size();
		ArrayList<Genome> removed = new ArrayList<>();
		while (genomes.size() > size - countToRemove) {
			removed.add(genomes.remove(Constants.rand.nextInt(genomes.size())));
		}
		return removed;
	}
	
	public Genome setRandomLeader() {
		return leaderGenome = genomes.get(Constants.rand.nextInt(genomes.size()));
	}
	
	public ArrayList<Genome> getGenomes() {
		return genomes;
	}
	
	public Genome getLeaderGenome() {
		return leaderGenome;
	}
	
}
