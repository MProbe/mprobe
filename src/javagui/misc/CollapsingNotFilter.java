package misc;

import javax.swing.RowFilter;

public class CollapsingNotFilter<M,I> extends RowFilter<M,I> {
    private RowFilter<M,I> filter;

    public CollapsingNotFilter(RowFilter<M,I> filter) {
        if (filter == null) {
            throw new IllegalArgumentException(
                "filter must be non-null");
        }
        
        // Collapses !!!filter into !filter
        if (filter instanceof CollapsingNotFilter)
        {
        	CollapsingNotFilter<M,I> cnfilter1 = ((CollapsingNotFilter<M,I>)filter);
        	
            if (cnfilter1.filter instanceof CollapsingNotFilter)
            {
            	CollapsingNotFilter<M,I> cnfilter2 = ((CollapsingNotFilter<M,I>)cnfilter1.filter);
            	filter = cnfilter2.filter;
            }
        }
        this.filter = filter;
	}

	public boolean include(Entry<? extends M, ? extends I> value) {
    	return !filter.include(value);
    }
}