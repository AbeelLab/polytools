package cli.options;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * Define the options for the coverage command.
 */
public class CoverageCommandOptions {
    /**
     * String used for bam file option reference and display in help message.
     */
    public static final String BAM_OPTION = "bam-file";
    /**
     * String used for chromosome name option reference and display in help message.
     */
    public static final String CHR_NAME = "chromosome-name";
    /**
     * String used for region file option reference and display in help message.
     */
    public static final String REGION = "region";
    /**
     * String used for mapping quality filter option reference and display in help message.
     */
    public static final String MAPPING_QUALITY = "mapping-quality";
    /**
     * String used for duplicate reads filter option reference and display in help message.
     */
    public static final String DUPLICATE_READS = "duplicate-reads";
    /**
     * String used for fails vendor read quality filter option reference and display in help message.
     */
    public static final String FAIL_VENDOR_READ_Q = "fails-vendor-read-quality";
    /**
     * String used for secondary or supplementary filter option reference and display in help message.
     */
    public static final String SECONDARY_OR_SUPPLEMENTARY = "secondary-or-supplementary";
    /**
     * String used for minimal coverage variable reference and display in help message.
     */
    public static final String MINIMAL_COVERAGE = "minimal-coverage";
    /**
     * String used for default settings option reference and display in help message.
     */
    public static final String DEFAULT_SETTINGS = "default-settings";
    /**
     * String used for array coverage report option reference and display in help message.
     */
    public static final String ARRAY_COVERAGE = "array-coverage";
    /**
     * String used for amount of threads option reference and display in help message.
     */
    public static final String AMOUNT_OF_THREADS = "threads";
    /**
     * String used for giving region with annotations.
     */
    public static final String ANNOTATION = "annotation";

    /**
     * Add the coverage options.
     * @param options The cli options object.
     */
    public void determineCoverageOptions(Options options) {
        determineBamOption(options);
        determineChrOption(options);
        determineArrayCoverage(options);
        determineCoverageOption(options);
        determineMappingOption(options);
        determineDuplicateOptions(options);
        determineVendorsOption(options);
        determineSecOption(options);
        determineDefaultOption(options);
        determineHelpOption(options);
        determineThreadOption(options);
        determineRegionOption(options);
    }

    private void determineBamOption(Options options) {
        Option bamOption = Option.builder("b")
                .required(true).hasArg(true).longOpt(BAM_OPTION)
                .desc("the bam coverage file to use")
                .build();
        options.addOption(bamOption);
    }

    private void determineChrOption(Options options) {
        Option chrOption = Option.builder("c").required(true).hasArg(true)
                .longOpt(CHR_NAME).desc("chromosome name to report coverage of")
                .build();
        options.addOption(chrOption);
    }

    private void determineArrayCoverage(Options options) {
        Option arrayCoverageOption = Option.builder("A").required(false)
                .hasArg(false).longOpt(ARRAY_COVERAGE)
                .desc("give more extensive coverage report")
                .build();
        options.addOption(arrayCoverageOption);
    }

    private void determineCoverageOption(Options options) {
        Option coverageOption = Option.builder("m")
                .required(false).hasArg(true).longOpt(MINIMAL_COVERAGE)
                .desc("minimal coverage to be considered covered (default: 1)")
                .build();
        options.addOption(coverageOption);
    }

    private void determineMappingOption(Options options) {
        Option mappingOption = Option.builder("fq")
                .required(false).hasArg(true).longOpt(MAPPING_QUALITY)
                .desc("use (a.o.) the mapping quality filter")
                .build();
        options.addOption(mappingOption);
    }

    private void determineDuplicateOptions(Options options) {
        Option duplicateOption = Option.builder("fd")
                .required(false).hasArg(false).longOpt(DUPLICATE_READS)
                .desc("use (a.o.) the duplicate reads filter")
                .build();
        options.addOption(duplicateOption);
    }

    private void determineVendorsOption(Options options) {
        Option failsVendorOption = Option.builder("fv")
                .required(false).hasArg(false).longOpt(FAIL_VENDOR_READ_Q)
                .desc("use (a.o.) the fails vendor read quality filter")
                .build();
        options.addOption(failsVendorOption);
    }

    private void determineSecOption(Options options) {
        Option secOption = Option.builder("fs")
                .required(false).hasArg(false).longOpt(SECONDARY_OR_SUPPLEMENTARY)
                .desc("use (a.o.) the secondary or supplementary filter")
                .build();
        options.addOption(secOption);
    }

    private void determineDefaultOption(Options options) {
        Option defaultOption = Option.builder("fy")
                .required(false).hasArg(false).longOpt(DEFAULT_SETTINGS)
                .desc("use default settings, overrides any filters set in the command line arguments")
                .build();
        options.addOption(defaultOption);
    }

    private void determineHelpOption(Options options) {
        Option helpOption = Option.builder("h")
                .required(false).hasArg(false).longOpt("help")
                .desc("show this help message and quit")
                .build();
        options.addOption(helpOption);
    }

    private void determineThreadOption(Options options) {
        Option threadOption = Option.builder("t")
                .required(false).hasArg(true).longOpt(AMOUNT_OF_THREADS)
                .desc("use a variable amount of threads to speed up analysis,"
                        + " only available for basic coverage (for now)")
                .build();
        options.addOption(threadOption);
    }

    private void determineRegionOption(Options options) {
        Option regionOption = Option.builder("r").required(false).hasArg(true)
                .longOpt(REGION).desc("region description of chromosome to analyse")
                .build();
        Option annotationsOption = Option.builder("a")
                .required(false)
                .hasArg(true)
                .longOpt(ANNOTATION)
                .desc("Uses an annotation file and a name to determine the regions to print.")
                .build();
        OptionGroup regionGroup = new OptionGroup();
        //Not required since the region can also be in the config.
        regionGroup.addOption(regionOption).addOption(annotationsOption).setRequired(false);
        options.addOptionGroup(regionGroup);
    }

}
