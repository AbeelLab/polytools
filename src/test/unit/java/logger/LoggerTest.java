package logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the MultiLogger class.
 */
@SuppressFBWarnings("DM_DEFAULT_ENCODING")
public class LoggerTest {
    private ByteArrayOutputStream byteArrayOutputStream;
    private PrintStream fakeStdOutStream;

    /**
     * Replace the System.out stream by a stream that we can easily read.
     */
    @Before
    public void initFakeStreams() {
        MultiLogger.exit();
        byteArrayOutputStream = new ByteArrayOutputStream(256);
        fakeStdOutStream = new PrintStream(byteArrayOutputStream);
        System.setOut(fakeStdOutStream);
    }

    /**
     * Make a random print stream and return it.
     * @return the randomly made print stream.
     */
    private PrintStream randomPrintStream() {
        ByteArrayOutputStream s = new ByteArrayOutputStream(256);
        return new PrintStream(s) {
            //very cheat way to check if out is null.
            @Override
            public boolean checkError() {
                return out == null;
            }
        };
    }

    /**
     * Init the logger and writeAlt a line.
     * @param s the line to writeAlt.
     */
    private void basicInitAndWriteMultiLogger(String s) {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().println(s);
        MultiLogger.get().flush();
    }

    /**
     * Get the output on the fake System.out stream.
     * @return the current output since replacement.
     */
    private String getOutputOnStream() {
        return new String(byteArrayOutputStream.toByteArray());
    }

    /**
     * Test empty constructor.
     */
    @Test
    public void testConstructorEmpty() {
        MultiLogger.init();
        assertThat(fakeStdOutStream).isNotEqualTo(System.out);
    }

    /**
     * Test constructor that does not replace std out.
     */
    @Test
    public void testConstructorNoReplaceStdOut() {
        MultiLogger.init(false);
        assertThat(fakeStdOutStream).isEqualTo(System.out);
    }

    /**
     * Test std out no color init.
     */
    @Test
    public void testConstructorStdOutNoColor() {
        MultiLogger.init(true, false, null, null);
        MultiLogger.get().println("!e some text here");
        MultiLogger.get().flush();

        assertThat(getOutputOnStream().length() > 0).isTrue();
        assertThat(getOutputOnStream()).doesNotContain("\u001B[33m");
    }

    /**
     * Test print fatal string color and message.
     */
    @Test
    public void testConstructorStdOutColorFatal() {
        basicInitAndWriteMultiLogger("!f someText here");
        assertThat(getOutputOnStream()).isNotEmpty();
        assertThat(getOutputOnStream()).contains("\u001B[31m"); //red
        assertThat(getOutputOnStream()).contains("!!"); //red
    }

    /**
     * Test print error.
     */
    @Test
    public void testConstructorStdOutColorError() {
        basicInitAndWriteMultiLogger("!e someText here");
        assertThat(getOutputOnStream()).isNotEmpty();
        assertThat(getOutputOnStream()).contains("\u001B[31m"); //red
    }

    /**
     * Test print warning.
     */
    @Test
    public void testConstructorStdOutColorWarning() {
        basicInitAndWriteMultiLogger("!w someText here");
        assertThat(getOutputOnStream()).isNotEmpty();
        assertThat(getOutputOnStream()).contains("\u001B[33m"); //yellow
    }

    /**
     * Test print info.
     */
    @Test
    public void testConstructorStdOutColorInfo() {
        basicInitAndWriteMultiLogger("!i some debug stuff here");
        assertThat(getOutputOnStream()).isNotEmpty();
        assertThat(getOutputOnStream()).contains("\u001B[0m"); //debug
    }

    /**
     * Test print debug.
     */
    @Test
    public void testConstructorStdOutColorDebug() {
        basicInitAndWriteMultiLogger("!d some debug stuff here");
        assertThat(getOutputOnStream()).isNotEmpty();
        assertThat(getOutputOnStream()).contains("\u001B[36m"); //debug
    }

    /**
     * Test print other.
     */
    @Test
    public void testConstructorStdOutColorOther() {
        basicInitAndWriteMultiLogger("!o some other stuff here");
        assertThat(getOutputOnStream()).isNotEmpty();
        assertThat(getOutputOnStream()).contains("\u001B[0m"); //reset
    }

    /**
     * Test if close method does not close Std Out.
     */
    @Test
    public void testCloseNotStdOut() {
        basicInitAndWriteMultiLogger("!d some debug stuff here");
        MultiLogger.get().close();
        basicInitAndWriteMultiLogger("!d other stuff here");
        assertThat(getOutputOnStream()).contains("other stuff");
    }

    /**
     * Test the close method.
     */
    @Test
    public void testActualClose() {
        basicInitAndWriteMultiLogger("!d some debug stuff here");
        ByteArrayOutputStream s = new ByteArrayOutputStream(256);
        PrintStream k = new PrintStream(s) {
            //very cheat way to check if out is null.
            @Override
            public boolean checkError() {
                return out == null;
            }
        };
        MultiLogger.get().addOutStream(k, true, LogLevel.OTHER);
        assertThat(k.checkError()).isFalse();
        MultiLogger.get().close();
        assertThat(k.checkError()).isTrue();
    }

