package annotations;
import lombok.Builder;
import lombok.Getter;

/**
 * A data class containing all the information of an annotation as specified by GFF3.
 * Also contains the children of this annotation.
 * Can recursively generate the region this annotation and its children has.
 */
@Builder
class AnnotationData {
    /**
     * The ID of the landmark used to establish the coordinate system for the current feature.
     * The value of the first column in GFF3
     */
    @Getter
    private final String sequenceID;

    /**
     * The source is a qualifier intended to describe the algorithm or operating procedure that generated this feature.
     */
    @Getter
    private final String source;

    /**
     * The type of the feature, such as Gene, mRNA, CDS.
     */
    @Getter
    private final String type;

    /**
     * The start coordinate of the feature are given in positive 1-based integer coordinates,
     * relative to the landmark given in column one.
     * Start is always less than or equal to end.
     * For features that cross the origin of a circular feature
     * (e.g. most bacterial genomes, plasmids, and some viral genomes),
     * the requirement for start to be less than or equal to end is satisfied
     * by making end = the position of the end + the length of the landmark feature.
     * For zero-length features, such as insertion sites,
     * start equals end and the implied site is to the right of the indicated base in the direction of the landmark.
     */
    @Getter
    private final int start;

    /**
     * The end coordinate of the feature are given in positive 1-based integer coordinates, relative to sequenceID.
     * Start is always less than or equal to end.
     * For features that cross the origin of a circular feature
     * (e.g. most bacterial genomes, plasmids, and some viral genomes),
     * the requirement for start to be less than or equal to end is satisfied
     * by making end = the position of the end + the length of the landmark feature.
     * For zero-length features, such as insertion sites,
     * start equals end and the implied site is to the right of the indicated base in the direction of the landmark.
     */
    @Getter
    private final int end;

    /**
     * The score of the feature, a floating point number.
     * -1 if undefined.
     */
    @Getter
    @Builder.Default
    private final double score = -1;

    /**
     * True if the sense/antisense is known and applicable.
     */
    @Getter
    @Builder.Default
    private final boolean stranded = false;

    /**
     * If true, it is a positive strand relative to the landmark. If false, a negative strand.
     */
    @Getter
    @Builder.Default
    private  final boolean sense = true;

    /**
     * For features of type "CDS", the phase indicates where the feature begins with reference to the reading frame.
     * The phase is one of the integers 0, 1, or 2, indicating the number of bases that should be removed
     * from the beginning of this feature to reach the first base of the next codon.
     * In other words, a phase of "0" indicates that the next codon
     * begins at the first base of the region described by the current line,
     * a phase of "1" indicates that the next codon begins at the second base of this region,
     * and a phase of "2" indicates that the codon begins at the third base of this region.
     * This is NOT to be confused with the frame, which is simply start modulo 3.
     * <p>
     * For forward strand features, phase is counted from the start field.
     * For reverse strand features, phase is counted from the end field.
     */
    @Getter
    @Builder.Default
    private  final int phase = 0;

    /**
     * Indicates the ID of the feature.
     * The ID attribute is required for features that have children (e.g. gene and mRNAs),
     * or for those that span multiple lines, but are optional for other features.
     * IDs for each feature must be unique within the scope of the GFF file.
     * In the case of discontinuous features (i.e. a single feature that exists over multiple genomic locations)
     * the same ID may appear on multiple lines.
     * <p>
     * All lines that share an ID must collectively represent a single feature.
     */
    @Getter
    private final String id;

    /**
     * Display name for the feature. This is the name to be displayed to the user.
     * Unlike IDs, there is no requirement that the Name be unique within the file.
     */
    @Getter
    private final String name;

    /**
     * A secondary name for the feature.
     * It is suggested that this tag be used whenever a secondary identifier for the feature is needed,
     * such as locus names and accession numbers.
     * Unlike ID, there is no requirement that Alias be unique within the file.
     */
    @Getter
    private final String alias;

    /**
     * The alignment of the feature to the target if the two are not collinear (e.g. contain gaps).
     */
    @Getter
    private final String gap;

    /**
     * Used to disambiguate the relationship between one feature and another
     * when the relationship is a temporal one rather than a purely structural "part of" one.
     */
    @Getter
    private final String derivesFrom;

    /**
     * A free text note.
     */
    @Getter
    private final String note;

    /**
     * A cross reference to an ontology term.
     */
    @Getter
    private final String ontologyTerm;

    /**
     * A database cross reference.
     */
    @Getter
    private final String dbxref;

    /**
     * Indicates the target of a nucleotide-to-nucleotide or protein-to-nucleotide alignment.
     * The format of the value is "target_id start end [strand]", where strand is optional and may be "+" or "-".
     * If the target_id contains spaces, they must be escaped as hex escape %20.
     */
    @Getter
    private final String target;

    /**
     * A flag to indicate whether a feature is circular.
     */
    @Getter
    @Builder.Default
    private final boolean isCircular = false;
}
