package consensus.filters;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import lombok.EqualsAndHashCode;

/**
 * The allele depth filter.
 */
@EqualsAndHashCode
public class DepthFilter implements VariantContextFilter {

    private final int max;
    private final int min;

    /**
     * Creates a new allelecount filter.
     *
     * @param min The minimum allele count.
     * @param max The maximum allele count.
     */
    public DepthFilter(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Tests if the record is included in the current filter.
     *
     * @param record The variant context.
     * @return true if it is included in the current filter.
     */
    @Override
    public boolean test(VariantContext record) {
        final Object ac = record.getAttribute("DP");
        if (ac instanceof String) {
            int count = Integer.parseInt((String) ac);
            return count >= this.min && count <= this.max;
        }
        return false;
    }
}
