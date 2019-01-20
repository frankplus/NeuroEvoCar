package jGraph;

public class order_inner implements java.util.Comparator<Vertex> {
	public int compare(Vertex o1, Vertex o2) {
		if (o1.altitude < o2.altitude)
			return -1;
		if (o1.altitude > o2.altitude)
			return +1;
		return 0;
	}
}