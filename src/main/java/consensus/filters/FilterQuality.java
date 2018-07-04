package consensus.filters;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import lombok.EqualsAndHashCode;

/**
 * A filter for the quality of the VariantContext.
 */
@EqualsAndHashCode
public class FilterQuality implements VariantContextFilter {

    private final double min;
    private final double max;

    /**
     * Creates a new Filter.
     * @param min the minimum quality.
     */
    public FilterQuality(double min) {
        this(min, Double.MAX_VALUE);
    }

    /**
     * Creates a new filter.
     * @param min The minimum quality.
     * @param max The maximum quality.
     */
    public FilterQuality(double min, double max) {
        if (min > max) {
            this.min = max;
            this.max = min;
        } else {
            this.min = min;
            this.max = max;
        }
    }

    /**
     * Test the variantcontext.
     * @param variantContext The variantContexts from an vcf file.
     * @return if the variant contexts is in the current filter.
     */
    @Override
    public boolean test(VariantContext variantContext) {
        double qual = variantContext.getPhredScaledQual();
        return qual >= min && qual <= max;
    }
}
