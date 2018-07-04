package consensus;

import consensus.samplers.Sampler;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class takes a list of samples and creates a position oriented collection of Consensuses by merging the samples.
 */
class VariantMerger {
    private List<Sampler.Sample> samples;


    /**
     * Creates a new VariantMerger.
     *
     * @param samples The samples to be used in merging.
     */
    VariantMerger(List<Sampler.Sample> samples) {
        this.samples = samples;
    }

    /**
     * Merges the given samples into a list consensuses.
     *
     * @param startPosition Smallest start position in this sample list.
     * @param endPosition   Greatest end position in this sample list.
     * @param stats         A statistics tracker
     * @return A list of consensuses.
     */
    List<Consensus> mergeVariants(int startPosition, int endPosition,
                                         ConsensusGenerator.ConsensusStatistics stats) {
        ReferenceConsensus[] consensuses = new ReferenceConsensus[endPosition + 1 - startPosition];

        consensuses[0] = new ReferenceConsensus(startPosition, false);

        for (int i = 1; i < consensuses.length; i++) {
            consensuses[i] = new ReferenceConsensus(i + startPosition, false);
            consensuses[i - 1].setNextConsensus(consensuses[i]);
        }

        for (Sampler.Sample sample : samples) {
            boolean hetero = sample.getSampleType() == Sampler.SampleType.HETERO;
            int index = sample.getStart() - startPosition;

            if (sample.getVariantType() == Sampler.VariantType.SIMPLE_DEL) {
                applyDeletion(consensuses, sample, hetero, index, stats);
            }

            if (sample.getVariantType() == Sampler.VariantType.MNP) {
                applyMNP(consensuses, sample, hetero, index);
            }

            if (sample.getVariantType() == Sampler.VariantType.NO_CHANGE) {
                applyNoChange(consensuses, sample, hetero, index);
            }

            if (sample.getVariantType() == Sampler.VariantType.SNP) {
                applySNP(consensuses, sample, hetero, index);
            }

            if (sample.getVariantType() == Sampler.VariantType.INSERTION) {
                applyInsertion(consensuses, sample, hetero, index, stats);
            }

            if (sample.getVariantType() == Sampler.VariantType.COMPLEX_INDEL) {
                applyComplexIndel(consensuses, sample, hetero, index, stats);
            }
        }

        List<Consensus> consensusList = new ArrayList<>();
        Consensus consensus = consensuses[0];
        while (consensus.getNextConsensus() != null) {
            consensusList.add(consensus);
            consensus = consensus.getNextConsensus();
        }
        consensusList.add(consensus);

        return consensusList;
    }

    private void applySNP(ReferenceConsensus[] consensuses, Sampler.Sample sample, boolean hetero, int index) {
        applyPolymorphism(consensuses, sample, hetero, index);
    }

    private void applyNoChange(ReferenceConsensus[] consensuses, Sampler.Sample sample, boolean hetero, int index) {
        if (sample.getSampleType() == Sampler.SampleType.REFERENCE) {
            for (int i = 0; i < sample.getAllele().length; i++) {
                consensuses[index + i].setRef(sample.getAllele()[i]);
            }
        } else {
            applyPolymorphism(consensuses, sample, hetero, index);
        }
    }

    private void applyMNP(ReferenceConsensus[] consensuses, Sampler.Sample sample, boolean hetero, int index) {
        applyPolymorphism(consensuses, sample, hetero, index);
    }

    private void applyDeletion(ReferenceConsensus[] consensuses, Sampler.Sample sample, boolean hetero, int index,
                               ConsensusGenerator.ConsensusStatistics stats) {
        int i = 0;
        for (; i < sample.getAllele().length; i++) {
            consensuses[index + i].addVariation(sample.getAllele()[i]);
            if (hetero) {
                consensuses[index + i].setHeteroLevel(Math.max(consensuses[index + i].getHeteroLevel(), 1));
            }
        }

        for (; i <= sample.getEnd() - sample.getStart(); i++) {
            if (hetero) {
                consensuses[index + i].setHeteroLevel(consensuses[index + i].getHeteroLevel() + 1);
            }
            consensuses[index + i].setDeleted();
        }
        stats.addDeletion(i - sample.getAllele().length);
    }