    /**
     * Test error on close all streams.
     */
    @Test
    public void testCloseAllThenCheckError() {
        basicInitAndWriteMultiLogger("!d some debug stuff here");
        PrintStream k = randomPrintStream();
        MultiLogger.get().addOutStream(k, true, LogLevel.OTHER);
        assertThat(k.checkError()).isFalse();
        MultiLogger.get().close(); //closed k
        assertThat(MultiLogger.get().checkError()).isFalse();
        fakeStdOutStream.close();
        //true because the checkError tries to do flush.
        assertThat(MultiLogger.get().checkError()).isTrue();
    }

    /**
     * Test no error while closing multiple streams.
     */
    @Test
    public void testCloseActualWhileLoggerMoreStreams() {
        basicInitAndWriteMultiLogger("!d some debug stuff here");
        PrintStream k = randomPrintStream();
        PrintStream l = randomPrintStream();

        MultiLogger.get().addOutStream(k, true, LogLevel.OTHER);
        MultiLogger.get().addOutStream(l, true, LogLevel.OTHER);

        k.close();
        MultiLogger.get().close();
        assertThat(MultiLogger.get().checkError()).isFalse();
    }

    /**
     * Test closing is not recursive.
     */
    @Test
    public void testNoRecursiveClosing() {
        basicInitAndWriteMultiLogger("!d some debug stuff here");

        MultiLogger.get().addOutStream(MultiLogger.get(), true, LogLevel.OTHER);
        MultiLogger.get().close();
        assertThat(MultiLogger.get().checkError()).isFalse();
    }

    /**
     * Test closing and then try to writeAlt.
     */
    @Test
    public void testCloseActualThenWrite() {
        basicInitAndWriteMultiLogger("!d some debug stuff here");
        assertThat(MultiLogger.get().checkError()).isFalse();
        fakeStdOutStream.close();
        //true because all output streams are now closed:
        MultiLogger.get().print("something");
        assertThat(MultiLogger.get().checkError()).isTrue();
    }

