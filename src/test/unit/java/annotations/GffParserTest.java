package annotations;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

/**
 * Tests for the GFF parser.
 * Created by regiv on 25/05/2018.
 */
public class GffParserTest {

    private static final String GFF_FILE = "src/test/resources/annotations/sampleAnnotations.gff3";
    private static final String GFF_FILE_ZIPPED = "src/test/resources/annotations/sampleAnnotations.gff3.gz";
    private static final String MALFORED_GFF_1 = "src/test/resources/annotations/malformedAnnotations.gff3";
    private static final String EMPTY_GFF = "src/test/resources/annotations/emptyAnnotations.gff3";
    private static final String NO_PARENT_GFF = "src/test/resources/annotations/noParentAnnotations.gff3";
    private static final String PARENT_GFF = "src/test/resources/annotations/parentAnnotations.gff3";
    private static final String OPTIONAL_GFF3 = "src/test/resources/annotations/optionalParameters.gff3";


    /**
     * Test the optional parameters.
     */
    @Test
    public void testOptionalParameters() {
        File gff = new File(OPTIONAL_GFF3);
        try {
            final List<Annotation> annotations = new GffParser(gff).parse();
            Assertions.assertThat(annotations).anyMatch(a -> Objects.equals(a.getData().getAlias(), "mrSuper"));
            Assertions.assertThat(annotations).anyMatch(a -> Objects.equals(a.getData().getTarget(), "Aquired"));
            Assertions.assertThat(annotations).anyMatch(a -> Objects.equals(a.getData().getGap(), "D"));
            Assertions.assertThat(annotations).anyMatch(a -> Objects.equals(a.getData().getDerivesFrom(), "operon001"));
            Assertions.assertThat(annotations).anyMatch(a -> Objects.equals(a.getData().getNote(), "This is fun"));
            Assertions.assertThat(annotations).anyMatch(a -> Objects.equals(a.getData().getDbxref(), "mrna0001"));
            Assertions.assertThat(annotations).anyMatch(a -> Objects.equals(a.getData().getOntologyTerm(), "test"));
//            Assertions.assertThat(annotations).anyMatch(Annotation::isCircular);
        } catch (Exception e) {
            Assertions.fail("Exception occured", e);
        }
    }

