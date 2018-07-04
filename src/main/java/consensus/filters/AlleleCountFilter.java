package consensus.filters;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import lombok.EqualsAndHashCode;

/**
 * The allele count filter.
 */
@EqualsAndHashCode
public class AlleleCountFilter implements VariantContextFilter {

    private final int max;
    private final int min;
    private boolean matchAll;

    /**
     * Creates a new allelecount filter.
     *
     * @param min The minimum allele count.
     * @param max The maximum allele count.
     * @param matchAll Indicates whether all or 1 allele needs to fulfill the filter requirements
     */
    public AlleleCountFilter(int min, int max, boolean matchAll) {
        this.min = min;
        this.max = max;
        this.matchAll = matchAll;
    }

    /**
     * Tests if the record is included in the current filter.
     *
     * @param record The variant context.
     * @return true if it is included in the current filter.
     */
    @Override
    public boolean test(VariantContext record) {
        final Object ac = record.getAttribute("AC");
        if (ac instanceof String) {
            String[] values = ((String) ac).split(",");
            for (String value : values) {
                int count = Integer.parseInt((String) value);
                if ((count < this.min || count > this.max) == matchAll) {
                    return !matchAll;
                }
            }
            return matchAll;
        }
        return false;
    }
}
