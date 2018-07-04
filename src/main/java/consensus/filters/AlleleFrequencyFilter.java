package consensus.filters;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import lombok.EqualsAndHashCode;

/**
 * The allele frequency filter.
 */
@EqualsAndHashCode
public class AlleleFrequencyFilter implements VariantContextFilter {
    private final double max;
    private final double min;
    private final boolean matchAll;

    /**
     * Creates a new allele frequency filter.
     *
     * @param min The minimum allele count.
     * @param max The maximum allele count.
     * @param matchAll Indicates whether all or 1 allele needs to fulfill the filter requirements
     */
    public AlleleFrequencyFilter(double min, double max, boolean matchAll) {
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
        final Object af = record.getAttribute("AF");
        if (af instanceof String) {
            String[] values = ((String) af).split(",");
            for (String value : values) {
                double count = Double.parseDouble((String) value);
                if ((count < this.min || count > this.max) == matchAll) {
                    return !matchAll;
                }
            }
            return matchAll;
        }
        return false;
    }
}