    /**
     * Checks if the gffparser does not throw an exception on proper data.
     */
    @Test
    public void testParseNoException() {
        File gff = new File(GFF_FILE);
        try {
            new GffParser(gff).parse();
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Checks if the gffParser does not throw an execption on a g-zipped file.
     * Check if the first annotation is present.
     */
    @Test
    public void testParseZipped() {
        File gff = new File(GFF_FILE_ZIPPED);
        try {
            final List<Annotation> parse = new GffParser(gff).parse();
            Assertions.assertThat(parse).anyMatch(annotation -> annotation.getStart() == 5
                    && annotation.getEnd() == 15
                    && Objects.equals(annotation.getName(), "superOperon"));
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Tests if the parser throws a correct exception when no file is found.
     */
    @Test
    public void testNoGffFound() {
        File gff = new File("bla");

        try {
            new GffParser(gff).parse();
            fail();
        } catch (FileNotFoundException e) {
            Assertions.assertThat(e.getMessage()).startsWith("Could not find the specified GFF3 file:");
        } catch (IOException e) {
            fail("IoExecption occured");
        }
    }


    /**
     * Tests if the parser throws a correct exception when a parent is missing from the file.
     */
    @Test
    public void testNoParentFound() {
        File gff = new File(NO_PARENT_GFF);
        try {
            new GffParser(gff).parse();
            fail();
        } catch (InputMismatchException e) {
            assertEquals("Child without parent found. Missing parent: mrna0001", e.getMessage());
        } catch (FileNotFoundException e) {
            fail();
        } catch (IOException e) {
            fail("IOException occured");
        }
    }

    /**
     * Tests if the parser displays the correct line number when the file is not properly tab delimited.
     */
    @Test
    public void testLineCounter() {
        File gff = new File(MALFORED_GFF_1);
        try {
            new GffParser(gff).parse();
            fail();
        } catch (InputMismatchException e) {
            assertEquals("7 columns found instead of 9 in the GFF file at line 3.", e.getMessage());
        } catch (FileNotFoundException e) {
            fail();
        } catch (IOException e) {
            fail("IOExecpted occured");
        }
    }

    /**
     * Tests if the parser throws a correct exception when the file does not have 9 columns.
     */
    @Test
    public void testNotEnoughColumns() {
        File gff = new File(MALFORED_GFF_1);
        try {
            new GffParser(gff).parseLine("ctg123  .\toperon\t5   15\t.\t+\t.\tID=operon001;Name=superOperon");
            fail();
        } catch (InputMismatchException e) {
            assertEquals("7 columns found instead of 9 in the GFF file at line 0.", e.getMessage());
        }
    }

    /**
     * Tests if empty annotation files are accepted.
     */
    @Test
    public void testEmptyAnnotations() {
        File gff = new File(EMPTY_GFF);
        try {
            new GffParser(gff).parse();
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Tests if not giving a source gives a null value in that field.
     */
    @Test
    public void testSourceNull() {
        File gff = new File("");
        try {
            AnnotationData annotation = new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t15\t.\t+\t.\tID=operon001;Name=superOperon")
                    .getKey();
            assertEquals(null, annotation.getSource());
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    /**
     * Tests if a malformed start position gives an exception.
     */
    @Test
    public void testStartMalformed() {
        File gff = new File("");
        try {
            new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5.0\t15\t.\t+\t.\tID=operon001;Name=superOperon")
                    .getKey();
            fail();
        } catch (InputMismatchException e) {
            assertEquals("Start and end expects a positive integer "
                    + "but received: start: 5.0 end: 15 at line 0", e.getMessage());
        }
    }

    /**
     * Tests if a malformed end position gives an exception.
     */
    @Test
    public void testEndMalformed() {
        File gff = new File("");
        try {
            new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\ta\t.\t+\t.\tID=operon001;Name=superOperon")
                    .getKey();
            fail();
        } catch (InputMismatchException e) {
            assertEquals("Start and end expects a positive integer "
                    + "but received: start: 5 end: a at line 0", e.getMessage());
        }
    }

    /**
     * Tests if a start that is larger than end gives an exception.
     */
    @Test
    public void testStartLargerThanEnd() {
        File gff = new File("");
        try {
            new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t10\t5\t.\t+\t.\tID=operon001;Name=superOperon")
                    .getKey();
            fail();
        } catch (InputMismatchException e) {
            assertEquals("Start is larger than end: 10>5 at line 0", e.getMessage());
        }
    }

    /**
     * Test if the score gets set to -1 correctly when it is ".".
     */
    @Test
    public void testScoreNull() {
        File gff = new File("");
        try {
            AnnotationData annotation = new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t.\t+\t.\tID=operon001;Name=superOperon")
                    .getKey();
            assertEquals(-1, annotation.getScore(), 0);
        } catch (InputMismatchException e) {
            fail();
        }
    }

    /**
     * Tests if the correct exception is thrown when score is malformed.
     */
    @Test
    public void testScoreMalformed() {
        File gff = new File("");
        try {
            new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\ta\t+\t.\tID=operon001;Name=superOperon")
                    .getKey();
            fail();
        } catch (InputMismatchException e) {
            assertEquals("Score expected a double but was: a at line 0", e.getMessage());
        }
    }

    /**
     * Test if the phase gets set to 0 correctly when it is ".".
     */
    @Test
    public void testPhaseNull() {
        File gff = new File("");
        try {
            AnnotationData annotation = new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t.\t+\t.\tID=operon001;Name=superOperon")
                    .getKey();
            assertEquals(0, annotation.getPhase());
        } catch (InputMismatchException e) {
            fail();
        }
    }

    /**
     * Tests if the correct exception is thrown when phase is malformed.
     */
    @Test
    public void testPhaseMalformed() {
        File gff = new File("");
        try {
            new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t7\t+\t5.0\tID=operon001;Name=superOperon")
                    .getKey();
            fail();
        } catch (InputMismatchException e) {
            assertEquals("Expected an integer for the phase but was: 5.0 at line 0", e.getMessage());
        }
    }

    /**
     * Happy path line parse test.
     */
    @Test
    public void testSimpleLineParse() {
        File gff = new File("");
        try {
            AnnotationData annotation = new GffParser(gff).parseLine(
                    "ctg123\tREADER\toperon\t5\t10\t5\t+\t0\tID=operon001;Name=superOperon")
                    .getKey();
            assertEquals("ctg123", annotation.getSequenceID());
            assertEquals("READER", annotation.getSource());
            assertEquals("operon", annotation.getType());
            assertEquals(5, annotation.getStart());
            assertEquals(10, annotation.getEnd());
            assertEquals(5.0, annotation.getScore(), 0);
            assertEquals(true, annotation.isSense());
            assertEquals(true, annotation.isStranded());
            assertEquals(0, annotation.getPhase());
            assertEquals("operon001", annotation.getId());
            assertEquals("superOperon", annotation.getName());
        } catch (InputMismatchException e) {
            fail();
        }
    }

    /**
     * Tests if the positive value for the strand field is supported correctly.
     */
    @Test
    public void testStrandPositive() {
        File gff = new File("");
        try {
            AnnotationData annotation = new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t.\t+\t.\tID=operon001;Name=superOperon")
                    .getKey();
            assertEquals(true, annotation.isStranded());
            assertEquals(true, annotation.isSense());
        } catch (InputMismatchException e) {
            fail();
        }
    }

    /**
     * Tests if the negative value for the strand field is supported correctly.
     */
    @Test
    public void testStrandNegative() {
        File gff = new File("");
        try {
            AnnotationData annotation = new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t.\t-\t.\tID=operon001;Name=superOperon")
                    .getKey();
            assertEquals(true, annotation.isStranded());
            assertEquals(false, annotation.isSense());
        } catch (InputMismatchException e) {
            fail();
        }
    }

    /**
     * Tests if the unknown value for the strand field is supported correctly.
     */
    @Test
    public void testStrandUnknown() {
        File gff = new File("");
        try {
            AnnotationData annotation = new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t.\t.\t.\tID=operon001;Name=superOperon")
                    .getKey();
            assertEquals(false, annotation.isStranded());
            assertEquals(true, annotation.isSense());
        } catch (InputMismatchException e) {
            fail();
        }
    }

    /**
     * Tests if the unstranded value for strand field is supported.
     */
    @Test
    public void testStrandUnstranded() {
        File gff = new File("");
        try {
            AnnotationData annotation = new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t.\t?\t.\tID=operon001;Name=superOperon")
                    .getKey();
            assertEquals(false, annotation.isStranded());
            assertEquals(true, annotation.isSense());
        } catch (InputMismatchException e) {
            fail();
        }
    }

    /**
     * tests if a malformed strand field throws the correct exception.
     */
    @Test
    public void testStrandMalformed() {
        File gff = new File("");
        try {
            new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t.\tBanana\t.\tID=operon001;Name=superOperon")
                    .getKey();
            fail();
        } catch (InputMismatchException e) {
            assertEquals("Malformed strand field, was: Banana at line 0", e.getMessage());

        }
    }

    /**
     * tests if an unknown tag is parsed without exception.
     */
    @Test
    public void testUnknownTag() {
        File gff = new File("");
        try {
            new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t.\t?\t.\tID=operon001;Name=superOperon;Canary=2")
                    .getKey();
        } catch (InputMismatchException e) {
            fail();
        }
    }

    /**
     * Tests if the parents tag gets parsed in to the correct list.
     */
    @Test
    public void testParentsTag() {
        File gff = new File("");
        try {
            List<String> parents = new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t.\t?\t.\tID=operon001;Name=superOperon;Parent=1,2,a,b")
                    .getValue();
            assertEquals(Arrays.asList(new String[]{"1", "2", "a", "b"}), parents);
        } catch (InputMismatchException e) {
            fail();
        }
    }

    /**
     * Tests if a tag without a value gives the correct exception.
     */
    @Test
    public void testEmptyTag() {
        File gff = new File("");
        try {
            new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t.\t?\t.\tID=operon001;Name=superOperon;Parent=")
                    .getValue();
        } catch (IllegalArgumentException e) {
            assertEquals("malformed tag found at line 0", e.getMessage());
        }
    }

    /**
     * Tests if multiple unescaped = in a tag gives the correct exception.
     */
    @Test
    public void testOverfilledTag() {
        File gff = new File("");
        try {
            new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t.\t?\t.\tID=operon001;Name=superOperon;hello=a=b")
                    .getValue();
        } catch (IllegalArgumentException e) {
            assertEquals("malformed tag found at line 0", e.getMessage());
        }
    }

    /**
     * tests if no parent tag gives a null value.
     */
    @Test
    public void testNoParentsTag() {
        File gff = new File("");
        try {
            List<String> parents = new GffParser(gff).parseLine(
                    "ctg123\t.\toperon\t5\t10\t.\t?\t.\tID=operon001;Name=superOperon")
                    .getValue();
            assertEquals(null, parents);
        } catch (InputMismatchException e) {
            fail();
        }
    }

    /**
     * Tests if inheritance works correctly.
     */
    @Test
    public void testParents() {
        File gff = new File(PARENT_GFF);
        boolean found = false;
        try {
            List<Annotation> annotations = new GffParser(gff).parse();
            for (Annotation father : annotations) {
                if (father.getName() != null && father.getName().equals("superOperon")) {
                    found = true;
                    List<Annotation> children = father.getChildren();
                    for (Annotation child : children) {
                        assertEquals(father, child.getParents().get(0));
                        assertEquals(1, child.getParents().size());
                    }
                }
            }
        } catch (Exception e) {
            fail();
        }
        if (!found) {
            fail();
        }
    }

    /**
     * Tests if deeper inheritance still functions correctly.
     */
    @Test
    public void testGrandFather() {
        File gff = new File(PARENT_GFF);
        boolean found = false;
        try {
            List<Annotation> annotations = new GffParser(gff).parse();
            for (Annotation grandFather : annotations) {
                if (grandFather.getName() != null && grandFather.getName().equals("superOperon")) {
                    for (Annotation father : grandFather.getChildren()) {
                        if (father.getName() != null && father.getName().equals("sonichedgehog")) {
                            List<Annotation> children = father.getChildren();
                            assertEquals(2, children.size());
                            for (Annotation child : children) {
                                assertThat(child.getParents(), hasItem(father));
                            }
                            found = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            fail();
        }

        if (!found) {
            fail();
        }
    }

    /**
     * Tests if multiple inheritance gets processed correctly.
     */
    @Test
    public void multipleInheritance() {
        File gff = new File(PARENT_GFF);
        boolean found = false;
        try {
            List<Annotation> annotations = new GffParser(gff).parse();
            for (Annotation grandFather : annotations) {
                if (grandFather.getName() != null && grandFather.getName().equals("superOperon")) {
                    for (Annotation father : grandFather.getChildren()) {
                        if (father.getName() != null && father.getName().equals("sonichedgehog")) {
                            List<Annotation> children = father.getChildren();
                            for (Annotation child : children) {
                                if (child.getParents().size() == 2) {
                                    found = true;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            fail();
        }

        if (!found) {
            fail();
        }
    }
}