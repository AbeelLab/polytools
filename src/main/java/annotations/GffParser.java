package annotations;

import annotations.AnnotationData.AnnotationDataBuilder;
import general.GZip;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Parses the gff file into a list of Annotation objects.
 * Does not use header information.
 * Currently Target, Gap, Derives_From, Note, Dbxref and Ontology_term are just parsed as a simple string.
 * Created by regiv on 25/05/2018.
 */
public class GffParser {

    /**
     * An array with all the encoded characters.
     * According to the gff3 specifications and the RFC 3986 Percent-Encoding.
     * (https://github.com/The-Sequence-Ontology/Specifications/blob/master/gff3.md)
     */
    //Intellij just does not want to use 8 spaces instead of 12
    @SuppressWarnings("checkstyle:Indentation")
    private static final char[] ENCODED_CHARS = {
            '!', '*', '`', '(', ')', ';', ':', '@', '&', ' ',
            '=', '+', '$', ',', '/', '?', '#', '[', ']', '%',
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x10, 0x0A, 0x0B, 0x0C, 0x0D, 0x0F,
            0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
            0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f,
            0x7f
    };

    private static final String[] ENCODED_STRINGS = new String[ENCODED_CHARS.length];

    static {
        for (int i = 0; i < ENCODED_CHARS.length; i++) {
            int intValue = (int) ENCODED_CHARS[i];
            String stringValue = Integer.toString(intValue, 16);
            String hexadecimal = (stringValue.length() == 1 ? "%0" : "%") + stringValue;
            ENCODED_STRINGS[i] = hexadecimal;
        }
    }

    /**
     * Comparator for Annotations that does primary sorting on region start en secondary sorting on region end.
     */
    private static final class RegionComparator implements Comparator<Annotation>, Serializable {
        @Override
        public int compare(Annotation t0, Annotation t1) {
            int result = t0.getStart() - t1.getStart();
            if (result != 0) {
                return result;
            }
            return t0.getEnd() - t1.getEnd();
        }
    }

    private int lineCounter;

    private File gff;

    /**
     * Creates a new GffParser for a given file.
     *
     * @param gff The GFF file this parser will parse.
     */
    public GffParser(File gff) {
        this.gff = gff;
    }

    /**
     * Parses the gff file into a list of Annotation objects.
     * Does not use header information.
     * Currently Target, Gap, Derives_From, Note, Dbxref and Ontology_term are just parsed as a simple string.
     *
     * @return A list of annotations based on the GFF file.
     * @throws FileNotFoundException If the file does not exist.
     * @throws IOException If something went wrong with reading.
     */
    public List<Annotation> parse() throws IOException {

        lineCounter = 0;
        Scanner reader;

        if (!gff.exists()) {
            throw new FileNotFoundException("Could not find the specified GFF3 file: " + gff.getAbsolutePath());
        }

        InputStream stream = new BufferedInputStream(new FileInputStream(gff));
        if (GZip.isGZipInputStream(stream)) {
            stream = new GZIPInputStream(stream);
        }
        reader = new Scanner(stream, "UTF-8");

        List<Annotation> annotations = new ArrayList<>();
        HashMap<String, Annotation> annotationsByID = new HashMap<>();
        TreeMap<Annotation, List<String>> parents = new TreeMap<>(new RegionComparator());
        while (reader.hasNext()) {
            String line = reader.nextLine();
            lineCounter++;
            if (line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }

            Map.Entry<AnnotationData, List<String>> tuple = parseLine(line);
            Annotation annotation = new Annotation(tuple.getKey());

            if (tuple.getValue() != null) {
                parents.put(annotation, tuple.getValue());
            }

            annotations.add(annotation);
            if (annotation.getData().getId() != null) {
                annotationsByID.put(annotation.getData().getId(), annotation);
            }
        }

        for (Map.Entry<Annotation, List<String>> entry : parents.entrySet()) {
            for (String p : entry.getValue()) {
                Annotation parent = annotationsByID.get(p);

                if (parent == null) {
                    throw new InputMismatchException("Child without parent found. Missing parent: " + p);
                }
                parent.addChild(entry.getKey());
                entry.getKey().addParent(parent);
            }
        }
        reader.close();
        return annotations;
    }

    /**
     * Parses one non-comment line of a GFF3 file.
     *
     * @param line The line to parse.
     * @return An annotation and the seqID's of it's parents.
     */
    Map.Entry<AnnotationData, List<String>> parseLine(String line) {
        AnnotationDataBuilder builder = AnnotationData.builder();
        String[] values = line.split("\t");
        if (values.length != 9) {
            throw new InputMismatchException(
                    values.length + " columns found instead of 9 in the GFF file at line " + lineCounter + ".");
        }

        builder.sequenceID(decode(values[0]));

        if (!values[1].equals(".")) {
            builder.source(decode(values[1]));
        }

        builder.type(values[2]);

        parseRegions(values, builder);
        parseScore(values, builder);
        parseStrandedAndSense(values, builder);
        parsePhase(values, builder);

        List<String> parentsList = parseOptionalParameters(values[8], builder);
        AnnotationData annotationData = builder.build();
        return new AbstractMap.SimpleEntry<>(annotationData, parentsList);
    }

