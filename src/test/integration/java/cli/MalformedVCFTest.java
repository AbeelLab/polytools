package cli;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


/**
 * Created by regiv on 07/06/2018.
 */
@RunWith(JUnitParamsRunner.class)
public class MalformedVCFTest {

    private static final String FILE_ROOT = "src/test/resources/cli/MalformedVCF/";
    private static final String MISSING_FILE_FORMAT_VCF = "MissingFileFormat.vcf";
    private static final String RESTARTING_META_VCF = "RestartingMeta.vcf";
    private static final String DOUBLE_FILE_FORMAT_VCF = "DoubleFileFormat.vcf";
    private static final String HEADER_MISSING_COLUMN_VCF = "HeaderMissingColumn.vcf";
    private static final String HEADER_NOT_DELIMITED_VCF = "HeaderNotTabDelimited.vcf";
    private static final String HEADER_MISSING_FORMAT_COLUMN_VCF = "HeaderMissingFormatColumn.vcf";
    private static final String DOUBLE_HEADER_VCF = "DoubleHeader.vcf";
    private static final String MALFORMED_LINES_VCF = "MalformedLines";

    private static final String STRING_FORMAT = "UTF-8";

    private ByteArrayOutputStream outputStream;
    private PrintStream stdout;

    /**
     * Replace the System.out stream by a stream that we can easily read.
     */
    @Before
    public void init() {
        outputStream = new ByteArrayOutputStream();
        stdout = System.out;
        try {
            PrintStream printStream = new PrintStream(outputStream, false, STRING_FORMAT);
            System.setOut(printStream);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * Closes the outputstream and resets the System.out.
     */
    @After
    public void cleanup() {
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.setOut(stdout);
    }

    private ConsensusCommand makeCommand(String fileName, int start, int end) {
        return new ConsensusCommand("-c " + FILE_ROOT + fileName + " -r " + start + "-" + end);
    }

    /**
     * Creates the test case parameters.
     *
     * @return The parameters of a test case.
     */
    public Object[] parameters() {
        int i = 0;
        return new Object[][]{
                {MISSING_FILE_FORMAT_VCF, 1, 10, false},
                {RESTARTING_META_VCF, 1, 10, false},
                {DOUBLE_FILE_FORMAT_VCF, 1, 10, true},
                {HEADER_MISSING_COLUMN_VCF, 1, 10, false},
                {HEADER_NOT_DELIMITED_VCF, 1, 10, false},
                {HEADER_MISSING_FORMAT_COLUMN_VCF, 1, 10, false},
                {DOUBLE_HEADER_VCF, 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, false},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, false},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true}, //5
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, false},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, false},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, false}, //10
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, false},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, false},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, false}, //15
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, false},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, false},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true}, //20
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true},
                {MALFORMED_LINES_VCF + i++ + ".vcf", 1, 10, true} //25
        };
    }

    /**
     * The actual test to be ran.
     *
     * @param file  The file to use.
     * @param start The start point of the region.
     * @param end   The end point of the region.
     * @param works Whether this test case should break execution.
     */
    @Test
    @Parameters(method = "parameters")
    public void testMalformedVCF(String file, int start, int end, boolean works) {
        try {
            ConsensusCommand cmd = makeCommand(file, start, end);
            int errorCode = cmd.execute();
            if (works) {
                assertThat(errorCode).isEqualTo(0);
            } else {
                assertThat(outputStream.toString(STRING_FORMAT)).containsSequence("VCF");
                assertThat(outputStream.toString(STRING_FORMAT)).containsSequence("malformed");
            }
        } catch (Exception e) {
            fail(e.toString());
        }
    }
}
