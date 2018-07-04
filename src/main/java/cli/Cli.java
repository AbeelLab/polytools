package cli;


import examples.Examples;
import examples.SpecialCaseFinder;
import logger.LogLevel;
import logger.MultiLogger;

import java.time.LocalDateTime;

/**
 * Displays the main help message and chooses the command to execute.
 */
public final class Cli {
    /**
     * Main method for command line.
     * Starts the logger, then passes arguments to launch method.
     *
     * @param args the program arguments.
     */
    public static void main(String[] args) {
        //output all info to a log file:
        MultiLogger.init(false, false, "./"
                + NAME.toLowerCase() + "-" + VERSION + ".log", LogLevel.OTHER);
        MultiLogger.get().setStdLogLevel(LogLevel.NOTHING);
        String dateString = LocalDateTime.now().toLocalDate().toString();
        MultiLogger.get().println("-- Started on " + dateString + " with args: '"
                + String.join(" ", args) + "'");
        int errorCode = Cli.launch(args);
        MultiLogger.get().println("-- Exit with error code: " + errorCode);
        System.exit(errorCode);
    }

    /**
     * constructor.
     */
    private Cli() {
        // not called
    }

    /**
     * Version number of the program, should be read from Gradle in future.
     */
    private static final String VERSION = "2.0.0";

    /**
     * Name of the executable.
     */
    private static final String NAME = "PolyTools";

    /**
     * Provides help message contents.
     *
     * @return String with help message contents.
     */
    public static String getHelp() {
        return (NAME + " v" + VERSION + ", a polyploid consensus sequence generator.\n"
                + "Usage: " + NAME + " [--version][--help] <command> <option1> <option2> ..\n\n"
                + "Available option arguments:\n"
                + "\thelp         - Display this help text and exit.\n"
                + "\tversion      - Display the " + NAME + " version and exit.\n"
                + "\tconsensus    - Run the sequencer with specified arguments.\n"
                + "\t                  Use '" + NAME + " consensus -h' to show arguments.\n"
                + "\t                  The outputted sequence will use '[...]' to indicate\n"
                + "\t                  heterozygous deletions\n"
                + "\t                  and '(...)' to indicate heterozygous insertions.\n"
                + "\tcoverage     - Run the BAM coverage generator with specified arguments.\n"
                + "\t                  Use '" + NAME + " coverage -h' to show arguments.\n"
                + "\texamples     - Run predefined examples with a tiny explanation.");
    }

    /**
     * Displays usage and help message.
     */
    private static void displayHelp() {
        MultiLogger.get().println("!i Displaying help");
        System.out.println(getHelp());
    }

    /**
     * Launches command with options.
     *
     * @param args array of Strings that each contain a command line option
     * @return int the exitcode.
     */
    public static int launch(final String... args) {
        if (args.length < 1) {
            displayHelp();
            return 1;
        }
        String[] cmdArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
            case "help":
            case "--help":
            case "-h":
                displayHelp();
                return 0;
            case "version":
            case "--version":
            case "-v":
                MultiLogger.get().println("!i Displaying version");
                System.out.println(NAME + " - version: " + VERSION);
                return 0;
            case "consensus":
                return (new ConsensusCommand(cmdArgs)).execute();
            case "coverage":
                return (new CoverageCommand(cmdArgs)).execute();
            case "examples":
                Examples.main(cmdArgs);
                return 0;
            case "testgen":
                SpecialCaseFinder.main(cmdArgs);
                return 0;
            default:
                displayHelp();
                return 1;
        }
    }

}