    /**
     * Test print double.
     */
    @Test
    public void testPrintDouble() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().print(5.9);
        assertThat(getOutputOnStream()).isEqualTo("5.9");
    }

    /**
     * Test print float.
     */
    @Test
    public void testPrintFloat() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().print((float) 5.9);
        assertThat(getOutputOnStream()).isEqualTo("5.9");
    }

    /**
     * Test print long.
     */
    @Test
    public void testPrintLong() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().print(100000000000000L);
        assertThat(getOutputOnStream()).isEqualTo("100000000000000");
    }

    /**
     * Test print char.
     */
    @Test
    public void testPrintChar() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().print('x');
        assertThat(getOutputOnStream()).isEqualTo("x");
    }

    /**
     * Test writeAlt int.
     */
    @Test
    public void testWriteInt() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().write(5);
        assertThat(getOutputOnStream()).isEqualTo("5");
    }

    /**
     * Test writeAlt byte array.
     */
    @Test
    public void testWriteByteArr1() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().write(new byte[]{64, 65, 68, 69}, 0, 4);
        assertThat(getOutputOnStream()).isEqualTo("@ADE");
    }

    /**
     * Test print line empty.
     */
    @Test
    public void testPrintln() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().println();
        assertThat(getOutputOnStream()).isEqualTo("\n");
    }

    /**
     * Test println boolean.
     */
    @Test
    public void testPrintlnBoolean() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().println(true);
        assertThat(getOutputOnStream()).isEqualTo("true\n");
    }

    /**
     * Test print line character.
     */
    @Test
    public void testPrintlnCharacter() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().println('z');
        assertThat(getOutputOnStream()).isEqualTo("z\n");
    }

    /**
     * Test println integer.
     */
    @Test
    public void testPrintlnInteger() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().println(7);
        assertThat(getOutputOnStream()).isEqualTo("7\n");
    }

    /**
     * Test println log.
     */
    @Test
    public void testPrintlnLong() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().println((long) 7);
        assertThat(getOutputOnStream()).isEqualTo("7\n");
    }

    /**
     * Test println float.
     */
    @Test
    public void testPrintlnFloat() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().println(3F);
        assertThat(getOutputOnStream()).isEqualTo("3.0\n");
    }

    /**
     * Test println double.
     */
    @Test
    public void testPrintlnDouble() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().println(9D);
        assertThat(getOutputOnStream()).isEqualTo("9.0\n");
    }

    /**
     * Test println char array.
     */
    @Test
    public void testPrintlnCharArr() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().println(new char[]{'h', 'e', 'n', 'k'});
        assertThat(getOutputOnStream()).isEqualTo("henk\n");
    }

    /**
     * Test println object.
     */
    @Test
    public void testPrintlnObject() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().println(new Object() {
            @Override
            public String toString() {
                return "some cool object";
            }
        });
        assertThat(getOutputOnStream()).isEqualTo("some cool object\n");
    }

    /**
     * Test print boolean.
     */
    @Test
    public void testPrintBoolean() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().print(true);
        assertThat(getOutputOnStream()).isEqualTo("true");
    }

    /**
     * Test print integer.
     */
    @Test
    public void testPrintInteger() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().print(8);
        assertThat(getOutputOnStream()).isEqualTo("8");
    }

    /**
     * Test print char array.
     */
    @Test
    public void testPrintCharArr() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().print(new char[]{'k', 'a', 'p', 'p', 'a'});
        assertThat(getOutputOnStream()).isEqualTo("kappa");
    }

    /**
     * Test print null.
     */
    @Test
    public void testPrintNull() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().print((String) null);
        assertThat(getOutputOnStream()).isEqualTo("null");
    }

    /**
     * Test print object.
     */
    @Test
    public void testPrintObject() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().print(new Object() {
            @Override
            public String toString() {
                return "lol";
            }
        });
        assertThat(getOutputOnStream()).isEqualTo("lol");
    }

    /**
     * Test start process bar.
     */
    @Test
    public void testStartProcessBar() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().processBarStart(50);
        assertThat(getOutputOnStream()).startsWith("\u001B[0m\r[........");
    }

    /**
     * Test process bar 50%.
     */
    @Test
    public void testStartProcessBarHalf() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().processBarStart(10);
        MultiLogger.get().processBarSet(0.5);
        assertThat(getOutputOnStream().substring(25)).startsWith("\r[=====.....]");
    }

    /**
     * Test process bar done.
     */
    @Test
    public void testStartProcessBarDone1() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().processBarStart(10);
        MultiLogger.get().processBarSet(1);
        assertThat(getOutputOnStream().substring(25)).startsWith("\r[==========]");
    }

    /**
     * Test writeAlt during process bar blocked.
     */
    @Test
    public void testStartProcessBarTestMidBarWrite() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().processBarStart(10);
        MultiLogger.get().println("kappa keepo");
        MultiLogger.get().processBarSet(1);
        assertThat(getOutputOnStream().substring(25)).startsWith("\r[==========]");
    }

    /**
     * Test process bar done by stop method.
     */
    @Test
    public void testStartProcessBarDone2() {
        MultiLogger.init(true, true, null, null);
        MultiLogger.get().processBarStart(10);
        MultiLogger.get().processBarStop();
        assertThat(getOutputOnStream().substring(25)).startsWith("\r[==========]");
    }

    /**
     * Test process bar done, then writing is unblocked.
     */
    @Test
    public void testStartProcessBarDoneThenWrite() {
        MultiLogger.init(true, false, null, null);
        MultiLogger.get().processBarStart(10);
        MultiLogger.get().processBarStop();
        MultiLogger.get().println("lol");
        assertThat(getOutputOnStream().substring(40)).contains("lol\n");
    }

    /**
     * Test change std out color.
     */
    @Test
    public void testStdOutFromNoColorToColor() {
        MultiLogger.init(true, false, null, null);
        MultiLogger.get().println("!d some text here");
        MultiLogger.get().flush();

        assertThat(getOutputOnStream().length() > 0).isTrue();
        assertThat(getOutputOnStream()).doesNotContain("\u001B[33m");

        MultiLogger.get().setStdColor(true);
        MultiLogger.get().println("!d some other text here");

        assertThat(getOutputOnStream()).contains("\u001B[36m");
    }

    /**
     * Get default log level test.
     */
    @Test
    public void getDefaultLogLevel() {
        MultiLogger.init(true, false, null, null);
        MultiLogger.get().println("!e some text here");
        MultiLogger.get().flush();
        assertThat(MultiLogger.get().getDefaultLogLevel()).isEqualTo(LogLevel.NO_LOG);
    }

    /**
     * Test set default log level.
     */
    @Test
    public void setDefaultLogLevel() {
        MultiLogger.init(true, false, null, null);
        MultiLogger.get().println("!e some text here");
        MultiLogger.get().flush();
        MultiLogger.get().setDefaultLogLevel(LogLevel.OTHER);
        assertThat(MultiLogger.get().getDefaultLogLevel()).isEqualTo(LogLevel.OTHER);
    }

    /**
     * Test get logger without init is not null.
     */
    @Test
    public void getLoggerWithoutInstance() {
        assertThat(MultiLogger.get()).isNotNull();
    }

    /**
     * Set the std out log level, test nothing below it is printed.
     */
    @Test
    public void setStdLogLevel() {
        MultiLogger.init(true, false, null, null);
        MultiLogger.get().setStdLogLevel(LogLevel.FATAL);
        MultiLogger.get().println("!e some text here");
        MultiLogger.get().flush();
        assertThat(getOutputOnStream()).isEmpty();
    }
}