    private void parsePhase(String[] values, AnnotationDataBuilder builder) {
        if (!values[7].equals(".")) {
            if (!values[7].matches("-?\\d+")) {
                throw new InputMismatchException(
                        "Expected an integer for the phase but was: " + values[7] + " at line " + lineCounter);
            }
            builder.phase(Integer.parseInt(values[7]));
        }
    }

    private void parseStrandedAndSense(String[] values, AnnotationDataBuilder builder) {
        switch (values[6]) {
            case "+":
                builder.sense(true).stranded(true);
                break;
            case "-":
                builder.sense(false).stranded(true);
                break;
            case ".":
            case "?":
                builder.stranded(false);
                break;
            default:
                throw new InputMismatchException(
                        "Malformed strand field, was: " + values[6] + " at line " + lineCounter);
        }
    }

    private void parseScore(String[] values, AnnotationDataBuilder builder) {
        if (!values[5].equals(".")) {
            //Check if the string matches a double.
            if (!values[5].matches("^[0-9]+(\\.[0-9]+)?$")) {
                throw new InputMismatchException(
                        "Score expected a double but was: " + values[5] + " at line " + lineCounter);
            }
            builder.score(Double.parseDouble(values[5]));
        }
    }

    private void parseRegions(String[] values, AnnotationDataBuilder builder) {
        if (!values[3].matches("\\d+") || !values[4].matches("\\d+")) {
            throw new InputMismatchException("Start and end expects a positive integer but received: start: "
                    + values[3] + " end: " + values[4] + " at line " + lineCounter);
        }
        int start = Integer.parseInt(values[3]);
        int end = Integer.parseInt(values[4]);

        if (start > end) {
            throw new InputMismatchException(
                    "Start is larger than end: " + start + ">" + end + " at line " + lineCounter);
        }

        builder.start(start).end(end);
    }

    /**
     * Parses the optional parameter field from the GFF file.
     * As the parents field can only be filled in when all annotations are parsed,
     * this method returns a list of strings with this annotation's parent IDs.
     *
     * @param optionalParameters A string containing the contents
     *                           of the optional parameter field from the GFF file (Column 9).
     * @param builder            The annotation prototype to add the optional parameters to.
     * @return A list with the parent IDs that this annotation has.
     */
    private List<String> parseOptionalParameters(String optionalParameters, AnnotationDataBuilder builder) {
        String[] optionalParam = optionalParameters.split(";");
        List<String> parents = null;
        for (String p : optionalParam) {
            String[] parameter = p.split("=");
            if (parameter.length != 2) {
                throw new IllegalArgumentException("malformed tag found at line " + lineCounter);
            }
            switch (parameter[0]) {
                case "ID":
                    builder.id(parameter[1]);
                    break;
                case "Name":
                    builder.name(parameter[1]);
                    break;
                case "Alias":
                    builder.alias(parameter[1]);
                    break;
                case "Parent":
                    String[] parentsArray = parameter[1].split(",");
                    for (int i = 0; i < parentsArray.length; i++) {
                        parentsArray[i] = decode(parentsArray[i]);
                    }
                    parents = Arrays.asList(parentsArray);
                    break;
                case "Target":
                    builder.target(parameter[1]);
                    break;
                case "Gap":
                    builder.gap(parameter[1]);
                    break;
                case "Derives_from":
                    builder.derivesFrom(parameter[1]);
                    break;
                case "Note":
                    builder.note(parameter[1]);
                    break;
                case "Dbxref":
                    builder.dbxref(parameter[1]);
                    break;
                case "Ontology_term":
                    builder.ontologyTerm(parameter[1]);
                    break;
                case "Is_circular":
                    builder.isCircular(Boolean.parseBoolean(parameter[1]));
                    break;
                default:
            }
        }
        return parents;
    }

    /**
     * Decodes a string according to the RFC 3986 encoding.
     *
     * @param string The encoded string.
     * @return The decoded string
     */
    private String decode(String string) {
        String result = string;
        for (int i = 0; i < ENCODED_CHARS.length; i++) {
            char encodedChar = ENCODED_CHARS[i];
            result = result.replaceAll(ENCODED_STRINGS[i], String.valueOf(encodedChar));
        }
        return result;
    }

}
