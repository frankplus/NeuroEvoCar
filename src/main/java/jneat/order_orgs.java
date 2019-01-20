package jneat;

public class order_orgs implements java.util.Comparator<Organism> {
	public int compare(Organism o1, Organism o2) {
		if (o1.fitness < o2.fitness)
			return +1;
		if (o1.fitness > o2.fitness)
			return -1;
		return 0;
	}
}