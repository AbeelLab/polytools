package coverage;

import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.filter.*;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalList;
import htsjdk.samtools.util.SamLocusIterator;
import logger.MultiLogger;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class for generating coverage over BAM files.
 */
public class CoverageGenerator {

    /**
     * Variable used to describe the minimal depth to
     * consider a base as 'covered'.
     */
    @Getter
    @Setter
    private int minCoverage;

    /**
     * List of SAM record filters to use.
     */
    @Getter
    private final ArrayList<SamRecordFilter> filterList;

    /**
     * Constructor with minCoverage default to 0.
     */
    public CoverageGenerator() {
        this(0, 1, true, true, true);
    }

    /**
     * Constructor with boolean and integer values for easy creation of generator.
     * @param minCoverage the minimal coverage.
     * @param mappingQualityFilter if bigger than 0, use a mapping quality filter with that value.
     * @param duplicateReadFilter true to use duplicate read filter.
     * @param failsVendorReadQualityFilter true to use fails vendor read quality filter.
     * @param secondaryOrSupplementaryFilter true to use secondary or supplementary filter.
     */
    public CoverageGenerator(int minCoverage, int mappingQualityFilter, boolean duplicateReadFilter,
                             boolean failsVendorReadQualityFilter, boolean secondaryOrSupplementaryFilter) {
        this.minCoverage = minCoverage;
        filterList = new ArrayList<>();
        if (mappingQualityFilter > 0) {
            filterList.add(new MappingQualityFilter(mappingQualityFilter));
        }
        if (duplicateReadFilter) {
            filterList.add(new DuplicateReadFilter());
        }
        if (failsVendorReadQualityFilter) {
            filterList.add(new FailsVendorReadQualityFilter());
        }
        if (secondaryOrSupplementaryFilter) {
            filterList.add(new SecondaryOrSupplementaryFilter());
        }
    }

    /**
     * Create a SamLocusIterator over a given file, chromosome, and interval.
     *
     * @param file the file to create the iterator over.
     * @param chr  the chromosome to consider.
     * @param from the start of the interval.
     * @param to   the end of the interval.
     * @return a iterator over that interval in the given chromosome.
     */
    private SamLocusIterator samLocusIterator(File file, String chr, int from, int to) {
        SamReader samReader = SamReaderFactory.makeDefault()
                .validationStringency(ValidationStringency.LENIENT)
                .open(file);

        // Loci to sample (could be a single position):
        IntervalList il = new IntervalList(samReader.getFileHeader());
        il.add(new Interval(chr, from, to));

        // Start iterating through loci: The last parameter was true, but changed to hasIndex
        SamLocusIterator iterator = new SamLocusIterator(samReader, il, samReader.hasIndex());
        iterator.setSamFilters(filterList); // Optional list of List<SamRecordFilter>
        return iterator;
    }

    /**
     * Get the length of a sequence in a BAM/SAM file.
     * @param file the file to get the length of.
     * @param chr the chromosome to get the length of.
     * @return the length, as integer amount of bases.
     * @throws IOException if close reader failed.
     */
    private int getSequenceLength(File file, String chr) throws IOException {
        SamReader samReader = SamReaderFactory.makeDefault()
                .validationStringency(ValidationStringency.LENIENT)
                .open(file);
        int len = samReader.getFileHeader().getSequence(chr).getSequenceLength();
        samReader.close();
        return len;
    }

    /**
     * Generate basic coverage over a bam file, chromosome and interval.
     *
     * @param bamFile the file to create the iterator over.
     * @param chr     the chromosome to consider.
     * @param from    the start of the interval.
     * @param to      the end of the interval.
     * @return basic coverage, giving the total depth and breath coverage.
     * @throws IOException if reader fails.
     */
    public Coverage bamFileBasicCoverage(String bamFile, String chr, int from, int to) throws IOException {
        File file = new File(bamFile);
        int seqLen = getSequenceLength(file, chr);
        if (to > seqLen) {
            to = seqLen;
        }

        long totalDepth = 0;
        int totalCovered = 0;

        SamLocusIterator samLocusIterator = samLocusIterator(file, chr, from, to);
        for (SamLocusIterator.LocusInfo locusInfo : samLocusIterator) {
            int depth = locusInfo.getRecordAndPositions().size();
            totalDepth += depth;
            if (depth > minCoverage) {
                totalCovered++;
            }
        }

        samLocusIterator.close();
        return new Coverage(chr, from, to, totalDepth, totalCovered, minCoverage);
    }

