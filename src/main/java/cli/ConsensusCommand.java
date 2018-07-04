package cli;

import cli.options.ConsensusCommandOptions;
import cli.options.ConsensusFilterOptions;
import consensus.ConsensusGenerator;
import consensus.encoders.IupacEncoder;
import consensus.filters.ChromosomeFilter;
import consensus.samplers.AlleleFrequencySampler;
import consensus.samplers.Sampler;
import consensus.samplers.SimpleSampler;
import fasta.FastaSequence;
import general.FormattingOutputStream;
import htsjdk.tribble.TribbleException;
import htsjdk.variant.variantcontext.filter.CompoundFilter;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import logger.MultiLogger;
import org.apache.commons.cli.Options;
import vcf.iterator.VCFIterator;
import vcf.iterator.VCFIteratorBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static cli.options.ConsensusCommandOptions.*;
import static cli.options.ConsensusFilterOptions.SKIP_FAILING_CONTEXT;

/**
 * Class that implements the consensus command on the cli.
 */
public class ConsensusCommand extends Command {
    /**
     * Create a Consensus command using a single String
     * that contains all command line parameters seperated by spaces.
     *
     * @param args String that contains all command line paramaters
     */
    ConsensusCommand(final String args) {
        super(args);
    }

    /**
     * Create a Consensus command using an array of Strings
     * that contains all command line parameters.
     *
     * @param args array of Strings that contains all command line paramaters
     */
    ConsensusCommand(final String[] args) {
        super(args);
    }

