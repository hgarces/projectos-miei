package circuit;

import java.util.Comparator;

public class FitnessComparator implements Comparator<Individual> {

	@Override
	public int compare(Individual i1, Individual i2) {
		
		if (i1.fitness() > i2.fitness()) {
			return 1;
		}
		else if (i1.fitness() < i2.fitness()) {
			return -1;
		}
		else {
			return 0;
		}
	}

}
