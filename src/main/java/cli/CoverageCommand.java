package cli;

import cli.options.CoverageCommandOptions;
import coverage.Coverage;
import coverage.CoverageArray;
import coverage.CoverageGenerator;
import logger.MultiLogger;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.util.List;

import static cli.options.CoverageCommandOptions.*;

/**
 * Command line option for coverage generation.
 */
class CoverageCommand extends Command {



    /**
     * Constructor with command line arguments.
     *
     * @param args the arguments after 'coverage'.
     */
    CoverageCommand(String[] args) {
        super(args);
    }

    /**
     * Define the possible command line options for this command.
     *
     * @return the options.
     */
    @Override
    Options defineOptions() {
        Options options = super.defineOptions();
        new CoverageCommandOptions().determineCoverageOptions(options);
        return options;
    }

    /**
     * Parse a string to an integer, if it fails, return the 'otherwise' value.
     *
     * @param s         the string to parse to an integer.
     * @param otherwise the value to return on fail.
     * @return the parsed string or the otherwise value.
     */
    private int parseInt(String s, int otherwise) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return otherwise;
        }
    }

    /**
     * Interpret the commandline options and execute them.
     *
     * @return the status code for how it went.
     */
    @Override
    int interpretCommand() {
        if (hasOption("help")) {
            displayHelp();
            return 0;
        }

        try {
            List<Region> regions = determineRegions(REGION, ANNOTATION);
            Region region = regions.get(0);
            CoverageGenerator generator;
            if (hasOption(DEFAULT_SETTINGS)) {
                generator = new CoverageGenerator();
            } else {
                generator = getDefaultGenerator();
            }

            if (hasOption(ARRAY_COVERAGE)) {
                execArrayCoverage(generator, region);
            } else {
                execStandardCoverage(generator, region);
            }
        } catch (RuntimeException | IOException e) {
            System.out.println("Error interpreting command: " + e.getMessage() + " see log for more information.");
            MultiLogger.get().println(e.getClass() + ": " + e.getMessage()
                    + ((e.getCause() == null) ? "" : (" caused by: " + e.getCause().getMessage())));
            return 1;
        }
        return 0;
    }

    private void execStandardCoverage(CoverageGenerator generator, Region region) throws IOException {
        Coverage c = generator.bamFileBasicCoverageThreads(
                hasOption(AMOUNT_OF_THREADS) ? parseInt(getOptionValue(AMOUNT_OF_THREADS), 1) : 1,
                getOptionValue(BAM_OPTION),
                getOptionValue(CHR_NAME),
                region.getStart(), region.getEnd());
        System.out.println(c.toString());
    }

    private void execArrayCoverage(CoverageGenerator generator, Region region) throws IOException {
        CoverageArray c = generator.bamFileArrayCoverage(
                getOptionValue(BAM_OPTION),
                getOptionValue(CHR_NAME),
                region.getStart(), region.getEnd());
        System.out.println(c.coverageStatisticsOverview(4));
    }

    private CoverageGenerator getDefaultGenerator() {
        int minCoverage = hasOption(MINIMAL_COVERAGE)
                ? parseInt(getOptionValue(MINIMAL_COVERAGE), 1) : 1;
        int mappingQuality = hasOption(MAPPING_QUALITY)
                ? parseInt(getOptionValue(MAPPING_QUALITY), -1) : -1;
        return new CoverageGenerator(minCoverage, mappingQuality,
                hasOption(DUPLICATE_READS), hasOption(FAIL_VENDOR_READ_Q),
                hasOption(SECONDARY_OR_SUPPLEMENTARY));
    }

    /**
     * Define the text about the 'syntax' description.
     *
     * @return the string to show.
     */
    @Override
    String defineSyntax() {
        return "BAM Coverage Generator";
    }
}
