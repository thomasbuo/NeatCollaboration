package data;

import java.util.ArrayList;
import java.util.Collections;

import network.Core;
import network.Genome;
import network.Species;

public enum MutateMethod {

	PRESERVE_OLD {
		@Override
		public void mutate(Core c) {
			ArrayList<Genome> genomes = c.getGenomes();
			int size = genomes.size();
			for (int i = size - size / 2; i < size; i++) {
				genomes.get(i).mutate();
			}
		}
	},
	PRESERVE_TOP {
		@Override
		public void mutate(Core c) {
			for (Species s : c.getSpecies()) {
				Genome max = Collections.max(s.getGenomes());
				s.getGenomes().stream().filter(g -> g != max).forEach(g -> g.mutate());
			}
		}
	};
	
	public abstract void mutate(Core c);
	
}
