package general;

import logger.MultiLogger;
import lombok.NonNull;
import lombok.Setter;

import java.io.*;
import java.util.HashMap;

/**
 * This class reads the default config file for the default argument values.
 */
public class DefaultParameters {

    /**
     * The instances that holds the values.
     */
    @Setter
    private static DefaultParameters instance;

    private HashMap<String, HashMap<String, String>> argsMap;

    /**
     * Creates a instance that holds all the parameters stored in the file.
     *
     * @param file The path to the config file.
     */
    protected DefaultParameters(String file) {
        this.argsMap = new HashMap<>();
        this.parseFiles(file);
    }

    /**
     * This method parses the file and stores all the parameters in the map.
     *
     * @param file The path to the config file.
     */
    private void parseFiles(String file) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String line;
            String command = null;
            while ((line = reader.readLine()) != null) {
                //Ignore parameters if no command was yet defined.
                if (line.startsWith("-") && command == null) {
                    continue;
                }

                String trimmed = line.trim();

                String[] args = trimmed.split("\\s");
                //Ignore emptyline & comments
                if (args.length == 0 || trimmed.startsWith("#")) {
                    continue;
                }

                if (trimmed.startsWith("-")) {
                    int index = trimmed.startsWith("--") ? 2 : 1;

                    //Line with no flags found.
                    if (args[0].equals("-") || args[0].equals("--")) {
                        continue;
                    }

                    String value = args.length > 1 ? trimmed.substring(args[0].length() + 1) : null;
                    assert command != null;
                    this.argsMap.get(command.toLowerCase()).put(args[0].substring(index), value);

                } else {
                    command = trimmed;
                    this.argsMap.putIfAbsent(command, new HashMap<>());
                }
            }
            reader.close();
        } catch (java.io.IOException e) {
            //If something is wrong we just assume that there are no default values.
            MultiLogger.get().println(e);
        }
    }

    /**
     * Gets the default parameter.
     * This will return the default parameter of the command and parameters are found.
     * Otherwise it will return the defaultValue.
     *
     * @param command      The command.
     * @param parameter    The parameter you want to find.
     * @param defaultValue The default value in case the parameter does not exists.
     * @return The parameter.
     */
    public String get(@NonNull String command, String parameter, String defaultValue) {
        String string = get(command, parameter);
        return string == null ? defaultValue : string;
    }

    /**
     * Gets the default parameter.
     * This will return the default parameter of the command and parameters are found.
     * Otherwise it will return null.
     *
     * @param command   The command.
     * @param parameter The parameter you want to find.
     * @return The parameter.
     */
    public String get(@NonNull String command, String parameter) {
        final HashMap<String, String> strings = this.argsMap.get(command.toLowerCase());
        return strings != null ? strings.get(parameter) : null;
    }

    /**
     * Checks if the current parameter is contained in the map.
     *
     * @param command   The command.
     * @param parameter The parameter you want to find.
     * @return True if the parameter was found false otherwise.
     */
    public boolean contains(@NonNull String command, String parameter) {
        final HashMap<String, String> strings = this.argsMap.get(command.toLowerCase());
        return strings != null && strings.containsKey(parameter);
    }

    /**
     * Gets the instance that holds the DefaultParameters.
     *
     * @return The instance.
     */
    public static synchronized DefaultParameters getInstance() {
        if (instance == null) {
            instance = new DefaultParameters("./src/main/resources/default-values.config");
        }
        return instance;
    }
}
