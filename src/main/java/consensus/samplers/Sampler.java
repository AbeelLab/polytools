package consensus.samplers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import lombok.Getter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by regiv on 13/06/2018.
 */
public abstract class Sampler {

    /**
     * Extracts the applicable samples from a context.
     *
     * @param context The context to extract samples from.
     * @return A list of the applicable samples.
     */
    public List<Sample> sample(VariantContext context) {
        if (context.hasAttribute("FILTER_FAIL")) {
            List<Sample> retList = new ArrayList<>();
            retList.add(getRef(context, SampleType.REFERENCE));
            return retList;
        } else {
            return getSample(context);
        }
    }

    /**
     * Extracts the applicable samples from a context.
     * Gets called from sample().
     *
     * @param context The context to extract samples from.
     * @return A list of the applicable samples.
     */
    abstract List<Sample> getSample(VariantContext context);

    /**
     * Get the alternative samples.
     *
     * @param variantContext the context.
     * @param type           The type of variation
     * @return the alternative as byte array.
     */
    List<Sample> getAlt(VariantContext variantContext, SampleType type) {
        List<Sample> alts = new ArrayList<>();
        byte[] ref = variantContext.getReference().getBases();
        ref = getOriginalAllele(ref, variantContext, "RIU");

        if (variantContext.getAlternateAlleles().size() < 1) {
            alts.add(new Sample(ref, type, VariantType.NO_CHANGE, variantContext.getStart(),
                    variantContext.getStart() + variantContext.getReference().length() - 1));
            return alts;
        }

        for (Allele allele : variantContext.getAlternateAlleles()) {
            byte[] alt = getOriginalAllele(allele.getBases(), variantContext, "AIU");
            VariantType variantType = determineVariantType(ref, alt);
            alts.add(new Sample(alt, type, variantType, variantContext.getStart(),
                    variantContext.getStart() + variantContext.getReference().length() - 1));
        }
        return alts;
    }

    /**
     * Get the reference bases.
     *
     * @param variantContext the context.
     * @param type           The type of variation.
     * @return the reference as byte array.
     */
    Sample getRef(VariantContext variantContext, SampleType type) {
        byte[] ref = getOriginalAllele(variantContext.getReference().getBases(), variantContext, "RIU");
        return new Sample(ref, type, VariantType.NO_CHANGE, variantContext.getStart(),
                variantContext.getStart() + variantContext.getReference().length() - 1);
    }

    /**
     * Gets the original allele based on the attirbutes.
     * This returns the origninal bases if it does not equal the byte
     * array of {'N'}.
     *
     * @param bases          The original bases.
     * @param variantContext The current variantcontext.
     * @param attribute      The attribute (RIU for reference AIU for alternate)
     * @return the original alle.
     */
    private byte[] getOriginalAllele(byte[] bases, VariantContext variantContext, String attribute) {
        if (!variantContext.hasAttribute(attribute)) {
            return bases;
        }
        final Object original = variantContext.getAttribute(attribute);
        if (original instanceof String) {
            try {
                return ((String) original).getBytes("UTF-8");
            } catch (UnsupportedEncodingException ignore) {
                //never gonna happen
            }
        }
        return bases;
    }

    /**
     * Get the Iupac encoded version of the reference + alternative.
     * As if they are heterozygous.
     *
     * @param variantContext the context.
     * @return the byte array of the new heterozygous thing.
     */
    List<Sample> getHetero(VariantContext variantContext) {
        List<Sample> samples = getAlt(variantContext, SampleType.HETERO);
        samples.add(getRef(variantContext, SampleType.HETERO));
        return samples;
    }

    /**
     * Determines the variant type of a variation.
     *
     * @param ref The reference to compare the variation to.
     * @param alt The variation.
     * @return The type of variation.
     */
    private VariantType determineVariantType(byte[] ref, byte[] alt) {
        if (alt.length == 0) {
            return VariantType.NO_CHANGE;
        }

        if (ref.length == 0 && alt.length > 0) {
            return VariantType.INSERTION;
        }

        if (ref.length == 1 && alt.length == 1) {
            return ref[0] == alt[0] ? VariantType.NO_CHANGE : VariantType.SNP;
        }

        if (ref.length == 1 && alt.length > 1) {
            return VariantType.INSERTION;
        }

        if (ref.length > 1 && alt.length == 1) {
            return VariantType.SIMPLE_DEL;
        }

        if (ref.length > 1 && ref.length == alt.length) {
            return VariantType.MNP;
        }

        if (ref.length > 1 && alt.length > 1 && ref.length != alt.length) {
            return VariantType.COMPLEX_INDEL;
        }

        return VariantType.UNTYPED;
    }

    /**
     * Sample class.
     */
    @Getter
    @SuppressFBWarnings("EI")
    public static class Sample {
        private byte[] allele;
        private SampleType sampleType;
        private int start;
        private int end;
        private VariantType variantType;

        /**
         * Constructs a new sample.
         *
         * @param allele      The genetic data this sample encodes.
         * @param sampleType  The type of sample.
         * @param variantType The type of variation.
         * @param start       Where this sample starts in the reference.
         * @param end         Where this sample ends in the reference.
         */
        Sample(byte[] allele, SampleType sampleType, VariantType variantType, int start, int end) {
            this.allele = allele;
            this.sampleType = sampleType;
            this.variantType = variantType;
            this.start = start;
            this.end = end;
        }
    }

    /**
     * Enum for where the samples come from.
     */
    public enum SampleType {
        REFERENCE, ALTERNATIVE, HETERO
    }

    /**
     * Enum for what type of variation this is.
     */
    public enum VariantType {
        NO_CHANGE, SNP, MNP, SIMPLE_DEL, INSERTION, MULTIPLE_DEL, COMPLEX_INDEL, UNTYPED
    }
}
