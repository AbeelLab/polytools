package consensus.filters;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import lombok.EqualsAndHashCode;

/**
 * The allele count filter.
 */
@EqualsAndHashCode
public class ChromosomeFilter implements VariantContextFilter {

    private final String chromosome;

    /**
     * Creates a new chromosome filter.
     *
     * @param chromosome The #CHROM field
     */
    public ChromosomeFilter(String chromosome) {
        this.chromosome = chromosome;
    }

    /**
     * Tests if the record is included in the current filter.
     *
     * @param record The variant context.
     * @return true if it is included in the current filter.
     */
    @Override
    public boolean test(VariantContext record) {
        final Object name = record.getContig();
        return chromosome.equals(name);
    }
}
