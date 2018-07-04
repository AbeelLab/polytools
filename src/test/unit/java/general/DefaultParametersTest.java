package general;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests whether the default paremeters are correctly parsed.
 */
public class DefaultParametersTest {
    private DefaultParameters instance;

    /**
     * Initializes the instance.
     */
    @Before
    public void init() {
        instance = new DefaultParameters("src/test/resources/DefaultParameters/values.config");
        DefaultParameters.setInstance(instance);
        assertThat(DefaultParameters.getInstance() == instance).isTrue();
    }

    /**
     * Test parameters on file that does not exists.
     */
    @Test
    public void testNonExistingFile() {
        try {
            new DefaultParameters("./thisFileDoesNotExistOfCourse");
        } catch (Exception e) {
            fail("Non existing file should not raise exception!");
        }
    }

    /**
     * Test getting the instance.
     */
    @Test
    public void testGetInstance() {
        try {
            DefaultParameters.setInstance(null);
            DefaultParameters.getInstance();
        } catch (Exception e) {
            fail("Getting instance should not raise exception!");
        }
    }

    /**
     * Tests the values of the first command.
     */
    @Test
    public void testGetFirstCommand() {
        assertThat(instance.get("consensus", "region")).isEqualTo("1-100");
        assertThat(instance.get("consensus", "fq")).isEqualTo("0");
    }

    /**
     * Test the values of the second command.
     */
    @Test
    public void testSecondCommand() {
        assertThat(instance.get("test", "something")).isEqualTo("value");
        assertThat(instance.get("test", "something2")).isEqualTo("value2");
    }

    /**
     * Tests that for the command it is case insensitive.
     */
    @Test
    public void testIgnoresCase() {
        assertThat(instance.get("ConSensus", "region")).isEqualTo("1-100");
    }

    /**
     * Test null value when parameter does not exist.
     */
    @Test
    public void testValueNotExists() {
        assertThat(instance.get("consensus", "NA")).isNull();
        assertThat(instance.get("NA", "NA")).isNull();
    }

    /**
     * Test that you get the value or a default value if it not exists.
     */
    @Test
    public void testDefaultValue() {
        assertThat(instance.get("consensus", "region", "default")).isEqualTo("1-100");
        assertThat(instance.get("consensus", "NA", "default")).isEqualTo("default");
    }

    /**
     * Test to see if a parameter is contained in the command.
     */
    @Test
    public void contains() {
        assertThat(instance.contains("test", "noarg")).isTrue();
    }
}