    /**
     * Generate array coverage over a bam file, chromosome and interval.
     *
     * @param bamFile the file to create the iterator over.
     * @param chr     the chromosome to consider.
     * @param from    the start of the interval.
     * @param to      the end of the interval.
     * @return array coverage, giving the total depth and breath coverage,
     * as well as an array for coverage over each base
     * @throws IOException if reader fails.
     */
    public CoverageArray bamFileArrayCoverage(String bamFile, String chr, int from, int to) throws IOException {
        File file = new File(bamFile);
        int seqLen = getSequenceLength(file, chr);
        if (to > seqLen) {
            to = seqLen;
        }

        long totalDepth = 0;
        int totalCovered = 0;
        int len = to > from ? (to - from) : (from - to);
        // + 1 because both the start and end are inclusive.
        int[] arrayCovered = new int[len + 1];

        SamLocusIterator samLocusIterator = samLocusIterator(file, chr, from, to);
        for (SamLocusIterator.LocusInfo locusInfo : samLocusIterator) {
            int depth = locusInfo.getRecordAndPositions().size();
            int pos = locusInfo.getPosition();
            totalDepth += depth;
            if (depth > minCoverage) {
                totalCovered++;
            }
            arrayCovered[pos - from] = depth;
        }

        samLocusIterator.close();
        return new CoverageArray(chr, from, to, totalDepth, totalCovered, minCoverage, arrayCovered);
    }

    /**
     * Generate basic coverage over a bam file, chromosome and interval on multiple threads.
     *
     * @param threadNum  the amount of threads to run on.
     * @param bamFile the file to create the iterator over.
     * @param chr     the chromosome to consider.
     * @param from    the start of the interval.
     * @param to      the end of the interval.
     * @return basic coverage, giving the total depth and breath coverage.
     * @throws IOException if reader fails.
     */
    public Coverage bamFileBasicCoverageThreads(
            int threadNum, String bamFile, String chr,
            int from, int to) throws IOException {
        if (threadNum <= 1) {
            return bamFileBasicCoverage(bamFile, chr, from, to);
        }
        File file = new File(bamFile);
        int seqLen = getSequenceLength(file, chr);
        if (to > seqLen) {
            to = seqLen;
        }

        final Coverage result = new Coverage(chr, from, to, 0, 0, minCoverage);

        int amountOfBases = 1 + (to > from ? (to - from) : (from - to));
        Thread[] threads = new Thread[threadNum];
        int[] parts = splitIntoParts(amountOfBases, threadNum);

        int lastStart = 0;
        for (int i = 0; i < threadNum; i++) {
            int goUntil = lastStart + parts[i] - 1;
            if (i == threadNum - 1) {
                goUntil++;
            }

            int finalLastStart = lastStart;
            int finalGoUntil = goUntil;
            threads[i] = new Thread(() -> {
                try {
                    Coverage v = bamFileBasicCoverage(bamFile, chr, finalLastStart, finalGoUntil);
                    result.addToTotalCovered(v.getTotalCovered());
                    result.addToTotalDepth(v.getTotalDepth());
                } catch (IOException e) {
                    MultiLogger.get().println("!e IOException when creating basic coverage!");
                }
            });
            threads[i].setName("Coverage Thread " + i + "/" + threadNum);
            threads[i].start();
            lastStart += parts[i];
        }

        for (int i = 0; i < threadNum; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                MultiLogger.get().println("!w Wait for '" + threads[i].getName() + "' join interrupted.");
            }
        }
        return result;
    }

    /**
     * Split a number in to relatively equal parts.
     * So 20, 4 would be [5, 5, 5, 5].
     *
     * @param whole the number.
     * @param parts the amount of parts.
     * @return the parts.
     */
    private int[] splitIntoParts(int whole, int parts) {
        //lets say we do 8 parts.
        int[] arr = new int[parts];
        int lastStart = 0;
        //make parts 1-7 okay-ish
        for (int i = 0; i < parts - 1; i++) {
            int sizeToDo = whole / (parts);
            arr[i] = sizeToDo;
            lastStart += sizeToDo;
        }
        //make sure part 8 does the rest
        arr[parts - 1] = whole - lastStart;
        return arr;
    }
}