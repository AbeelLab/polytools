package consensus;

import cli.Region;
import consensus.encoders.InversionEncoder;
import consensus.encoders.IupacEncoder;
import consensus.filters.ChromosomeFilter;
import consensus.samplers.Sampler;
import fasta.FastaSequence;
import general.CachedReversingStream;
import general.FormattingOutputStream;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.FilteringIterator;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import htsjdk.variant.vcf.VCFContigHeaderLine;
import lombok.Setter;
import vcf.iterator.VCFIterator;
import vcf.iterator.VCFIteratorBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * The class that sequences a chromosome.
 */
public class ConsensusGenerator implements Closeable {
    //TODO create a better way to set a default writeAlt length.
    private static final int DEFAULT_WRITE_LENGTH = 1000;

    private VCFIterator vcfIterator;
    private final FastaSequence fasta;
    private final String chromosome;
    private IupacEncoder encoder;
    private Sampler sampler;
    private final String vcfFile;

    private int lastEnd;

    @Setter
    private boolean useFailingContextRef;

    /**
     * Creates a new ConsensusGenerator from the given Fasta sequence and the VCF file.
     *
     * @param fasta   The The fasta sequence.
     * @param vcfFile The name of the VariantContextsFile
     * @param vcf     The VariationContexts for the fasta sequence.
     * @param encoder The encoder to use.
     * @param sampler The allele sampler.
     */
    public ConsensusGenerator(FastaSequence fasta, String vcfFile,
                              VCFIterator vcf, IupacEncoder encoder, Sampler sampler) {
        this(fasta, vcfFile, vcf, encoder, sampler, fasta != null ? fasta.getGenome()
                : (vcf.getHeader().getContigLines().size() > 0
                ? vcf.getHeader().getContigLines().get(0).getID() : null));
    }

    /**
     * Creates a new ConsensusGenerator from the given Fasta sequence and the VCF file.
     *
     * @param fasta      The fasta sequence.
     * @param vcfFile    The name of the VariantContextsFile
     * @param vcf        The call variant context calls iterator.
     * @param encoder    The IUPAC encoder.
     * @param sampler    The allele sampler.
     * @param chromosome The chromosome.
     */
    public ConsensusGenerator(FastaSequence fasta, String vcfFile,
                              VCFIterator vcf, IupacEncoder encoder, Sampler sampler, String chromosome) {
        this.fasta = fasta;
        this.vcfFile = vcfFile;
        this.chromosome = chromosome;
        this.vcfIterator = vcf;
        this.encoder = encoder;
        this.sampler = sampler;
        this.useFailingContextRef = true;
    }

    private OutputStream initWriting(OutputStream destination, Region region) throws IOException {
        modifyRegionEnd(region);

        //set begin, end and strandedness
        if (region.getEnd() == -1) {
            region.setEnd(DEFAULT_WRITE_LENGTH);
        }

        initializeNewIteratorIfNeeded(region);
        return destination;
    }

    /**
     * Write the consensus region and header to output stream.
     * Applies the correct filters of course.
     *
     * @param destination The output to writeAlt to.
     * @param region      The region to writeAlt.
     * @param filters     the filters to use.
     * @param statsOut    The output stream for statistics.
     * @throws IOException if writing or something failed.
     */
    public void write(OutputStream destination, Region region, VariantContextFilter filters,
                      OutputStream statsOut) throws IOException {
        destination = initWriting(destination, region);
        writeHeader(destination, region);
        if (!region.getStrandedness()) {
            destination = new CachedReversingStream(destination, (region.getEnd() - region.getStart()) + 1);
            encoder = new InversionEncoder(encoder);
        }
        writeConsensus(destination, region.getStart(), region.getEnd(), filters, statsOut);
    }

    /**
     * Writes using a region object.
     *
     * @param destination Where to writeAlt to.
     * @param region      The region to writeAlt.
     * @param filters     The filters to apply.
     * @param statsOut    The output stream for statistics.
     * @throws IOException If anything goes wrong with reading or writing.
     */
    void writeNoHeader(OutputStream destination, Region region, VariantContextFilter filters,
                       OutputStream statsOut) throws IOException {
        destination = initWriting(destination, region);
        writeConsensus(destination, region.getStart(), region.getEnd(), filters, statsOut);
    }

