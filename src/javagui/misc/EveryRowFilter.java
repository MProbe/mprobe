package misc;

import javax.swing.RowFilter;

public class EveryRowFilter<M,I> extends RowFilter<M, I> {

	@Override
	public boolean include(
			javax.swing.RowFilter.Entry<? extends M, ? extends I> entry) {
		return true;
	}

}
