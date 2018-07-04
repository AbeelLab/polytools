package cli.options;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * Define the commandline options for the consensus command.
 */
public class ConsensusCommandOptions {

    /**
     * String used for fasta file option reference and display in help message.
     */
    public static final String FASTA_OPTION = "fasta-ref";
    /**
     * String used for vcf file option reference and display in help message.
     */
    public static final String VCF_OPTION = "calls";
    /**
     * String used for output specification option and display in help message.
     */
    public static final String OUTPUT_OPTION = "out";
    /**
     * String used for allele frequency encoder option and display in help message.
     */
    public static final String AF_ENCODER_OPTION = "af-encoder";
    /**
     * String used for sample encoder option and display in help message.
     */
    public static final String SAMPLE_ENCODER_OPTION = "sample-encoder";
    /**
     * String used for color option and display in help message.
     */
    public static final String COLOR_OPTION = "color";
    /**
     * String used for region specification option and display in help message.
     */
    public static final String REGION = "region";

    /**
     * String used for annotation specification option and display in help message.
     */
    public static final String ANNOTATION = "annotation";

    /**
     * String used for chromosome specification option and display in help message.
     */
    public static final String CHROMOSOME_OPTION = "chromosome";

    /**
     * String used for stats out stream specification option and display in help message.
     */
    public static final String STATS_TO_ERR = "stats-to-err";

    /**
     * String used for stats out stream specification option and display in help message.
     */
    public static final String STATS_TO_OUT = "stats-to-out";

    /**
     * Adds the non-filter related options to the consensus-command.
     *
     * @param options The options.
     */
    public void defineCommandOptions(Options options) {
        defineFastaOption(options);
        defineCallOptions(options);
        defineRegionOptions(options);
        defineChromosomeOption(options);
        defineOutputOption(options);
        defineAFEncoderOption(options);
        defineSampleEncoderOptions(options);
        defineColorOption(options);
        defineHelpOption(options);
        defineStatsToOutOption(options);
        defineStatsOption(options);
    }

    private void defineStatsOption(Options options) {
        Option option = Option.builder("so")
                .required(false).hasArg(false).longOpt(STATS_TO_OUT)
                .desc("Output the consensus generation statistics to the standard out stream")
                .build();
        options.addOption(option);
    }

    private void defineStatsToOutOption(Options options) {
        Option helpOption = Option.builder("se")
                .required(false).hasArg(false).longOpt(STATS_TO_ERR)
                .desc("Output the consensus generation statistics to the error stream")
                .build();
        options.addOption(helpOption);
    }

    private void defineHelpOption(Options options) {
        Option helpOption = Option.builder("h")
                .required(false).hasArg(false).longOpt("help")
                .desc("Show this help message and quit.")
                .build();
        options.addOption(helpOption);
    }

    private void defineColorOption(Options options) {
        Option colorOption = Option.builder("col")
                .required(false).hasArg(true).longOpt(COLOR_OPTION)
                .desc("Use color in output only if output is not to file, use 0 for no color, "
                        + "1 for only giving IUPAC letters color, or 2 to give every base a color."
                        + "Use color 3 to only color insertions and deletions with respect to reference."
                        + "When using option 3, it will color all the nucleotides that where part of a variant with a "
                        + "insertion or deletion."
                        + "(Default when writing to console: 0)")
                .build();
        options.addOption(colorOption);
    }

    private void defineSampleEncoderOptions(Options options) {
        Option sampleEncoderOption = Option.builder("es")
                .required(false).hasArg(false).longOpt(SAMPLE_ENCODER_OPTION)
                .desc("Use an encoder based on the sample field. "
                        + "The default encoder also works based on the sample.")
                .build();
        options.addOption(sampleEncoderOption);
    }

    private void defineAFEncoderOption(Options options) {
        Option afEncoderOption = Option.builder("ea")
                .required(false).hasArg(true).longOpt(AF_ENCODER_OPTION)
                .desc("Use an encoder based on the allele frequency field. "
                        + "The default encoder chooses based on the sample.")
                .build();
        options.addOption(afEncoderOption);
    }

    private void defineOutputOption(Options options) {
        Option outputOption = Option.builder("o")
                .required(false).hasArg(true).longOpt(OUTPUT_OPTION)
                .desc("Filename and path of output fasta file.").build();
        options.addOption(outputOption);
    }

    private void defineChromosomeOption(Options options) {
        Option chromosomeOption = Option.builder("chr")
                .required(false)
                .hasArg()
                .longOpt(CHROMOSOME_OPTION)
                .desc("Specify a chromosome to pick samples from")
                .build();
        options.addOption(chromosomeOption);
    }

    private void defineRegionOptions(Options options) {
        Option regionOption = Option.builder("r")
                .required(false).hasArgs().longOpt(REGION).valueSeparator(' ')
                .desc("Define regions to generate consensus sequences with. "
                        + "Format: [start position]-[end position], start and end inclusive. "
                        + "Alternatively a single position can be obtained with: [position]"
                        + "Multiple regions can be used separated by spaces, "
                        + "overlapping regions currently not supported. "
                        + "Start position has to be smaller than end position.")
                .build();
        Option annotationsOption = Option.builder("a")
                .required(false)
                .hasArgs()
                .longOpt(ANNOTATION)
                .desc("Uses an annotation file and a name to determine the regions to print.")
                .build();

        OptionGroup regionGroup = new OptionGroup();

        //Not required since it can also be in the config file.
        regionGroup.addOption(regionOption).addOption(annotationsOption).setRequired(false);
        options.addOptionGroup(regionGroup);
    }

    private static void defineCallOptions(Options options) {
        Option callOption = Option.builder("c")
                .required(true).hasArg(true).longOpt(VCF_OPTION)
                .desc("vcf file that contains the calls").build();
        options.addOption(callOption);
    }

    private static void defineFastaOption(Options options) {
        Option fastaOption = Option.builder("f")
                .required(false).hasArg(true).longOpt(FASTA_OPTION)
                .desc("reference fasta-file").build();
        options.addOption(fastaOption);
    }
}