    private void applyInsertion(ReferenceConsensus[] consensuses, Sampler.Sample sample, boolean hetero,
                                int index, ConsensusGenerator.ConsensusStatistics stats) {
        int i = 0;
        for (; i < sample.getEnd() + 1 - sample.getStart(); i++) {
            if (hetero) {
                consensuses[index + i].setHeteroLevel(Math.max(consensuses[index + i].getHeteroLevel(), 1));
            }
            consensuses[index + i].addVariation(sample.getAllele()[i]);
        }

        int refIndex = index + i - 1;
        stats.addInsertion(sample.getAllele().length - i);

        Consensus previousConsensus = consensuses[refIndex];
        for (; i < sample.getAllele().length; i++) {
            InsertionConsensus nextConsensus;

            if (previousConsensus.getNextConsensus() == null
                    || previousConsensus.getNextConsensus().getRelativePosition() == 0) {
                nextConsensus = new InsertionConsensus(previousConsensus);
                nextConsensus.setNextConsensus(previousConsensus.getNextConsensus());
                previousConsensus.setNextConsensus(nextConsensus);
            } else {
                nextConsensus = (InsertionConsensus) previousConsensus.getNextConsensus();
            }

            nextConsensus.addVariation(sample.getAllele()[i]);
            if (hetero) {
                nextConsensus.setHeteroLevel(consensuses[refIndex].getHeteroLevel() + 1);
            }

            previousConsensus = nextConsensus;
        }
    }

    private void applyComplexIndel(ReferenceConsensus[] consensuses, Sampler.Sample sample, boolean hetero,
                                   int index, ConsensusGenerator.ConsensusStatistics stats) {
        int refLength = sample.getEnd() + 1 - sample.getStart();
        int sampleLength = sample.getAllele().length;

        if (sampleLength > refLength) {
            applyInsertion(consensuses, sample, hetero, index, stats);
        } else {
            applyDeletion(consensuses, sample, hetero, index, stats);
        }
    }

    private void applyPolymorphism(ReferenceConsensus[] consensuses, Sampler.Sample sample, boolean hetero, int index) {
        for (int i = 0; i <= sample.getEnd() - sample.getStart(); i++) {
            if (hetero) {
                consensuses[index + i].setHeteroLevel(Math.max(consensuses[index + i].getHeteroLevel(), 1));
            }
            consensuses[index + i].addVariation(sample.getAllele()[i]);
        }
    }

    /**
     * Represents a location on the consensus sequence, with the reported variant on that location.
     */
    public abstract class Consensus {
        private List<Byte> consensus;
        @Getter
        @Setter
        private byte ref;

        @Getter
        @Setter
        private int heteroLevel;

        @Getter
        @Setter
        private Consensus nextConsensus = null;

        /**
         * Creates a new consensus.
         */
        Consensus() {
            consensus = new ArrayList<>();
            heteroLevel = 0;
        }

        /**
         * Adds a new variation to this position.
         *
         * @param b The variant to add.
         */
        void addVariation(Byte b) {
            consensus.add(b);
        }

        /**
         * Returns the variations at this position.
         *
         * @return the variations at this position.
         */
        public List<Byte> getConsensus() {
            return consensus;
        }

        /**
         * Returns the position in the original reference genome this base would occupy,
         * or the position of the first preceding base that has a position in the reference genome.
         *
         * @return The position.
         */
        public abstract int getRefPosition();

        /**
         * Returns the position distance to the first preceding base that has a position in the reference genome.
         * Is 0 if this position was in the original genome.
         *
         * @return The relative position.
         */
        abstract int getRelativePosition();

        /**
         * Returns whether or not this base has been deleted in a variant.
         *
         * @return Whether this base has been deleted in a variant.
         */
        public abstract boolean isDeleted();
    }

    /**
     * A base with a reference position.
     */
    private class ReferenceConsensus extends Consensus {
        private int position;
        private boolean deleted;

        ReferenceConsensus(int position, boolean deleted) {
            this.position = position;
            this.deleted = deleted;
        }

        @Override
        public int getRefPosition() {
            return position;
        }

        @Override
        public int getRelativePosition() {
            return 0;
        }

        @Override
        public boolean isDeleted() {
            return deleted;
        }

        /**
         * Marks this base as deleted in at least one variant.
         */
        private void setDeleted() {
            deleted = true;
        }
    }

    /**
     * A base that has no position in the original reference, but is inserted between reference bases.
     */
    private class InsertionConsensus extends Consensus {
        @Getter
        private Consensus previousConsensus;

        @Getter
        @Setter
        private Consensus nextConsensus = null;

        InsertionConsensus(Consensus previousConsensus) {
            super();
            this.previousConsensus = previousConsensus;
        }

        @Override
        public int getRefPosition() {
            return previousConsensus.getRefPosition();
        }

        @Override
        public int getRelativePosition() {
            return previousConsensus.getRelativePosition() + 1;
        }

        @Override
        public boolean isDeleted() {
            return previousConsensus.isDeleted();
        }

        @Override
        public int getHeteroLevel() {
            return Math.max(super.getHeteroLevel(), previousConsensus.getHeteroLevel());
        }
    }
}