    /**
     * Defines all options that can be used with this command.
     *
     * @return object that contains the set of options with their properties
     */
    @Override
    Options defineOptions() {
        Options options = super.defineOptions();
        new ConsensusFilterOptions().defineFilterOptions(options);
        new ConsensusCommandOptions().defineCommandOptions(options);
        return options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String defineSyntax() {
        return "Consensus Sequence Generator";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    int interpretCommand() {
        if (hasOption("help")) {
            displayHelp();
            return 0;
        }
        int statusCode = 0;
        try (FastaSequence fastaSequence = createFastaSequence();
             VCFIterator iterator = createVCFIterator();
             FormattingOutputStream outputStream = determineOutputStream()) {

            List<Region> regions = determineRegions(REGION, ANNOTATION);
            CompoundFilter filter = new ConsensusCommandFilterParser().createFilters(this);

            String vcfFile = getVCFFile();

            ConsensusGenerator generator = makeGenerator(filter, iterator, fastaSequence, vcfFile);

            generator.setUseFailingContextRef(!hasOption(SKIP_FAILING_CONTEXT));
            writeRegions(generator, filter, regions, outputStream);


        } catch (IllegalArgumentException | TribbleException | IOException e) {
            System.out.println("Error interpreting command: " + e.getMessage() + " see log for more information.");
            e.printStackTrace(MultiLogger.get());
            MultiLogger.get().println(e.getClass() + ": " + e.getMessage()
                    + ((e.getCause() == null) ? "" : (" caused by: " + e.getCause().getMessage())));

            statusCode = 1;
        }
        return statusCode;
    }

    private ConsensusGenerator makeGenerator(List<VariantContextFilter> filters, VCFIterator iterator,
                                             FastaSequence fastaSequence, String vcfFile) {
        if (hasOption(CHROMOSOME_OPTION) && getOptionValue(CHROMOSOME_OPTION) != null) {
            String chromosome = getOptionValue(CHROMOSOME_OPTION);
            filters.add(new ChromosomeFilter(chromosome));
            return makeConsensusGenerator(
                    fastaSequence,
                    vcfFile,
                    iterator,
                    new IupacEncoder(),
                    determineSampler(),
                    chromosome);
        } else {
            return makeConsensusGenerator(
                    fastaSequence,
                    vcfFile,
                    iterator,
                    new IupacEncoder(),
                    determineSampler());
        }
    }

    private void writeRegions(ConsensusGenerator generator, CompoundFilter filter,
                              List<Region> regions, FormattingOutputStream outputStream) throws IOException {
        OutputStream statsOut = hasOption(STATS_TO_ERR) ? System.err : hasOption(STATS_TO_OUT) ? System.out : null;
        for (Region region : regions) {
            outputStream.startFormatting();
            generator.write(outputStream, region, filter, statsOut);
            outputStream.stopFormatting();
            outputStream.write(System.lineSeparator().getBytes("UTF-8"));
        }
        generator.close();
    }

    private Sampler determineSampler() {
        if (hasOption(AF_ENCODER_OPTION)) {
            String[] afOptions = getOptionValues(AF_ENCODER_OPTION);
            if (afOptions.length == 1) {
                afOptions = afOptions[0].split("[-:]");
            }
            if (afOptions.length == 2) {
                try {
                    return new AlleleFrequencySampler(Double.parseDouble(afOptions[0]),
                            Double.parseDouble(afOptions[1]));
                } catch (NumberFormatException ignore) {
                    throw new IllegalArgumentException("AE encoder expects lower & upperbound to be numbers");
                }
            }
            throw new IllegalArgumentException("AE encoder expects two numbers as values, usage: -ea <lower>-<upper>");
        }

        if (hasOption(SAMPLE_ENCODER_OPTION)) {
            return new SimpleSampler();
        }
        return new SimpleSampler();
    }

    /**
     * Create a fastasequence from a file specified in the command line.
     *
     * @return the fasta sequence.
     */
    private FastaSequence createFastaSequence() throws IOException {
        String fastaFile = getOptionValue(FASTA_OPTION);
        if (fastaFile == null) {
            return null;
        }
        if (getOptionValues(FASTA_OPTION).length > 1) {
            fastaFile = String.join(" ", getOptionValues(FASTA_OPTION));
        }
        try {
            return new FastaSequence(fastaFile);
        } catch (IOException e) {
            throw new IOException("Could not open file " + fastaFile + " : " + e.getMessage());
        }
    }

    /**
     * Create a vcfIterator from a file specified in the command line.
     *
     * @return the fasta sequence.
     */
    private VCFIterator createVCFIterator() throws IOException {
        String vcfFile = getVCFFile();
        try {
            return new VCFIteratorBuilder().open(vcfFile);
        } catch (IOException e) {
            throw new IOException("Could not open file " + vcfFile + " : " + e.getMessage());
        }
    }

    private String getVCFFile() {
        String vcfFile = getOptionValue(VCF_OPTION);
        if (getOptionValues(VCF_OPTION).length > 1) {
            vcfFile = String.join(" ", getOptionValues(VCF_OPTION));
        }
        return vcfFile;
    }


    /**
     * Determine the output stream from the command line.
     *
     * @return the output stream, with System.out as basic.
     * @throws IOException in case the output stream could not be created.
     */
    private FormattingOutputStream determineOutputStream() throws IOException {
        if (hasOption(OUTPUT_OPTION)) {
            try {
                FileOutputStream fileOut = new FileOutputStream(
                        new File(getOptionValue(OUTPUT_OPTION)));
                return new FormattingOutputStream(fileOut, 70);
            } catch (IOException e) {
                throw new IOException("Could not open or create output file: " + e.getMessage());
            }
        } else {
            int color = 0;
            if (hasOption(COLOR_OPTION)) {
                try {
                    color = Integer.parseInt(getOptionValue(COLOR_OPTION));
                } catch (NumberFormatException e) {
                    MultiLogger.get().println("Could not parse " + getOptionValue(COLOR_OPTION) + " to integer.");
                }
            }
            return new FormattingOutputStream(System.out, 70, color);
        }
    }

    /**
     * Currently package private for testing purposes, pending a refactoring.
     *
     * @param fasta      The fasta sequence for the reference genome.
     * @param vcfFile    The name of the VCF-File
     * @param vcf        The vcf iterator with the variations.
     * @param encoder    The encoder to use.
     * @param sampler    The sampler to use.
     * @param chromosome The name of the chromosome.
     * @return A new ConsensusGenerator.
     */
    ConsensusGenerator makeConsensusGenerator(FastaSequence fasta, String vcfFile, VCFIterator vcf,
                                              IupacEncoder encoder, Sampler sampler, String chromosome) {
        return new ConsensusGenerator(fasta, vcfFile, vcf, encoder, sampler, chromosome);
    }

    /**
     * Currently package private for testing purposes, pending a refactoring.
     *
     * @param fasta   The fasta sequence for the reference genome.
     * @param vcfFile The name of the VCF-File
     * @param vcf     The vcf iterator with the variations.
     * @param encoder The encoder to use.
     * @param sampler The sampler to use.
     * @return A new ConsensusGenerator.
     */
    ConsensusGenerator makeConsensusGenerator(
            FastaSequence fasta, String vcfFile, VCFIterator vcf, IupacEncoder encoder, Sampler sampler) {
        return new ConsensusGenerator(fasta, vcfFile, vcf, encoder, sampler);
    }

}