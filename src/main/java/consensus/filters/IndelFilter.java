package consensus.filters;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;

/**
 * This will filter out all variants that alter the reference length.
 */
public class IndelFilter implements VariantContextFilter {

    /**
     * Test if the current variant is part of the filter.
     * @param record The variant context.
     * @return true if it is part of the filter false otherwise.
     */
    @Override
    public boolean test(VariantContext record) {
        return record.getAlternateAlleles().stream().allMatch(a -> a.length() == record.getReference().length());
    }
}