    /**
     * This method creates a new vcfIterator if there is an overlapping region.
     *
     * @param region The new region.
     */
    private void initializeNewIteratorIfNeeded(Region region) throws IOException {
        // We need to reset the iterator if the start is smaller or equal to the last
        // position of the element.
        if (lastEnd == -1) {
            //If the lastEnd is -1 you can use the initial created iterator.
            return;
        }
        if (region.getStart() > lastEnd) {
            // Do not need to reset if you writeAlt after last position.
            return;
        }
        if (vcfIterator != null) {
            vcfIterator.close();
        }
        this.vcfIterator = new VCFIteratorBuilder().open(this.vcfFile);
    }

    private void modifyRegionEnd(Region region) {
        Optional<VCFContigHeaderLine> first = vcfIterator.getHeader().getContigLines()
                .stream().filter(v -> v.getID().equals(chromosome)).findFirst();

        if (first.isPresent()) {
            int chromosomeLength = first.get().getSAMSequenceRecord().getSequenceLength();
            if (region.getEnd() > chromosomeLength || region.getEnd() == -1) {
                region.setEnd(chromosomeLength);
            }
        }
    }


    /**
     * Write a part of the new sequence to an output stream, main writeAlt method!
     *
     * @param destination The OutputStream to writeAlt to.
     * @param begin       From which index (1-index) in the fasta you want to writeAlt.
     * @param end         To max index (1-index) in the fasta you want to writeAlt (inclusive).
     * @param filters     A VCF Filter
     * @throws IOException If something went wrong with writing to the output stream.
     */
    private void writeConsensus(OutputStream destination, int begin, int end,
                                VariantContextFilter filters, OutputStream statsOut) throws IOException {
        if (useFailingContextRef) {
            filters = new ReturningFilterWrapper(filters);
        }
        CloseableIterator<VariantContext> filteredVCFIterator =
                new FilteringIterator(vcfIterator, filters);
        if (this.chromosome != null) {
            filteredVCFIterator = new FilteringIterator(filteredVCFIterator, new ChromosomeFilter(this.chromosome));
        }
        ConsensusStatistics stats = new ConsensusStatistics();
        writeVcfLoop(destination, filteredVCFIterator, begin, end, stats);
        destination.flush();

        if (statsOut != null) {
            statsOut.write(stats.toWriteAbleBytes());
        }
    }

    /**
     * Write from VCF iterator to destination. Use fasta file if needed and possible.
     *
     * @param destination         The destination to writeAlt to.
     * @param filteredVCFIterator The iterator to read from.
     * @param begin               The index to start generating output.
     * @param end                 The index to stop generating output.
     * @throws IOException if destination or any input fails.
     */
    private void writeVcfLoop(OutputStream destination, Iterator<VariantContext> filteredVCFIterator,
                              int begin, int end, ConsensusStatistics statistics) throws IOException {
        LinkedList<VariantContext> relatedContexts = new LinkedList<>();

        int currentWriteIndex = begin;
        VariantContext currentVariantContext;
        while (currentWriteIndex <= end && filteredVCFIterator.hasNext()) {
            currentVariantContext = filteredVCFIterator.next();
            lastEnd = currentVariantContext.getStart() + currentVariantContext.getReference().length() - 1;

            final int startChangePos = currentVariantContext.getStart();
            if (startChangePos < begin) {
                continue;
            }
            if (startChangePos > end) {
                break;
            }
            if (addRelatedContextsToList(currentVariantContext, relatedContexts)) {
                //if added to 'relatedContexts' continue cause maybe we haven't found all related yet.
                continue;
            }
            currentWriteIndex = writeRelatedContexts(destination,
                    currentWriteIndex, end, relatedContexts, statistics);
            //it wrote the old ones, now add new one to (new) list
            relatedContexts = new LinkedList<>();
            relatedContexts.add(currentVariantContext);
        }
        if (!relatedContexts.isEmpty()) {
            currentWriteIndex = writeRelatedContexts(destination,
                    currentWriteIndex, end, relatedContexts, statistics);
        }
        writeFastaUntilPos(destination, currentWriteIndex, end, statistics);
    }

    /**
     * Add variant to the list in case it is related to the other ones in the list.
     * Also adds the current variant in case the list was empty.
     * If it has been added, return true, otherwise return false.
     *
     * @param current         the current context, to maybe add to the list.
     * @param relatedContexts the list of contexts to add the current one to.
     * @return true in case the current context was added.
     */
    private boolean addRelatedContextsToList(VariantContext current, List<VariantContext> relatedContexts) {
        if (relatedContexts.isEmpty()) {
            relatedContexts.add(current);
            return true;
        }

        final int startChangePos = current.getStart();
        for (VariantContext context : relatedContexts) {
            if (startChangePos <= (context.getStart() + context.getReference().length() - 1)) {
                //so we had 10, which was 2 long
                //now we have 11, these are related!
                relatedContexts.add(current);
                return true;
            }
        }
        return false;
    }

