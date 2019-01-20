package jneat;

public class order_inner implements java.util.Comparator<NNode> {
	public int compare(NNode o1, NNode o2) {
		if (o1.inner_level < o2.inner_level)
			return -1;
		if (o1.inner_level > o2.inner_level)
			return +1;
		return 0;
	}
}