package consensus.samplers;

import htsjdk.variant.variantcontext.VariantContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to determine which alleles to consider based on the allele frequency.
 */
public class AlleleFrequencySampler extends Sampler {
    private double lowerBound;
    private double upperBound;

    /**
     * Constructor with bound values.
     *
     * @param lower the lower bound, below which the reference is accepted.
     * @param upper the upper bound, above which the alternative is accepted.
     */
    public AlleleFrequencySampler(double lower, double upper) {
        if (upper < lower) {
            throw new IllegalArgumentException("Allele frequency encoder's lower bound is bigger then upper bound");
        }
        this.lowerBound = lower;
        this.upperBound = upper;
    }

    /**
     * Encode the variantContext to a byte array.
     *
     * @param variantContext the variant context.
     * @return the byte array result.
     */
    @Override
    protected List<Sample> getSample(VariantContext variantContext) {
        double af = getAlleleFrequency(variantContext);
        if (af < lowerBound) {
            //it is the reference
            List<Sample> samples = new ArrayList<Sample>();
            samples.add(getRef(variantContext, SampleType.REFERENCE));
            return samples;
        }
        if (af > upperBound) {
            //it is the alternative
            return getAlt(variantContext, SampleType.ALTERNATIVE);
        }
        return getHetero(variantContext);
    }

    /**
     * Get the allele frequency.
     *
     * @param context the context.
     * @return the allele frequency.
     */
    private double getAlleleFrequency(VariantContext context) {
        try {
            Object o = context.getAttribute("AF");
            if (o instanceof String) {
                return Double.parseDouble((String) o);
            }
            if (o instanceof Double) {
                return (Double) o;
            }
        } catch (Exception ignore) {
        }
        return -1;
    }
}
