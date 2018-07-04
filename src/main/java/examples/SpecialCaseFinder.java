package examples;

import htsjdk.variant.variantcontext.VariantContext;
import logger.MultiLogger;
import vcf.iterator.VCFIteratorBuilder;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class which tries to find special cases in vcf files.
 */
public class SpecialCaseFinder {
    private static final String DEFAULT_CASE_FOLDER = "./examples/cases/";
    private static final File TEMP_CASE_FILE = new File(DEFAULT_CASE_FOLDER, "temp.case");

    private SpecialCaseFinder() {
    }

    /**
     * Get cases from case files.
     *
     * @return the cases to run.
     */
    static LinkedList<ExampleTest> getCases() {
        LinkedList<ExampleTest> exampleTests = new LinkedList<>();
        File folder = new File(DEFAULT_CASE_FOLDER);
        if (!folder.exists()) {
            return exampleTests;
        }
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return exampleTests;
        }
        for (File f : files) {
            exampleTests.add(new SpecialCaseFoundTest(f.getName(),
                    "AUTO GENERATED", f.getAbsolutePath()));
        }
        return exampleTests;
    }

    private static void runCasesCmd() {
        LinkedList<ExampleTest> cases = getCases();
        Iterator<ExampleTest> it = cases.iterator();
        int totalCases = cases.size();
        int failedCases = 0;
        int ranCases = 0;


        System.out.println("Found " + totalCases + " hand checked test cases.");
        System.out.println("Running all test cases:");
        for (int r = 0; r < totalCases; r++) {
            ExampleTest test = it.next();
            boolean passes = test.getRunOutput().replace("\r", "")
                    .replace("\n", "").endsWith(test.getExpectedOutput()
                    .replace("\r", "").replace("\n", ""));
            if (!passes) {
                failedCases++;
            }
            ranCases++;

            System.out.print("\r[");
            for (int i = 0; i < totalCases; i += 5) {
                if ((i - 1) < ranCases) {
                    System.out.print("=");
                } else {
                    System.out.print(".");
                }
            }
            System.out.print("] " + (ranCases - failedCases) + " passed.");
        }
        System.out.println();
        System.out.println("Finished with " + failedCases + " failed cases!");
    }

    /**
     * Main method, to run generating case files.
     *
     * @param args the arguments, which is at least a vcf file.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a vcf file name to find test cases.");
            return;
        }
        String firstArg = args[0];
        if (firstArg.equalsIgnoreCase("run")) {
            runCasesCmd();
            return;
        }
        int need = 100;
        if (args.length > 1) {
            try {
                need = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignore) {
            }
        }
        int skip = 0;
        if (args.length > 2) {
            try {
                skip = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignore) {
            }
        }

        try {
            runCommandLine(firstArg, need, skip);
        } catch (IOException e) {
            System.out.println("IOException while generating cases. See logs for more information.");
            MultiLogger.get().println(e.getMessage());
        }
    }

    /**
     * Run creating examples on the command line.
     * This shows the case on the command line, and you can type an answer.
     *
     * @param fileName     the file name to run on.
     * @param requiredNeed the required need level.
     * @throws IOException in case some stuff failed.
     */
    private static void runCommandLine(String fileName, int requiredNeed, int skipLines) throws IOException {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        makeCases(fileName, requiredNeed, (c) -> {
            System.out.println(c.printLines());
            String result = c.runCase(TEMP_CASE_FILE).trim();
            result = result.substring(result.lastIndexOf("|") + 1).trim();
            System.out.println("We found output: " + result);
            System.out.print("Type wanted output or type 'CORRECT' to store this output.\n$");
            String typed = scanner.nextLine();
            if (typed.equalsIgnoreCase("correct")) {
                return result;
            }
            return typed;
        }, skipLines);
    }

    /**
     * Run making cases.
     * Get the cases from some file, only if they have at least the required need level.
     * Get the 'result' of each case from the getResult method.
     *
     * @param fileName     the file name to read from.
     * @param requiredNeed the amount of 'need' needed.
     * @param getResult    the function to generate the result.
     * @throws IOException in case reading or writing failed.
     */
    private static void makeCases(String fileName, int requiredNeed,
                                  StringFunction getResult, int skipLines) throws IOException {
        File inputFile = new File(fileName);
        if (!inputFile.isFile()) {
            System.out.println("Path is not a file or did not exist!");
            return;
        }
        BufferedWriter writer = null;

        try (
                VCFIteratorBuilder.VCFReaderIterator vcfIterator = (VCFIteratorBuilder.VCFReaderIterator)
                        new VCFIteratorBuilder().open(new BufferedInputStream(new FileInputStream(inputFile)))
        ) {
            AtomicInteger caseFileId = new AtomicInteger(1);
            Case currentCase = new Case(inputFile, null);
            while (vcfIterator.hasNext()) {
                if (skipLines > 0) {
                    vcfIterator.next();
                    skipLines--;
                    continue;
                }

                currentCase.addNeedAndLine(vcfIterator.next(), vcfIterator.getLastReturnedLine());
                if (currentCase.timeout()) {
                    if (currentCase.needLevel > requiredNeed) {
                        System.out.println("Writing case with need level: "
                                + currentCase.needLevel);

                        BufferedWriter tempW = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(TEMP_CASE_FILE), "UTF-8"));
                        tempW.write("##fileformat=VCFv4.1\n");
                        tempW.write("##contig=<ID=x,length=200>" + "\n");
                        currentCase.write(tempW, null);
                        tempW.close();

                        String result = getResult.get(currentCase);
                        writer = nextCaseWriter(caseFileId);
                        if (writer == null) {
                            return;
                        }
                        currentCase.write(writer, result);
                        writer.close();
                    }
                    currentCase = new Case(inputFile, currentCase.lastLines());
                }
            }
        } catch (IOException e) {
            System.out.println("Exception reading inputPath!");
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
            TEMP_CASE_FILE.deleteOnExit();
        }
    }

    /**
     * Get a writer to write to the next case file.
     * Checks file from some known id.
     * <p>
     * Returns null in case there was no file anymore.
     *
     * @param from from file id.
     * @return the new buffered writer.
     */
    private static BufferedWriter nextCaseWriter(AtomicInteger from) {
        File file = getNextAutoCaseFile(from);
        System.out.println(file);
        if (file == null) {
            return null;
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), "UTF-8"));
            bufferedWriter.write("##fileformat=VCFv4.1\n");
            bufferedWriter.write("##contig=<ID=x,length=200>" + "\n");
            return bufferedWriter;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Get the next file to write to.
     * Count from an atomic integer value.
     * <p>
     * Return null if we have more than 10000 files.
     *
     * @return the file.
     */
    private static File getNextAutoCaseFile(AtomicInteger from) {
        File folder = new File(DEFAULT_CASE_FOLDER);
        if (!folder.exists() && !folder.mkdirs()) {
            MultiLogger.get().println("Could not find or make case folder!");
            return null;
        }
        File file = null;
        for (int i = from.get(); i < 10000; i++) {
            file = new File(folder, "caseFile" + i + ".vcf");
            if (!file.exists()) {
                from.set(i);
                break;
            }
        }

        if (file.exists()) {
            return null;
        }
        return file;
    }

    /**
     * The string function, used for getting the result.
     * This allows for having multiple ways to generate cases.
     */
    @FunctionalInterface
    interface StringFunction {
        /**
         * Get a string from some case.
         *
         * @param toShow the case to show.
         * @return the result string.
         */
        String get(Case toShow);
    }

    /**
     * Private class for storing the current case.
     * This is the case that we are currently reading and rating.
     */
    private static class Case {
        private final LinkedList<String> lines;

        private File file;
        private int firstIndex;
        private int timeOutLines;
        private int needLevel;

        private Case(File file, LinkedList<String> before) {
            this.file = file;
            this.lines = new LinkedList<>();
            if (before != null && !before.isEmpty()) {
                int start = getStartFromText(before.getFirst());
                if (start != -1) {
                    this.firstIndex = start;
                    lines.addAll(before);
                }
            }
            this.timeOutLines = 5;
            this.needLevel = 0;
        }

        private int getStartFromText(String line) {
            int firstTab = line.indexOf('\t');
            int secondTab = line.indexOf('\t', firstTab + 1);
            if (secondTab == -1) {
                return -1;
            }
            String num = line.substring(firstTab + 1, secondTab);
            try {
                return Integer.parseInt(num);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        private void addNeedAndLine(VariantContext context, String line) {
            if (lines.isEmpty()) {
                firstIndex = context.getStart();
            }


            int need = 0;

            //this method can be changed to get
            //specific or rare or other cases.

            int refLen = context.getReference().length();

            if (refLen > 1) {
                need += 35; //deletion
            }

            if (context.getReference().length() > 25
                    || (context.getAlternateAlleles().size() > 0
                    && context.getAlternateAlleles().get(0).length() > 25)) {
                need -= 150;
            }

            if (context.getAlternateAlleles().size() > 1) {
                //rare, always store this!
                need += 300;
            }

            if (context.getAlternateAlleles().size() > 0
                    && context.getAlternateAllele(0).length() > 1) {
                need += 35; //insertion
            }

            if (context.getAlternateAlleles().size() > 0
                    && context.getAlternateAllele(0).length() == refLen) {
                //variation of same length
                if (refLen == 1) {
                    //boring...
                    need += 10;
                } else {
                    need += 35;
                }
            }

            addLine(line, need);
        }

        private void addLine(String line, int need) {
            if (need > 0) {
                needLevel += need;
                timeOutLines = 5;
            } else {
                timeOutLines--;
            }
            lines.add(line);
        }

        private String toSmallIndexLine(String toWrite) {
            //NAME \t INDEX \t
//            return toWrite;
            int firstTab = toWrite.indexOf('\t');
            int secondTab = toWrite.indexOf('\t', firstTab + 1);
            if (secondTab == -1) {
                return null;
            }
            int start = getStartFromText(toWrite);
            return "x\t" + (start - firstIndex + 1) + toWrite.substring(secondTab);
        }

        private boolean timeout() {
            return timeOutLines <= 0 || lines.size() > 30;
        }

        private void write(BufferedWriter writer, String result) throws IOException {
            if (result != null && result.startsWith("!")) {
                writer.write("##/RESULT=" + result.substring(1));
            } else {
                writer.write("##RESULT=" + result);
            }
            writer.write("\n#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tSAMPLE\n");
            for (String s : lines) {
                String ns = toSmallIndexLine(s);
                if (ns == null) {
                    writer.write("ERROR, COULD NOT WRITER FURTHER");
                    return;
                }
                writer.write(ns);
                writer.write("\n");
            }
        }

        private String runCase(File file) {
            if (file == null) {
                file = this.file;
            }
            String run = ExampleUtil.getRunOutput(new String[]{"consensus", "-c", file.getAbsolutePath()});
            if (run == null) {
                return null;
            }
            int lastNotDotIndex = run.length() - 1;
            while (run.charAt(lastNotDotIndex) == '.'
                    || run.charAt(lastNotDotIndex) == '\n'
                    || run.charAt(lastNotDotIndex) == '\r') {
                lastNotDotIndex--;
            }
            return run.substring(0, lastNotDotIndex + 1);
        }

        private String printLines() {
            StringBuilder builder = new StringBuilder();
            for (String s : lines) {
                builder.append(s);
                builder.append("\n");
            }
            return builder.toString();
        }

        private LinkedList<String> lastLines() {
            LinkedList<String> strings = new LinkedList<>();
            for (int i = 0; i < 4 && !lines.isEmpty(); i++) {
                strings.addFirst(lines.removeLast());
            }
            return strings;
        }
    }
}
