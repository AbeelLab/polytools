package general;

/**
 * Mostly used for synchronized locks,
 * Since objects need to be final, but you want a boolean.
 */
public class DynamicBoolean {
    private boolean value;

    /**
     * Constructor with initial value.
     * @param value the value.
     */
    public DynamicBoolean(boolean value) {
        this.value = value;
    }

    /**
     * Get boolean value.
     * @return the value.
     */
    public boolean get() {
        return value;
    }

    /**
     * Set boolean value.
     * @param b value to set to.
     */
    public void set(boolean b) {
        this.value = b;
    }
}
