package logger;

/**
 * Class for specifying the level of a given log.
 */
@SuppressWarnings("unused")
public enum LogLevel {
    NOTHING("", ""),
    NO_LOG("", "\u001B[0m"), //reset
    FATAL("[!!FATAL!!]", "\u001B[31m"), //RED
    ERROR("[  ERROR  ]", "\u001B[31m"), //RED
    WARNING("[ WARNING ]", "\u001B[33m"), //YELLOW
    INFO("[  INFO   ]", "\u001B[0m"), //RESET
    DEBUG("[  DEBUG  ]", "\u001B[36m"), //CYAN
    OTHER("[  OTHER  ]", "\u001B[0m"); //RESET


    private final String ansiColor;
    private final String prefix;


    /**
     * Basic constructor.
     *
     * @param toString  the prefix.
     * @param colorCode the color code.
     */
    LogLevel(String toString, String colorCode) {
        this.prefix = toString;
        this.ansiColor = colorCode;
    }

    /**
     * Check if some log level is higher than some other log level.
     *
     * @param level the level to compare this to.
     * @return true if the other object is higher than this.
     */
    public boolean shouldPrint(LogLevel level) {
        return (this.compareTo(level) >= 0);
    }

    /**
     * Add the normal prefix to a string.
     *
     * @param s the string to add to.
     * @return the resulting string.
     */
    public String prefix(String s) {
        return prefix + s;
    }

    /**
     * Add the color prefix and the normal prefix to a string.
     *
     * @param s the string to add to.
     * @return the new string.
     */
    public String colorPrefix(String s) {
        return ansiColor + prefix + " " + s;
    }
}
