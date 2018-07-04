package examples;

import java.util.*;

/**
 * Class defining examples, example variables and main method.
 */
public final class Examples {
    static final Map<String, String> VARIABLE_MAP = new HashMap<>();

    private static void fillVariables() {
        if (!VARIABLE_MAP.isEmpty()) {
            return;
        }

        VARIABLE_MAP.put("Reference_Genome_TB", "FILE//examples/Reference_Genome_TB.fasta");
        VARIABLE_MAP.put("Variant_Calls_TB", "FILE//examples/Variant_Calls_TB.vcf");
        VARIABLE_MAP.put("Small_Indels", "FILE//examples/Small_Indels.vcf");
        VARIABLE_MAP.put("GFF3_Annotations", "FILE//examples/GFF3_Annotations.gff3");
        VARIABLE_MAP.put("PYLON_1", "FILE//examples/Pylon_1.vcf");
    }

    /**
     * Examples main method.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        String line = String.join(" ", args);
        fillVariables();


        List<ExampleTest> functionalityExamples = new LinkedList<>();
        addTestsShowingOptions(functionalityExamples);
        List<ExampleTest> correctnessExamples = new LinkedList<>();
        addTestsShowingCorrectness(correctnessExamples);

        for (ExampleTest t : functionalityExamples) {
            if (t.getTestName().equalsIgnoreCase(line)) {
                System.out.println(t.exampleOutput());
                return;
            }
        }
        for (ExampleTest t : correctnessExamples) {
            if (t.getTestName().equalsIgnoreCase(line)) {
                System.out.println(t.exampleOutput());
                return;
            }
        }

        //no command found!
        System.out.println("Defined examples showing options are:");
        for (ExampleTest t : functionalityExamples) {
            System.out.println("\t- " + t.getTestName());
        }
        System.out.println("Defined examples showing correctness are:");
        for (ExampleTest t : correctnessExamples) {
            System.out.println("\t- " + t.getTestName());
        }


        System.out.println("Use \"examples <example name>\" to run an example.");
    }

    /**
     * Private constructor.
     */
    private Examples() {
        //never called
    }

    /**
     * Create example tests.
     *
     * @return the list of tests.
     */
    static List<ExampleTest> createTests() {
        fillVariables();
        List<ExampleTest> testList = new LinkedList<>();
        addTestsShowingOptions(testList);
        addTestsShowingCorrectness(testList);
        return testList;
    }

