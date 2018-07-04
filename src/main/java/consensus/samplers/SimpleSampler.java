package consensus.samplers;

import htsjdk.tribble.TribbleException;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import logger.MultiLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by regiv on 13/06/2018.
 */
public class SimpleSampler extends Sampler {

    @Override
    protected List<Sample> getSample(VariantContext context) {
        try {
            for (Genotype g : context.getGenotypes().iterateInSampleNameOrder()) {
                if (equalAlleles(g.getAlleles())) {
                    if (g.getAllele(0).equals(context.getReference(), true)) {
                        List<Sample> samples = new ArrayList<>();
                        samples.add(getRef(context, SampleType.REFERENCE));
                        return samples;
                    } else {
                        return getAlt(context, SampleType.ALTERNATIVE);
                    }
                }
            }
        } catch (TribbleException e) {
            MultiLogger.get().println("Found error in VCF, assumed call to be hetero! '"
                    + e.getMessage() + "'" + " - Context: " + context.toString());
        }
        return getHetero(context);
    }

    /**
     * Check if a list only contains the same type of alleles.
     * And there is at least 1 allele in the list.
     *
     * @param alleles the list to check.
     * @return true if they are equal.
     */
    private boolean equalAlleles(List<Allele> alleles) {
        Allele last = null;
        for (Allele a : alleles) {
            if (last != null && !last.equals(a)) {
                return false;
            }
            last = a;
        }
        return last != null;
    }
}
