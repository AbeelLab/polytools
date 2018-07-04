package consensus.filters;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import lombok.EqualsAndHashCode;

/**
 * This filter receives another filter as attribute.
 * This filter will succeed IFF the passed filter does not succeed.
 */
@EqualsAndHashCode
public class FilterInverse implements VariantContextFilter {

    private final VariantContextFilter filter;

    /**
     * Creates a new filter.
     *
     * @param filter An implementation of the filter.
     */
    public FilterInverse(VariantContextFilter filter) {
        this.filter = filter;
    }

    /**
     * Tests if the variantContext is included in the current filter.
     *
     * @param variantContext The variantContexts from an vcf file.
     * @return <code>true</code> if this filter succeeds <code>false</code> otherwise.
     */
    @Override
    public boolean test(VariantContext variantContext) {
        return !this.filter.test(variantContext);
    }
}