    private static void addTestsShowingOptions(List<ExampleTest> list) {
        list.add(new ExampleTest("Color Output 1",
                "This example shows how to set a specific encoder.",
                "consensus -col 1 -c $Variant_Calls_TB -f $Reference_Genome_TB -r 103765-103785",
                ">gi|561108321|ref|NC_018143.2|103765-103785|\nGTGTTGGTCCACGGTTGGCCG"));
        list.add(new ExampleTest("Color Output 2",
                "This example shows how to set a specific encoder.",
                "consensus -col 2 -c $Variant_Calls_TB -f $Reference_Genome_TB -r 10-30",
                ">gi|561108321|ref|NC_018143.2|10-30|\n"
                        + "\u001B[36mG\u001B[0m\u001B[35mA\u001B[0m\u001B[32mC\u001B[0m\u001B[32mC\u001B[0m"
                        + "\u001B[32mC\u001B[0m\u001B[32mC\u001B[0m\u001B[36mG\u001B[0m\u001B[36mG\u001B[0m"
                        + "\u001B[31mT\u001B[0m\u001B[31mT\u001B[0m\u001B[32mC\u001B[0m\u001B[35mA\u001B[0m"
                        + "\u001B[36mG\u001B[0m\u001B[36mG\u001B[0m\u001B[32mC\u001B[0m\u001B[31mT\u001B[0m"
                        + "\u001B[31mT\u001B[0m\u001B[32mC\u001B[0m\u001B[35mA\u001B[0m\u001B[32mC\u001B[0m"
                        + "\u001B[32mC\u001B[0m"));
        list.add(new ExampleTest("Color Output 3",
                "This example shows how to set a specific encoder.",
                "consensus -col 3 -c $Variant_Calls_TB -f $Reference_Genome_TB -r 103765-103785",
                ">gi|561108321|ref|NC_018143.2|103765-103785|\n"
                        + "GTGTTGGT\u001B[32mC\u001B[0mC\u001B[35mA\u001B[0m\u001B[32mC\u001B[0mGGTTGGCCG"));

        list.add(new ExampleTest("Specifying Sample Encoder",
                "This example shows how to set a specific encoder by using the -es option.",
                "consensus -c $Variant_Calls_TB -es -r 66200-66300",
                ">gi|561108321|ref|NC_018143.2|66200-66300|\n"
                        + "......................................................................\n"
                        + ".............G[GGGCATTCGCGCTGGCTGAGCGCCGA]"));
        list.add(new ExampleTest("Basic region",
                "This example shows the use of a region by the -r command line argument.",
                "consensus -c $Variant_Calls_TB"
                        + " -f $Reference_Genome_TB -r 10-100",
                ">gi|561108321|ref|NC_018143.2|10-100|\n"
                        + "GACCCCGGTTCAGGCTTCACCACAGTGTGGAACGCGGTCGTCTCCGAACTTAACGGCGACCCTAAGGTTG\n"
                        + "ACGACGGACCCAGCAGTGATG"));
        list.add(new ExampleTest("Multiple regions",
                "This example shows that it is possible to specify multiple regions using the "
                        + "-r argument and output will be split by headers",
                "consensus -c $Variant_Calls_TB "
                        + "-f $Reference_Genome_TB -r 10-100 600-700",
                ">gi|561108321|ref|NC_018143.2|10-100|\nGACCCCGGTTCAGGCTTCACCACAGTGTGGAACGCGGTC"
                        + "GTCTCCGAACTTAACGGCGACCCTAAGGTTG\nACGACGGACCCAGCAGTGATG\n>gi|561108321|ref|"
                        + "NC_018143.2|600-700|\nTTACAACCCCCTGTTCATCTGGGGCGAGTCCGGTCTCGGCAAGACACACCTGC"
                        + "TACACGCGGCAGGCAAC\nTATGCCCAACGGTTGTTCCCGGGAATGCGGG"));
        list.add(new ExampleTest("GFF3 regions",
                "This example show that you also can use an gff3 file to specify the regions",
                "consensus -c $Variant_Calls_TB -f $Reference_Genome_TB "
                        + "-a $GFF3_Annotations superOperon",
                ">gi|561108321|ref|NC_018143.2|10-100|superOperon|operon|\n"
                        + "GACCCCGGTTCAGGCTTCACCACAGTGTGGAACGCGGTCGTCTCCGAACTTAACGGCGACCCTAAGGTTG\n"
                        + "ACGACGGACCCAGCAGTGATG\n"
                        + ">gi|561108321|ref|NC_018143.2|600-700| |exon|\n"
                        + "TTACAACCCCCTGTTCATCTGGGGCGAGTCCGGTCTCGGCAAGACACACCTGCTACACGCGGCAGGCAAC\n"
                        + "TATGCCCAACGGTTGTTCCCGGGAATGCGGG"));
        list.add(new ExampleTest("Pylon VCF",
                "This example shows that you can also use an pylon vcf to make a sequence",
                "consensus -c $PYLON_1",
                "No regions where specified with either --region or --annotation, doing full region!\n"
                        + ">sampleGen|1-12|\n"
                        + "TTAGCCCCAGAC"));
    }

    private static void addTestsShowingCorrectness(List<ExampleTest> list) {
        list.add(new ExampleTest("Hetro-Double-Deletion",
                "This example shows how brackets are used to show heterozygous deletions,"
                        + "and shows a deletion inside another deletion.",
                "consensus -c $Small_Indels -r 1-14",
                ">CBS1|1-14|\nACAC[CACA[C]CA]CCA"));
        list.add(new ExampleTest("Insertions",
                "This example shows a homo- and heterozygous insertion.",
                "consensus -c $Small_Indels -r 51-63",
                ">CBS1|51-63|\nAAAAAAK(ARAAAAAAAAAAAAA)AAAAAT"));

    }
}
