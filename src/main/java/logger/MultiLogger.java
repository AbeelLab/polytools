package logger;

import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

/**
 * Summary:
 * Call MultiLogger.init() to begin.
 * After that System.out will be replaced with this MultiLogger.
 * If you don't want to replace System.out, call MultiLogger.init(false).
 * a third .init method is also provided for additional options (writing to log file).
 *
 *
 * Special functions you can use after that:
 *  - addOutStream, which adds an output stream to print to.
 *  - get, to get the instance, which returns the instance of this logger.
 *  - exit, which stops the MultiLogger and places System.out back.
 *
 *
 * Getters and Setters for settings which you can use/change:
 *  - get/set default log level
 *  - set printing to the default StdOut to use color or not.
 *  - set log levels that should be printed to StdOut.
 *
 */
public class MultiLogger extends PrintStream {

    /**
     * Object used for locking the init and exit methods.
     */
    private static final Object INIT_LOCK = new Object();

    /**
     * Singleton instance.
     */
    private static MultiLogger instance = null;

    /**
     * The default log level.
     */
    private LogLevel defaultLogLevel = LogLevel.NO_LOG;

    /**
     * List of output streams to writeAlt to.
     */
    private final LinkedList<LoggerOutStream> outputStreams;

    /**
     * will be bigger than 0 if you are showing a loading bar.
     * and smaller than or equal to 0 otherwise.
     */
    private int showingLoadingBar;

    /**
     * MultiLogger constructor.
     *
     * @param stdOutColor turn on ANSI colors on StdOut.
     * @param logFileName output to a log file with a given name.
     *                    This parameter can be null to not output to any log file.
     * @param fileLevel the log level for the output file, which can be null if logFileName is null.
     */
    private MultiLogger(boolean stdOutColor, String logFileName, LogLevel fileLevel,
                        boolean fileAppend) throws UnsupportedEncodingException {
        super(System.out, false, "UTF-8");
        showingLoadingBar = -1;
        outputStreams = new LinkedList<>();

        //basic out streams: a file and STD out
        addOutStream(getStdOut(), stdOutColor, LogLevel.OTHER);
        if (logFileName != null && !logFileName.isEmpty()) {
            addOutStream(initFileOutputStream(logFileName, fileAppend),
                    false, fileLevel);
        }
    }

    /**
     * Add a stream.
     *
     * @param stream       the stream.
     * @param colorSupport use ANSI colors.
     * @param level        only print above or equal to level.
     */
    public void addOutStream(OutputStream stream, boolean colorSupport, LogLevel level) {
        outputStreams.add(new LoggerOutStream(stream, colorSupport, level));
    }

    /**
     * Check to make sure that at least one stream is still open.
     * In which case the logger is still useful.
     */
    private void ensureOpen() throws IOException {
        outputStreams.removeIf(loggerOutStream -> loggerOutStream == null
                || loggerOutStream.stream == null);
        if (outputStreams.isEmpty()) {
            throw new IOException("All logger output streams are closed");
        }
    }

    /**
     * Flushes the streams. This is done by writing any buffered output bytes to
     * the underlying output streams and then flushing those streams.
     */
    @Override
    public void flush() {
        synchronized (this) {
            try {
                ensureOpen();
                for (LoggerOutStream o : outputStreams) {
                    if (o.stream == this) {
                        continue;
                    }
                    o.flush();
                }
            } catch (Exception x) {
                setError();
            }
        }
    }

    /**
     * Closes the stream.  This is done by flushing the streams and then closing
     * the underlying output streams.
     */
    @Override
    public void close() {
        synchronized (this) {
            try {
                ensureOpen();
                for (LoggerOutStream stream : outputStreams) {
                    //check to avoid recursive errors
                    if (stream.stream == this) {
                        continue;
                    }
                    //never close the System.out stream.
                    if (stream.stream != null && stream.stream != out) {
                        stream.flush();
                        stream.close();
                    }
                }
            } catch (Exception x) {
                setError();
            }
            if (System.out == this) {
                System.setOut(getStdOut());
            }
        }
    }

