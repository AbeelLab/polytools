package annotations;

import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A class that reflects the annotations present in a GFF3 file.
 * Provides methods for retrieving annotations by several criteria.
 */
public class GFF {
    @Getter
    private List<Annotation> annotations;

    /**
     * Constructs a GFF object from the collection of annotations in a GFF3 file.
     *
     * @param gffFile GFF3 file that contains annotations.
     * @throws FileNotFoundException If the specified GFF3 file can't be found.
     * @throws IOException If something went wrong with reading the file.
     */
    public GFF(File gffFile) throws IOException {
        this.annotations = new ArrayList<Annotation>();
        annotations.addAll(new GffParser(gffFile).parse());
    }

    /**
     * Constructs a GFF object from the given the given list of annotations.
     *
     * @param annotations List of annotations.
     */
    public GFF(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    /**
     * Recursively retrieves all annotations and children of this collection.
     *
     * @return new GFF collection that contains all annotations and children.
     * @throws Annotation.RecursionException Occurs when there is a cycle in at least one of the traversed
     *                                       parent-child relationships.
     */
    public GFF getAll() throws Annotation.RecursionException {
        List<Annotation> results = new ArrayList<>();
        for (Annotation annotation : annotations) {
            results.addAll(annotation.getDescendants());
        }
        return new GFF(results);
    }

    /**
     * Creates a new collection of annotations that is a subset of this collection and for which the
     * specified predicate is true.
     *
     * @param predicate Predicate which must hold for required Annotations
     * @return A new collection of annotations that is a subset of this collection and for which the
     * specified predicate is true.
     */
    private GFF filter(Predicate<Annotation> predicate) {
        return new GFF(annotations.stream().filter(predicate).collect(Collectors.toList()));
    }

    /**
     * Creates a new collection of annotations that is a subset of this collection and only contains
     * annotations with the specified name and all of their children (which may be named different).
     *
     * @param name The value in the 'Name' column of the desired rows of annotations.
     * @return a new collection of annotations that is a subset of this collection and only contains
     * annotations with the specified name and all of their children (which may be named different).
     * @throws Annotation.RecursionException Occurs when there is a cycle in at least one of the traversed
     *                                       parent-child relationships.
     */
    public GFF getAllByName(String name) throws Annotation.RecursionException {
        return filter(a -> a.getName() != null && a.getName().equals(name)).getAll();
    }

    /**
     * Creates a new collection of annotations that is a subset of this collection and only contains
     * annotations that have no parents.
     *
     * @return a new collection of annotations that is a subset of this collection and only contains
     * annotations that have no parents.
     * @throws Annotation.RecursionException Occurs when there is a cycle in at least one of the traversed
     *                                       parent-child relationships.
     */
    public GFF getAllRoots() throws Annotation.RecursionException {
        return filter(a -> a.getParents() != null && a.getParents().isEmpty()).getAll();
    }

    /**
     * Creates a new collection of annotations that is a subset of this collection and only contains
     * annotations with the specified name.
     *
     * @param name The value in the 'Name' column of the desired rows of annotations.
     * @return a new collection of annotations that is a subset of this collection and only contains
     * annotations with the specified name.
     */
    public GFF getByName(String name) {
        return filter(a -> a.getName().equals(name));
    }
}
