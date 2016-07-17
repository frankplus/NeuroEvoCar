package jneat;

public class order_species implements java.util.Comparator<Species> {
	/**
	 * order_species constructor comment.
	 */
	public order_species() {
		// super();
	}

	public int compare(Species o1, Species o2) {
		Organism _ox = (Organism) o1.organisms.firstElement();
		Organism _oy = (Organism) o2.organisms.firstElement();

		if (_ox.orig_fitness < _oy.orig_fitness)
			return +1;
		if (_ox.orig_fitness > _oy.orig_fitness)
			return -1;
		return 0;
	}
}