    /**
     * Does the actual writeAlt magic.
     *
     * @param outputPair to writeAlt, in not color and color.
     */
    private void writeToStreams(LogOutputPair outputPair) {
        try {
            //all data passes through here now... finally
            synchronized (this) {
                ensureOpen();
                for (LoggerOutStream o : outputStreams) {
                    if (!o.write(outputPair)) {
                        setError();
                    }
                }
            }
        } catch (IOException x) {
            setError();
        }
    }

    /**
     * Private but main writeAlt method.
     *
     * @param s the not yet formatted String to writeAlt
     */
    private void write(String s) {
        if (s == null || showingLoadingBar > 0) {
            return;
        }

        //if enter, just only hit enter, if more, format it
        writeToStreams(s.equals("\n") ? new LogOutputPair("\n",
                "\n", LogLevel.NO_LOG) : formatAsLog(s));
    }

    /**
     * Writes the specified int to the streams.
     * (Useless, I know, but we needed to override this...)
     *
     * @param b The int to be written.
     */
    @Override
    public void write(int b) {
        write(String.valueOf((byte) b));
    }

    /**
     * Writes the bytes in buf, then flush the steams.
     *
     * @param buf A byte array
     * @param off Offset from which to start taking bytes
     * @param len Number of bytes to writeAlt
     */
    @Override
    public void write(byte[] buf, int off, int len) {
        try {
            write(new String(buf, off, len, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            setError();
        }
    }

    /**
     * Terminates the current line by writing the line separator string '\n'.
     */
    @Override
    public void println() {
        write("\n");
    }

    /**
     * Prints a boolean and then terminate the line.
     *
     * @param x The boolean to be printed
     */
    @Override
    public void println(boolean x) {
        print(x + "\n");
    }

    /**
     * Prints a character and then terminate the line.
     *
     * @param x The char to be printed.
     */
    @Override
    public void println(char x) {
        print(x + "\n");
    }

    /**
     * Prints an integer and then terminate the line.
     *
     * @param x The int to be printed.
     */
    @Override
    public void println(int x) {
        print(x + "\n");
    }

    /**
     * Prints a long and then terminate the line.
     *
     * @param x The long to be printed.
     */
    @Override
    public void println(long x) {
        print(x + "\n");
    }

    /**
     * Prints a float and then terminate the line.
     *
     * @param x The float to be printed.
     */
    @Override
    public void println(float x) {
        print(x + "\n");
    }

    /**
     * Prints a double and then terminate the line.
     *
     * @param x The double to be printed.
     */
    @Override
    public void println(double x) {
        print(x + "\n");
    }

    /**
     * Prints an array of characters and then terminate the line.
     * Use is not recommended (relatively slow).
     *
     * @param x an array of chars to print.
     */
    @Override
    public void println(char[] x) {
        print(new String(x) + "\n");
    }

    /**
     * Prints a String and then terminate the line.
     *
     * @param x The string to be printed.
     */
    @Override
    public void println(String x) {
        print(x + "\n");
    }

    /**
     * Prints an Object and then terminate the line.  This method calls
     * at first String.valueOf(x) to get the printed object's string value,
     * then behaves as though it invokes println(String s).
     *
     * @param x The Object to be printed.
     */
    @Override
    public void println(Object x) {
        String s = String.valueOf(x);
        print(s + "\n");
    }


    /**
     * Prints a boolean value.
     *
     * @param b The boolean to be printed
     */
    @Override
    public void print(boolean b) {
        write(String.valueOf(b));
    }

    /**
     * Prints a character.
     *
     * @param c The char to be printed
     */
    @Override
    public void print(char c) {
        write(String.valueOf(c));
    }

    /**
     * Prints an integer.
     *
     * @param i The int to be printed
     */
    @Override
    public void print(int i) {
        write(String.valueOf(i));
    }

    /**
     * Prints a long integer.
     *
     * @param l The long to be printed
     */
    @Override
    public void print(long l) {
        write(String.valueOf(l));
    }

    /**
     * Prints a floating-point number.
     *
     * @param f The float to be printed
     */
    @Override
    public void print(float f) {
        write(String.valueOf(f));
    }

    /**
     * Prints a double-precision floating-point number.
     *
     * @param d The double to be printed
     */
    @Override
    public void print(double d) {
        write(String.valueOf(d));
    }

    /**
     * Prints an array of characters.
     *
     * @param s The array of chars to be printed
     */
    @Override
    public void print(char[] s) {
        write(String.valueOf(s));
    }

    /**
     * Prints a string.
     *
     * @param s The string to be printed
     */
    @Override
    public void print(String s) {
        if (s == null) {
            s = "null";
        }
        write(s);
    }

    /**
     * Prints an object.
     *
     * @param obj The Object to be printed
     */
    @Override
    public void print(Object obj) {
        write(String.valueOf(obj));
    }

    /**
     * Initialize a file output stream to a given file.
     * Creates the file if it doesn't exist.
     * Does however not create surrounding folders if they don't exist yet.
     *
     * @param fileName the filename to writeAlt to
     * @return the output stream to the file
     */
    private OutputStream initFileOutputStream(String fileName, boolean append) {
        File file = new File(fileName);

        try {
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                return null;
            }
            if (file.exists() || file.createNewFile()) {
                return new FileOutputStream(file, append);
            }
        } catch (IOException e) {
            setError();
        }
        return null;
    }

    /**
     * Start a process bar, which means all printing to
     * any streams is blocked until the process bar is finished.
     * you can stop the process bar by calling 'processBarStop()'.
     *
     * @param size the process bar size in characters.
     */
    public void processBarStart(int size) {
        showingLoadingBar = size;
        getStdOut().print("\u001B[0m"); //reset ansi
        processBarSet(0D);
    }

    /**
     * Stop a process bar that is currently running.
     *
     */
    public void processBarStop() {
        boolean running = checkDoLoadingBar(2);
        showingLoadingBar = -1;
        if (running) {
            getStdOut().println();
        }
    }

    /**
     * Set the process bar to a new value between 0 and 1.
     * If no process bar is active, don't do anything.
     *
     * @param val the value that bar should get.
     */
    public void processBarSet(double val) {
        checkDoLoadingBar(val);
    }

    /**
     * Check if you are doing a loading bar.
     *
     * @param d the new bar value.
     * @return true if it is doing a bar, false if not.
     */
    private boolean checkDoLoadingBar(double d) {
        if (showingLoadingBar <= 0) {
            return false;
        }

        StringBuilder loadingBar = new StringBuilder("\r[");
        for (int i = 1; i <= showingLoadingBar; i++) {
            if ((i / (double) showingLoadingBar) <= d) {
                loadingBar.append('=');
            } else {
                loadingBar.append('.');
            }
        }
        loadingBar.append("] ");
        loadingBar.append(new DecimalFormat("###.##").format(d > 1 ? 100 : (d * 100)));
        loadingBar.append("%     "); //5 spaces to counter all possible different lengths
        //print only to std out
        getStdOut().print(loadingBar.toString());
        return true;
    }

    /**
     * Get the logger stream that has System.out.
     */
    private LoggerOutStream getStdLoggerStream() {
        for (LoggerOutStream loggerOutStream : outputStreams) {
            if (loggerOutStream.stream == getStdOut()) {
                return loggerOutStream;
            }
        }
        return null;
    }

    /**
     * Set Std to use color or not.
     *
     * @param value boolean, true for using color.
     */
    public void setStdColor(boolean value) {
        LoggerOutStream stream = getStdLoggerStream();
        if (stream == null) {
            return;
        }
        stream.supportColor = value;
    }

    /**
     * Set the log level to use on Std Out.
     *
     * @param logLevel the new log level.
     */
    public void setStdLogLevel(LogLevel logLevel) {
        LoggerOutStream stream = getStdLoggerStream();
        if (stream == null) {
            return;
        }
        stream.printLevel = logLevel;
    }

    /**
     * Get the default log level.
     *
     * @return the default Log Level.
     */
    public LogLevel getDefaultLogLevel() {
        return defaultLogLevel;
    }

    /**
     * Set the default log level.
     *
     * @param defaultLogLevel the new default log level.
     */
    public void setDefaultLogLevel(LogLevel defaultLogLevel) {
        this.defaultLogLevel = defaultLogLevel;
    }

    /**
     * Initialize the MultiLogger static instance.
     * (which also captures the System.out stream,
     * so basically calling this starts capturing System.out)
     */
    public static void init() {
        init(true);
    }

    /**
     * Initialize the MultiLogger static instance.
     * The boolean specifies if you want to replace the std out stream with the logger.
     *
     * @param replaceStdOut true to replace the System.out stream.
     */
    public static void init(boolean replaceStdOut) {
        init(replaceStdOut, true, null, null);
    }

    /**
     * Initialize the MultiLogger static instance.
     * (which also captures the System.out stream,
     * so basically calling this starts capturing System.out)
     *
     * @param replaceStdOut true to replace the System.out stream.
     * @param stdOutColor print to StdOut in color.
     * @param toFile      also print to a log file.
     * @param logLevel    level to print to the log file.
     */
    public static void init(boolean replaceStdOut, boolean stdOutColor, String toFile, LogLevel logLevel) {
        synchronized (INIT_LOCK) {
            if (MultiLogger.instance == null) {
                try {
                    MultiLogger.instance = new MultiLogger(stdOutColor, toFile, logLevel, true);
                } catch (UnsupportedEncodingException e) {
                    //never gonna happen though.
                }
            }
            if (System.out != MultiLogger.instance && replaceStdOut) {
                System.setOut(MultiLogger.instance);
            }
        }
    }

    /**
     * Get the MultiLogger instance, or make a new one.
     *
     * @return the MultiLogger.
     */
    public static synchronized MultiLogger get() {
        if (MultiLogger.instance == null) {
            init(false);
        }
        return MultiLogger.instance;
    }

    /**
     * Quits the MultiLogger.
     * Closes all streams, and puts StdOut back.
     * Removes the static instance.
     * Prints conformation to be sure.
     */
    public static void exit() {
        synchronized (INIT_LOCK) {
            if (MultiLogger.instance == null) {
                return;
            }

            MultiLogger.instance.close();
            System.out.println("Closed MultiLogger");
            MultiLogger.instance = null;
        }
    }

    /**
     * Get the original System.out Print stream.
     * In case
     *
     * @return The original System.out
     */
    private PrintStream getStdOut() {
        return (PrintStream) out;
    }

    /**
     * Give some String input a Log Level based on prefix.
     * If no prefix detected, the defaultLogLevel field is used.
     * Otherwise the prefix is removed.
     * The result is the input, without prefix, formatted to the
     * detected Log Level.
     *
     * @param input a given input string.
     * @return the output format Strings and LogLevel.
     */
    private LogOutputPair formatAsLog(String input) {
        LogLevel logLevel;
        if (input.length() < 2) {
            return formatAsLog(input, defaultLogLevel);
        }
        switch (input.substring(0, 2).toLowerCase()) {
            case "!d":
                logLevel = LogLevel.DEBUG;
                break;
            case "!i":
                logLevel = LogLevel.INFO;
                break;
            case "!w":
                logLevel = LogLevel.WARNING;
                break;
            case "!e":
                logLevel = LogLevel.ERROR;
                break;
            case "!o":
                logLevel = LogLevel.OTHER;
                break;
            case "!f":
                logLevel = LogLevel.FATAL;
                break;
            default:
                return formatAsLog(input, defaultLogLevel);
        }
        String newInput = input.substring(2);
        while (newInput.startsWith(" ")) {
            newInput = newInput.substring(1);
        }
        return formatAsLog(newInput, logLevel);
    }

    /**
     * Format some input string to a specific log format.
     * Returns the format in both color and not color mode
     * and the Log Level, all as a LogOutputPair instance.
     *
     * @param input a given input string.
     * @param logLevel the logLevel to format the input to.
     * @return the output format Strings and LogLevel.
     */
    private LogOutputPair formatAsLog(String input, LogLevel logLevel) {
        if (logLevel == LogLevel.NO_LOG) {
            return new LogOutputPair(
                    input, input, logLevel
            );
        }

        StringBuilder output = new StringBuilder(" ");
        //log setup: [level] Time (thread) - input
        String time = LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
        int index = time.indexOf('.') + 3;
        if (index >= time.length()) {
            time = time + "000";
        }
        output.append(time, 0, index);
        output.append(" (");
        output.append(Thread.currentThread().getName());
        output.append(") - ");

        if (input.endsWith("\n")) {
            input = input.substring(0,
                    input.length() - 1);
        }
        output.append(input);

        return new LogOutputPair(
                logLevel.prefix(output.toString()) + "\n",
                logLevel.colorPrefix(output.toString()) + "\n",
                logLevel
        );
    }

    /**
     * Class meant for specifying what to print, with a color
     * and non color field for different color supporting streams.
     * Also contains the LogLevel so that streams can check if they even want
     * to print the given LogOutputPair.
     */
    private static class LogOutputPair {
        private LogLevel logLevel;
        private String nonColor;
        private String color;

        private LogOutputPair(String nonColor, String color, LogLevel logLevel) {
            this.logLevel = logLevel;
            this.nonColor = nonColor;
            this.color = color;
        }
    }

    /**
     * Class for easily writing to any stream by the MultiLogger.
     * Contains easy usable writeAlt, flush and close methods, keeps track
     * of what the stream should print by having a Log Level field, and
     * keeps track if you want to print in color or not.
     */
    private static class LoggerOutStream {
        private OutputStream stream;
        private boolean supportColor;
        private LogLevel printLevel;



        /**
         * Private constructor so that only the MultiLogger can make instances.
         *
         * @param stream       the stream.
         * @param supportColor print in color or not.
         * @param printLevel   print level to print (from).
         */
        private LoggerOutStream(OutputStream stream,
                                boolean supportColor,
                                LogLevel printLevel) {
            super();
            this.supportColor = supportColor;
            this.printLevel = printLevel;
            this.stream = stream;
        }

        /**
         * Checks if this stream should print the specified level.
         * If it does, writeAlt to the stream in either color or non-color,
         * depending on the 'supportColor' flag.
         * Then flushes the stream.
         *
         * Returns true if nothing went wrong.
         * A return false can mean the writeAlt went wrong or the flush went wrong.
         *
         * @param outputPair the output pair to print (if the log level is right).
         * @return true when writeAlt and flush gave no error.
         */
        private boolean write(LogOutputPair outputPair) {
            if (!printLevel.shouldPrint(outputPair.logLevel)) {
                return true;
            }

            try {
                if (this.supportColor) {
                    this.stream.write(outputPair.color.getBytes("UTF-8"));
                } else {
                    this.stream.write(outputPair.nonColor.getBytes("UTF-8"));
                }
            } catch (IOException e) {
                return false;
            }

            return flush();
        }

        /**
         * Returns true on success.
         *
         * @return true when flush gave no error.
         */
        private boolean flush() {
            if (this.stream != null) {
                try {
                    this.stream.flush();
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Close the stream, and set the stream to null.
         * If the stream already is null, do nothing.
         * If any exceptions occur, ignore them.
         */
        private void close() {
            if (this.stream != null) {
                try {
                    this.stream.close();
                    this.stream = null;
                } catch (Exception ignore) {
                }
            }
        }
    }
}
