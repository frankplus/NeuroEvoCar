package jGraph;

public class order_code implements java.util.Comparator<code> {
	public order_code() {
		super();
	}

	public int compare(code o1, code o2) {
		if (o1.tipo > o2.tipo)
			return -1;
		if (o1.tipo < o2.tipo)
			return +1;
		return 0;

	}
}