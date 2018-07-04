package cli;

import annotations.Annotation;
import annotations.GFF;
import logger.MultiLogger;
import lombok.Getter;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.*;

/**
 * Abstract class that implements common methods among commands.
 */
abstract class Command {
    @Getter
    private Options options;
    private CommandLineParser parser;
    private String[] args;
    @Getter
    private CommandLine cmdl;
    @Getter
    private CommandLine config;

    /**
     * String used for config file specification option and display in help message.
     */
    private static final String CONFIG = "configuration-file";

    /**
     * Create a Command using a single String.
     * that contains all command line parameters seperated by spaces.
     *
     * @param args String that contains all command line paramaters
     */
    Command(final String args) {
        this(args.split("\\s+"));
    }

    /**
     * Create a Consensus command using an array of Strings
     * that contains all command line parameters.
     *
     * @param args array of Strings that contains all command line paramaters
     */
    Command(final String[] args) {
        this.args = args;
        parser = new DefaultParser();
        options = defineOptions();
        cmdl = null;
        config = null;
    }

    /**
     * Defines all options that can be used with this command
     * that implements this class.
     *
     * @return object that contains the set of options with their properties
     */
    Options defineOptions() {
        Options options = new Options();
        Option configOption = Option.builder("conf")
                .required(false)
                .numberOfArgs(1)
                .longOpt(CONFIG)
                .desc("Path to a configuration file, that contains additional command line arguments on seperate lines"
                        + "The command line takes precedence if arguments are defined in both ")
                .build();
        options.addOption(configOption);
        return options;
    }

    /**
     * Implements how interpretation and execution of the implementing command.
     *
     * @return Exit status code.
     */
    abstract int interpretCommand();

    /**
     * Provides the syntax string for the help message.
     *
     * @return String that expresses the syntax of the command
     */
    abstract String defineSyntax();

    /**
     * Displays usage and help message.
     */
    void displayHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(defineSyntax(), options);
    }

    /**
     * Creates a copy of the command options, but with all options as optional.
     *
     * @return options as optional
     */
    private Options getOptionsAsOptional() {
        Options optionalOpts = new Options();
        for (Option option : defineOptions().getOptions()) {
            option.setRequired(false);
            optionalOpts.addOption(option);
        }
        return optionalOpts;
    }

    /**
     * Returns the option value of the command line if present, otherwise from the config file.
     *
     * @param opt Name of the command line option
     * @return the option value
     */
    String getOptionValue(String opt) {
        if (cmdl == null) {
            return null;
        }
        String value = cmdl.getOptionValue(opt);
        if (value == null && config != null) {
            return config.getOptionValue(opt);
        }
        return value;
    }

    /**
     * Returns the option value of the command line if present, otherwise from the config file.
     *
     * @param opt Name of the command line option.
     * @return the option value.
     */
    String[] getOptionValues(String opt) {
        if (cmdl == null) {
            return null;
        }
        if (cmdl.hasOption(opt)) {
            return cmdl.getOptionValues(opt);
        }
        if (config != null) {
            return config.getOptionValues(opt);
        }
        return null;
    }

    /**
     * Checks if option is present.
     *
     * @param opt option to be checked for presence.
     * @return whether the option is present.
     */
    boolean hasOption(String opt) {
        return cmdl.hasOption(opt) || config.hasOption(opt);
    }

    /**
     * Reads a config file into a parsable command line.
     *
     * @param file File containing options.
     * @return String array of args.
     * @throws IOException If file does not exist errors occur during reading.
     */
    private String[] readConfig(String file) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#")) {
                lines.addAll(Arrays.asList(line.trim().split("\\s")));
            }
        }
        reader.close();
        return lines.toArray(new String[]{});
    }

    /**
     * Tries to parse command line options
     * and displays usage message when invalid.
     *
     * @return Exit status code.
     */
    int parseArgs() {
        try {
            cmdl = this.parser.parse(options, args);
        } catch (ParseException e) {
            displayHelp();
            System.out.println("\nCould not parse command line:\n" + e.getMessage());
            MultiLogger.get().println("!f Command line '" + String.join(" ", args)
                    + "' could not be parsed " + e.getMessage());
            return 1;
        }
        String configPath = cmdl.getOptionValue(CONFIG);
        String[] configArgs = {};
        if (configPath != null) {
            try {
                configArgs = readConfig(configPath);
            } catch (IOException e) {
                System.out.println("Something went wrong when reading the config file!\n"
                        + "If you want to run without config file, remove the config option.");
                MultiLogger.get().println("!f exception reading config file: '"
                        + configPath + "' message: " + e.getMessage());
                return 1;
            }
        }
        try {
            config = this.parser.parse(getOptionsAsOptional(), configArgs);
        } catch (ParseException e) {
            System.out.println("Something went wrong when parsing the config file!\n"
                    + "If you want to run without config file, remove the config option.");
            MultiLogger.get().println("!f exception parsing config: " + e.getMessage());
            return 1;
        }
        return 0;
    }

    /**
     * Execute the command with arguments.
     *
     * @return Exit status code.
     */
    int execute() {
        if (parseArgs() == 0) {
            return interpretCommand();
        }
        return 1;
    }

    /**
     * Determine the region.
     *
     * @param regString        the region cmd string.
     * @param annotationString the annotation string.
     * @return the region, either the one in CommandLine or [1, -1]
     * @throws FileNotFoundException if the file was not found.
     */
    List<Region> determineRegions(String regString, String annotationString) throws FileNotFoundException {
        List<Region> results = new ArrayList<>();
        if (getOptionValues(annotationString) != null && getOptionValues(annotationString).length > 0) {
            try {
                String[] values = getOptionValues(annotationString);
                String filePath = values[0];

                File file = new File(filePath);
                if (!file.exists()) {
                    throw new FileNotFoundException("Annotation file not found: " + file.getAbsolutePath());
                }
                GFF gff = new GFF(file);
                for (int i = 1; i < values.length; i++) {
                    results.addAll(gff.getAllByName(values[i]).getAnnotations());
                }
                // Get all annotations that have no parents if no name was specified
                if (values.length < 2) {
                    System.out.println("No annotations where specified, doing all annotations!");
                    results.addAll(gff.getAllRoots().getAnnotations());
                }
            } catch (Annotation.RecursionException | IOException e) {
                MultiLogger.get().println("!w " + e.getMessage());
                throw new IllegalArgumentException("Annotation file was not a valid file: " + e.getMessage());
            }
        }
        if (hasOption(regString)) {
            RegionParser parser = new RegionParser();
            results.addAll(parser.parseRegions(getOptionValues(regString)));
        }
        if (results.isEmpty()) {
            System.out.println("No regions where specified with either --region or "
                + "--annotation, doing full region!");
            results.add(new CommandRegion(1, -1));
        }
        return results;
    }
}