    /**
     * This should be where all the magic happens.
     * Write a list of related variant contexts to the output.
     * Return the current writeAlt index, which you will get back next time this method is called.
     * The input 'related contexts' list of this method will never be null and never be empty!
     * This method makes the consensus generator very easily extensible.
     *
     * @param destination       the destination to writeAlt to.
     * @param currentWriteIndex the current writeAlt index.
     * @param end               the end index to stop writing at.
     * @param relatedContexts   the related contexts.
     * @return the new current writeAlt index after writing.
     * @throws IOException in case writing failed.
     */
    private int writeRelatedContexts(OutputStream destination, int currentWriteIndex,
                                     int end, List<VariantContext> relatedContexts,
                                     ConsensusStatistics statistics) throws IOException {
        int minStart = Integer.MAX_VALUE;
        int maxEnd = -1;
        List<Sampler.Sample> samples = new ArrayList<>();
        ConsensusByteArrayOut consensusByteArray = new ConsensusByteArrayOut(destination);

        for (VariantContext context : relatedContexts) {
            minStart = Math.min(minStart, context.getStart());
            maxEnd = Math.max(maxEnd, context.getStart() + context.getReference().length() - 1);
            samples.addAll(sampler.sample(context));
        }

        writeFastaUntilPos(destination,
                currentWriteIndex, Math.min(minStart - 1, end), statistics);

        VariantMerger merger = new VariantMerger(samples);
        List<VariantMerger.Consensus> consensuses = merger.mergeVariants(minStart, maxEnd, statistics);
        int lenToWrite = consensusByteArray.writeConsensus(consensuses, encoder, statistics);
        statistics.totalNucleotides += lenToWrite;
        return maxEnd + 1;
    }

    /**
     * Write from the fasta file or writeAlt dots in case there is no
     * fasta file to writeAlt from at some point.
     *
     * @param destination       The destination to writeAlt to.
     * @param currentWriteIndex The current writeAlt index to start.
     * @param pos               Stop position.
     * @return the new current writeAlt index, which is always 1 after either end of untilPos.
     * @throws IOException in case the output stream of fasta reader failed.
     */
    private int writeFastaUntilPos(OutputStream destination, int currentWriteIndex,
                                   int pos, ConsensusStatistics statistics) throws IOException {
        //writeAlt from FASTA until we are at the right position. Or until the end of the region.
        //writeAlt a ton of '.' in case fasta does not have a record of this position.
        //in case we are already further than we needed to writeAlt until, return.
        if (currentWriteIndex > pos) {
            return currentWriteIndex;
        }

        if (fasta != null) {
            //writeAlt from currentWriteIndex - 1
            int lenToWrite = pos - currentWriteIndex + 1;
            int toWriteAfterLoop = lenToWrite;
            for (int writtenInForLoop = 0; writtenInForLoop < lenToWrite; writtenInForLoop += 8192) {
                byte[] fastaBlock = fasta.read(currentWriteIndex - 1
                        + writtenInForLoop, Math.min(8192, lenToWrite));
                byte[] bytes = encoder.encodeReferenceBytes(fastaBlock);
                writeBytesFormat(destination, bytes, true, bytes.length);
                statistics.fastaReferenceNucleotides += bytes.length;
                statistics.totalNucleotides += bytes.length;
                currentWriteIndex += fastaBlock.length;
                toWriteAfterLoop -= fastaBlock.length;
            }
            byte[] fastaBlock = fasta.read(currentWriteIndex - 1, toWriteAfterLoop);
            byte[] bytes = encoder.encodeReferenceBytes(fastaBlock);
            writeBytesFormat(destination, bytes, true, bytes.length);
            statistics.fastaReferenceNucleotides += bytes.length;
            statistics.totalNucleotides += bytes.length;
            currentWriteIndex += fastaBlock.length;
        }
        //fasta wasn't long enough maybe or something idk
        for (; currentWriteIndex <= pos; currentWriteIndex++) {
            //this writeAlt is fine because this is ref anyway
            destination.write('.');
            statistics.unknown++;
            statistics.totalNucleotides++;
        }
        return currentWriteIndex;
    }

