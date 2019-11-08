package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import data.Constants;

public class Speciation {

	public static final float range = 15f;
	private Core core;
	HashMap<Genome, Float> locations;
	HashMap<Genome, Float> closestCenterDistances;
	
	public Speciation(Core core) {
		this.core = core;
		locations = new HashMap<>();
		closestCenterDistances = new HashMap<>();
		localize();
		int k = 1;
		while (!kMeans(kMeansPP(k))) {		
			k++;
		}
	}
	
	private void localize() {
		ArrayList<Genome> genomes = core.getGenomes();
		Genome g = genomes.get(0);
		Genome maxG = genomes.get(0);
		float max = 0;
		for (Genome g2 : genomes) {
			if (g != g2) {
				float dist = g.computeDistance(g2);
				if (dist > max) {
					maxG = g2;
					max = dist;
				}
			}
		}
		locations.put(maxG, 0f);
		for (Genome g2 : genomes) {
			if (maxG != g2) {
				float dist = g.computeDistance(g2);
				locations.put(g2, dist);
			}
		}
	}
	
	private float distance(Genome g1, Genome g2) {
		return (float)Math.abs(locations.get(g1) - locations.get(g2));
	}
	
	private ArrayList<Genome> kMeansPP(int k) {
		ArrayList<Genome> centers = new ArrayList<>();
		Genome randCenter = core.getGenomes().get(Constants.rand.nextInt(core.getGenomes().size()));
		Genome closestCenter = randCenter;
		for (int currK = 0; currK < k; currK++) {
			float rand = Constants.rand.nextFloat();
			float probSum = 0;
			float cumSum = 0;
			for (Genome g : core.getGenomes()) {
				float minDist = Float.MAX_VALUE;
				for (Genome center : centers) {
					float dist = distance(g, center);
					if (dist < minDist) {
						closestCenter = center;
						minDist = dist;
					}
				}
				closestCenterDistances.put(g, minDist);
				float dist = distance(g, closestCenter);
				cumSum += dist * dist;
			}
			for (Genome g : core.getGenomes()) {
				float dist = closestCenterDistances.get(g);
				probSum += (dist * dist) / cumSum;
				if (probSum >= rand) {
					centers.add(g);
					break;
				}
			}
		}
		return centers;
	}
	
	private boolean kMeans(ArrayList<Genome> centers) {
		ArrayList<Species> species = new ArrayList<>();
		for (Genome c : centers) {
			species.add(new Species(c));
		}
		for(Genome g : core.getGenomes().stream().filter(g2 -> !centers.contains(g2)).collect(Collectors.toList())) {
			float closestDist = Float.MAX_VALUE;
			Genome closestCenter = centers.get(0);
			for(Genome center : centers) {
				float dist = distance(closestCenter, g);
				if(dist < closestDist) {
					closestCenter = center;
					closestDist = dist;
				}
			}
			if(closestDist > range) {
				return false;
			}
			for (Species s : species) {
				if (s.getLeaderGenome() == closestCenter) {
					s.addGenome(g);
					break;
				}
			}
		}
		core.species = species;
		return true;
	}
	
}