    private void writeBytesFormat(OutputStream stream, byte[] bytes, boolean ref, int len) throws IOException {
        if (ref) {
            stream.write(bytes, 0, len);
            return;
        }
        //alt:
        if (stream instanceof FormattingOutputStream) {
            //writeAlt in color method
            ((FormattingOutputStream) stream).writeAlt(bytes, 0, len);
        } else {
            stream.write(bytes, 0, len);
        }
    }

    /**
     * Writes a header based on the original header, end the begin and end base positions.
     *
     * @param destination The OutputStream to writeAlt to.
     * @param region      The region being transcribed.
     * @throws IOException If something went wrong with writing to the stream.
     */
    private void writeHeader(OutputStream destination, Region region) throws IOException {
        String writingChr = chromosome == null ? " " : chromosome;
        String end = region.getEnd() == -1 ? "?" : String.valueOf(region.getEnd());
        String reg = region.getStart() + "-" + end;
        String type = null;
        String name = null;

        if (!writingChr.endsWith("|")) {
            writingChr = writingChr + "|";
        }

        if (region.getType() != null) {
            type = region.getType();
        }

        if (region.getName() != null) {
            name = region.getName();
        }

        //The format should be >chromosone|region|name|type| or >chromsone|region|
        //When you don't have a name but do have a type we use a space to indicitate no name was present like:
        //>chromsome|region| |type|
        String writeString = ">" + writingChr + reg + "|"
                + (name != null ? name + "|" : type != null ? " |" : "")
                + (type != null ? type + "|" : "") + System.lineSeparator();

        if (destination instanceof FormattingOutputStream) {
            FormattingOutputStream stream = (FormattingOutputStream) destination;
            stream.stopFormatting();
            stream.write(writeString.getBytes("UTF-8"));
            stream.startFormatting();
        } else {
            destination.write(writeString.getBytes("UTF-8"));
        }
    }

    /**
     * Closes the underlying streams used by the ConsensusGenerator.
     */
    public void close() {
        if (vcfIterator != null) {
            vcfIterator.close();
        }
    }

    /**
     * Class for storing and getting statistics about consensus generation.
     */
    static class ConsensusStatistics {
        private int totalNucleotides = 0;

        private int fastaReferenceNucleotides = 0;
        private int referenceNucleotides = 0;
        private int alternativeNucleotides = 0;
        private int heteroNucleotides = 0;
        private int unknown = 0;

        private int insertions = 0;
        private int insertionSize = 0;

        private int deletions = 0;
        private int deletionSize = 0;

        /**
         * Adds reference nucleotides to the statistics tracker.
         *
         * @param n The amount of nucleotides.
         */
        void addReferenceNucleotides(int n) {
            referenceNucleotides += n;
        }

        /**
         * Adds alternative nucleotides to the statistics tracker.
         *
         * @param n The amount of nucleotides.
         */
        void addAlternativeNucleotides(int n) {
            alternativeNucleotides += n;
        }

        /**
         * Adds hetero nucleotides to the statistics tracker.
         *
         * @param n The amount of nucleotides.
         */
        void addHeteroNucleotides(int n) {
            heteroNucleotides += n;
        }

        /**
         * Adds an insertion to the statistics tracker.
         *
         * @param size The size of the insertion.
         */
        void addInsertion(int size) {
            insertions++;
            insertionSize += size;
        }

        /**
         * Adds a deletion to the statistics tracker.
         *
         * @param size The size of the deletion.
         */
        void addDeletion(int size) {
            deletions++;
            deletionSize += size;
        }


        private byte[] toWriteAbleBytes() throws UnsupportedEncodingException {
            final String n = System.lineSeparator();
            return (n + "Consensus Generation Statistics:" + n
                    + "Total written nucleotides: " + totalNucleotides + n
                    + "\tReferences from VCF: " + referenceNucleotides + n
                    + "\tReferences from Fasta: " + fastaReferenceNucleotides + n
                    + "\tAlternatives from VCF: " + alternativeNucleotides + n
                    + "\tHeterozygous call results: " + heteroNucleotides + n
                    + "\tUnknown nucleotides: " + unknown + n
                    + "Insertions / Deletions" + n
                    + "\tInsertions: " + insertions + n
                    + "\tDeletions: " + deletions + n
                    + (insertionSize > 0 ? "\tAverage Insertion Length: "
                    + (insertions / (double) insertionSize) + n : "")
                    + (deletionSize > 0 ? "\tAverage Deletion Length:" + (deletions / (double) deletionSize) : "")
            ).getBytes("UTF-8");
        }
    }
}
